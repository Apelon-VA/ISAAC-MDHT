/*******************************************************************************
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package gov.va.isaac.mdht.otf.ui.properties;


import gov.va.isaac.mdht.otf.refset.RefsetAttributeType;
import gov.va.isaac.mdht.otf.refset.RefsetMember;
import gov.va.isaac.mdht.otf.services.ConceptBuilderService;
import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 * The properties section for Refset members.  Abstract superclass provides member table list for use in
 * RefsetSpecificationSection and AnnotationSection.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public abstract class RefsetMemberSection extends AbstractPropertySection {

	protected TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	protected ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	protected ConceptBuilderService builderService = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
	
	protected ConceptVersionBI conceptVersion;

	protected List<RefsetMember> newMembers = new ArrayList<RefsetMember>();
	
	protected GenericRefexTableViewer refexViewer = null;
	
	protected RefsetAttributeType referencedComponentKind = RefsetAttributeType.Concept;

	protected RefsetAttributeType valueKind = RefsetAttributeType.String;

	protected boolean dirty = false;
	
	protected Button addButton = null;

	protected Button removeButton = null;

	protected Button editButton = null;

	protected Button saveButton = null;
	
	/**
	 * Create blueprint and build chronicle
	 */
	protected void buildAndCommit() {
		List<RefsetMember> newMembersCopy = new ArrayList<RefsetMember>(newMembers);
		for (RefsetMember refsetMember : newMembersCopy) {
			try {
				refsetMember.validateRefex();
			} catch (Exception e) {
				MessageDialog.open(MessageDialog.ERROR, getPart().getSite().getShell(), 
						"Invalid Refex", e.getMessage(), SWT.NONE);
				continue;
			}

			try {
				RefexCAB refexCAB = refsetMember.createBlueprint();
				refexCAB.recomputeUuid();
				builderService.construct(refexCAB);
				newMembers.remove(refsetMember);

			} catch (IOException | ContradictionException | InvalidCAB e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot build Refex version", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}

		try {
			// commit enclosing concept
			storeService.addUncommitted(conceptVersion);
			if (newMembers.isEmpty()) {
				dirty = false;
			}
			
		} catch (IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot commit refset member(s)", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	protected void addMember() {
		try {
			ComponentVersionBI component = refexViewer.getMemberComponent(referencedComponentKind);
			
			if (component != null) {
				RefsetMember member = new RefsetMember(conceptVersion);
				member.setReferencedComponent(component);
				newMembers.add(member);
			}
			
		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot add refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	protected void retireMember(final RefexVersionBI<?> refex) {
		try {
			RefexCAB blueprint = refex.makeBlueprint(storeService.getSnomedStatedLatest(),
					IdDirective.PRESERVE, RefexDirective.INCLUDE);

			// TODO for now, required workaround to eliminate NPE
			if (blueprint.getMemberUUID() == null) {
				blueprint.setMemberUuid(refex.getPrimordialUuid());
			}
			
			blueprint.setRetired();
			builderService.construct(blueprint);
			
			dirty = true;

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot retire refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	protected void editMember(final RefexVersionBI<?> refex) {
		try {
			RefsetMember modified = new RefsetMember(refex);
			newMembers.add(modified);

			retireMember(refex);
			
//			if (refex.isUncommitted()) {
//				storeService.forget(refex);
//			}
//			else {
//				retireMember(refex);
//			}

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot edit refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	protected void updateContentState(ConceptVersionBI concept) {
		// may be overridden by subclasses
	}

	protected void updateContentState(Collection<? extends RefexVersionBI<?>> members) {
		// may be overridden by subclasses
	}
	
	protected Composite createMembersComposite(Composite parent) {
		Composite membersComposite = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		membersComposite.setLayout(layout);

		addButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image addImage = Activator.getDefault().getBundledImage("icons/eview16/add.gif");
		addButton.setImage(addImage);
		addButton.setToolTipText("Add refset member");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addMember();
				refexViewer.refresh();
			}
		});

		removeButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image removeImage = Activator.getDefault().getBundledImage("icons/eview16/delete.gif");
		removeButton.setImage(removeImage);
		removeButton.setToolTipText("Retire selected member(s)");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = refexViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof RefexVersionBI) {
							retireMember((RefexVersionBI<?>)selected);
						}
						else if (selected instanceof RefsetMember) {
							newMembers.remove((RefsetMember)selected);
						}
						refexViewer.refresh();
					}
				}
			}
		});

		editButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image editImage = Activator.getDefault().getBundledImage("icons/eview16/write.gif");
		editButton.setImage(editImage);
		editButton.setToolTipText("Edit selected refset member");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = refexViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof RefexVersionBI) {
							editMember((RefexVersionBI<?>)selected);
						}
					}
					refexViewer.refresh();
				}
			}
		});

		saveButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image saveImage = Activator.getDefault().getBundledImage("icons/eview16/save.gif");
		saveButton.setImage(saveImage);
		saveButton.setToolTipText("Save all changes");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buildAndCommit();
				refexViewer.refresh();
			}
		});

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		addButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(addButton, 0);
		removeButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(removeButton, 0);
		editButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(editButton, 0);
		saveButton.setLayoutData(data);
		
		Table table = getWidgetFactory().createTable(membersComposite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		data = new FormData();
		data.left = new FormAttachment(addButton, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		table.setLayoutData(data);

		refexViewer = createRefexViewer(table);
		
		return membersComposite;
	}
	
	protected GenericRefexTableViewer createRefexViewer(Table table) {
		GenericRefexTableViewer viewer = new GenericRefexTableViewer(table) {
			@Override
		    protected RefsetAttributeType[] getColumnTypes() {
		    	RefsetAttributeType[] memberKinds = { referencedComponentKind, valueKind, RefsetAttributeType.Concept, RefsetAttributeType.Concept, RefsetAttributeType.Concept };
		        return memberKinds;
		    }

			@Override
			protected IContentProvider createContentProvider() {
		        return new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) {
						try {
							List<Object> members = new ArrayList<Object>();
							
							Collection<? extends RefexVersionBI<?>> activeMembers = conceptVersion.getRefsetMembersActive();
							members.addAll(activeMembers);
							updateContentState(activeMembers);
							
							members.addAll(newMembers);
							saveButton.setEnabled(!newMembers.isEmpty());
							
							return members.toArray();
							
						} catch (Exception e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get refset members", e), 
									StatusManager.SHOW | StatusManager.LOG);
							return new Object[0];
						}
					}
					
					public void dispose() { }
					
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
						updateContentState(conceptVersion);

						if (!removeButton.isDisposed()) {
							removeButton.setEnabled(false);
							editButton.setEnabled(false);
						}

						if (!saveButton.isDisposed()) {
							saveButton.setEnabled(!newMembers.isEmpty());
						}
					}
		        	
		        };
			}

			@Override
			protected IWorkbenchPart getActivePart() {
				return getPart();
			}

			@Override
			protected void fireSelectionChanged(SelectionChangedEvent event) {
				super.fireSelectionChanged(event);
				
				boolean enabled = !event.getSelection().isEmpty();
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();	
				removeButton.setEnabled(enabled);
				editButton.setEnabled(enabled && !(selection instanceof CreateOrAmendBlueprint)
						&& !(selection instanceof RefsetMember));
			}
		};
		
		return viewer;
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		
//		createMembersComposite(parent);
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		
		if (dirty || newMembers.size() > 0) {
			buildAndCommit();
		}
		
		conceptVersion = null;
		newMembers = new ArrayList<RefsetMember>();
		
		Object selected = ((IStructuredSelection)selection).getFirstElement();
		if (selected instanceof ConceptVersionBI) {
			conceptVersion = (ConceptVersionBI) selected;
		}
		if (selected instanceof IAdaptable) {
			Object adapted = (ComponentVersionBI) ((IAdaptable)selected).getAdapter(ComponentVersionBI.class);
			if (adapted instanceof ConceptVersionBI) {
				conceptVersion = (ConceptVersionBI) adapted;
			}
		}
	}

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}
	@Override
	public void dispose() {
		super.dispose();

	}

	@Override
	public void refresh() {
		refexViewer.setInput(conceptVersion);
	}

}
