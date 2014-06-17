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
import gov.va.isaac.mdht.otf.ui.dialogs.ConceptListDialog;
import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.internal.l10n.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 * The properties section for Annotation Refset members.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class AnnotationSection extends RefsetMemberSection {

	protected ConceptVersionBI getAnnotationRefset() {
		ConceptVersionBI refset = null;
		
		IFilter annotationRefsetFilter = new IFilter() {
			@Override
			public boolean select(Object object) {
				try {
					if ((object instanceof ConceptVersionBI)
							&& ((ConceptVersionBI)object).isAnnotationStyleRefex()) {
						return true;
					}
				} catch (IOException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in isAnnotationStyleRefex()", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
				
				return false;
			}
		};
		
		ConceptListDialog searchDialog = new ConceptListDialog(getPart().getSite().getShell(),
				Messages.RefsetSelection_input_title, Messages.ConceptSelection_input_message, annotationRefsetFilter);
		int result = searchDialog.open();
		if (Dialog.OK == result && searchDialog.getResult().length == 1) {
			refset = (ConceptVersionBI) searchDialog.getResult()[0];
		}

		if (refset == null) {
			// prompt for concept UUID
			InputDialog inputDialog = new InputDialog(
					getPart().getSite().getShell(), "Refset UUID", "Enter Refset Concept UUID", "", null);
			if (inputDialog.open() == Window.OK) {
				String uuidString = inputDialog.getValue();
				if (uuidString != null && uuidString.length() > 0) {
					refset = queryService.getConcept(uuidString);
				}
			}
		}
		return refset;
	}

	/*
	protected RefexVersionBI<?> buildAndCommit() {
		RefexVersionBI<?> refexVersion = super.buildAndCommit();
		
		try {
			if (!getAnnotationRefset().isAnnotationStyleRefex()) {
				// TODO class cast exception.  Only works for Member, not extension Refex classes??
				conceptVersion.addAnnotation(refexVersion);
			}
		} catch (IOException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in isAnnotationStyleRefex()", e), 
			StatusManager.SHOW | StatusManager.LOG);
		}
		
		return refexVersion;
	}
	*/
	
	@Override
	protected void addMember() {
		try {
			ConceptVersionBI refset = getAnnotationRefset();
			
			if (refset != null) {
				RefsetMember member = new RefsetMember(refset);
				member.setReferencedComponent(conceptVersion);
				newMembers.add(member);
			}
			
		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot add refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	@Override
	protected GenericRefexTableViewer createRefexViewer(Table table) {
		GenericRefexTableViewer viewer = new GenericRefexTableViewer(table) {
			@Override
		    protected String[] getColumnTitles() {
		        String[] titles = { "Refset", "Value", "Component 1", "Component 2", "Component 3" };
		        return titles;
		    }

		    protected RefsetAttributeType[] getColumnTypes() {
		    	RefsetAttributeType[] memberKinds = { RefsetAttributeType.Concept, RefsetAttributeType.String, RefsetAttributeType.Concept, RefsetAttributeType.Concept, RefsetAttributeType.Concept };
		        return memberKinds;
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
			protected IContentProvider createContentProvider() {
		        return new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) {
						try {
							List<Object> members = new ArrayList<Object>();
							
							Collection<? extends RefexVersionBI<?>> activeMembers = conceptVersion.getAnnotationsActive(storeService.getSnomedStatedLatest());
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
		
		createMembersComposite(parent);
	}

}
