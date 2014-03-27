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


import gov.va.isaac.mdht.otf.refset.RefsetMember;
import gov.va.isaac.mdht.otf.services.ConceptBuilderService;
import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 * The properties section for Annotation Refset members.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class AnnotationSection extends AbstractPropertySection {

	private TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	private ConceptBuilderService builderService = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
	
	protected ConceptVersionBI conceptVersion;
	
	private boolean dirty = false;
	
	private GenericRefexTableViewer refexViewer = null;
	
	private Button removeButton = null;
	
	private void retireMember(final RefexVersionBI<?> refex) {
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

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		
		Composite membersComposite = getWidgetFactory().createComposite(parent);
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		membersComposite.setLayout(layout);
		
//		FormData data = new FormData();
//		data.left = new FormAttachment(0, 0);
//		data.top = new FormAttachment(0, 0);
//		membersComposite.setLayoutData(data);

		removeButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image removeImage = Activator.getDefault().getBundledImage("icons/eview16/remove.gif");
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
						refexViewer.refresh();
					}
				}
			}
		});

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		removeButton.setLayoutData(data);

		Table table = getWidgetFactory().createTable(membersComposite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		data = new FormData();
		data.left = new FormAttachment(removeButton, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		table.setLayoutData(data);

		refexViewer = new GenericRefexTableViewer(table) {
			@Override
		    protected String[] getColumnTitles() {
		        String[] titles = { "Refset", "Value", "Component 1", "Component 2", "Component 3" };
		        return titles;
		    }

			@Override
			protected ComponentVersionBI getFirstColumnComponent(RefexVersionBI<?> refex) {
				ComponentVersionBI component = null;
				int nid = refex.getAssemblageNid();
				if (nid != 0) {
					component = queryService.getComponent(nid);
				}
				return component;
			}

			@Override
			protected ComponentVersionBI getFirstColumnComponent(RefsetMember refex) {
				return refex.getRefsetConcept();
			}


			@Override
			protected IWorkbenchPart getActivePart() {
				return getPart();
			}

			@Override
			protected void fireSelectionChanged(SelectionChangedEvent event) {
				super.fireSelectionChanged(event);
				
				boolean enabled = !event.getSelection().isEmpty();
//				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();	
				removeButton.setEnabled(enabled);
			}

			@Override
			protected IContentProvider createContentProvider() {
		        return new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) {
						try {
							List<Object> members = new ArrayList<Object>();
							
							members.addAll(conceptVersion.getAnnotationsActive(storeService.getSnomedStatedLatest()));
							
							return members.toArray();
							
						} catch (Exception e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get refset members", e), 
									StatusManager.SHOW | StatusManager.LOG);
							return new Object[0];
						}
					}
					
					public void dispose() { }
					
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
						if (!removeButton.isDisposed()) {
							removeButton.setEnabled(false);
						}
					}
		        	
		        };
			}
		};

	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		
		conceptVersion = null;
		
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
	public void dispose() {
		super.dispose();

	}

	@Override
	public void refresh() {
		refexViewer.setInput(conceptVersion);
	}

}
