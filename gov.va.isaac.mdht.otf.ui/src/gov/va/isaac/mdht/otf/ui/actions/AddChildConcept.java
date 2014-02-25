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
package gov.va.isaac.mdht.otf.ui.actions;

import gov.va.isaac.mdht.otf.services.ConceptBuilderService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.ui.providers.ConceptItem;
import gov.va.isaac.mdht.otf.ui.views.TaxonomyView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IActionDelegate;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

public class AddChildConcept extends AbstractAction {

	private List<ConceptItem> conceptItems = null;
	
	/**
	 * Constructor for Action1.
	 */
	public AddChildConcept() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (conceptItems != null) {
			for (ConceptItem parentItem : conceptItems) {
				try {
					ConceptVersionBI parent = parentItem.getConceptVersion();
					
					String preferredName = null;
					String fsn = null;
					//prompt for Preferred Name, then derive FSN using suffix from parent
					InputDialog inputDialog = new InputDialog(
							activePart.getSite().getShell(), "New Concept", "Enter preferred Name", "", null);
					if (inputDialog.open() == Window.OK) {
						String value = inputDialog.getValue();
						if (value != null && value.length() > 0) {
							preferredName = value;
							String suffix = getFsnSuffix(parent);
							if (suffix == null) {
								suffix = "(top)";
							}
							fsn = preferredName + " " + suffix;
						}
					}
					
					if (preferredName != null && fsn != null) {
						ConceptBuilderService builder = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
						ConceptCB childBlueprint = builder.createConcept(parent, fsn, preferredName);
						
						// for now, build uncommitted concept so that taxonomy getChildren() includes the new concept
						ConceptVersionBI childVersion = builder.construct(childBlueprint);
						
						// is there a more general API interface for this?
						if (activePart instanceof TaxonomyView) {
							((TaxonomyView)activePart).getViewer().refresh(parentItem);
							((TaxonomyView)activePart).getViewer().reveal(childVersion);
						}
						
						//TODO refresh Properties view
					}
					
				} catch (IOException | ContradictionException | InvalidCAB e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getFsnSuffix(ConceptVersionBI concept) {
		String suffix = null;
		try {
			String fsnText = concept.getFullySpecifiedDescription().getText();
			int suffixIndex = fsnText.indexOf("(");
			if (suffixIndex > 0) {
				suffix = fsnText.substring(suffixIndex);
			}
		} catch (IOException | ContradictionException e) {
			// return null suffix
		}
		
		return suffix;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		conceptItems = new ArrayList<ConceptItem>();
		
		if (currentSelection != null) {
			for (Object element : ((IStructuredSelection) currentSelection).toList()) {
				if (((ConceptItem)element).getConceptVersion() != null) {
					conceptItems.add((ConceptItem)element);
				}
			}
		}
	}

}
