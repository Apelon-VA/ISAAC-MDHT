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
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.providers.ConceptItem;
import gov.va.isaac.mdht.otf.ui.views.TaxonomyView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

public class RetireConcept extends AbstractAction {

	private List<ConceptItem> conceptItems = null;
	
	/**
	 * Constructor for Action1.
	 */
	public RetireConcept() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (conceptItems != null) {
			for (ConceptItem conceptItem : conceptItems) {
				try {
					TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
					ConceptBuilderService builderService = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
					
					ConceptVersionBI conceptVersion = conceptItem.getConceptVersion();

					ConceptCB blueprint = conceptVersion.makeBlueprint(
							storeService.getSnomedStatedLatest(),
							IdDirective.PRESERVE,
							RefexDirective.INCLUDE);
					
					blueprint.setRetired();
					builderService.construct(blueprint);
					
//					ViewCoordinate vc = storeService.getSnomedStatedLatest();
//					ConceptAttributeVersionBI attr = conceptVersion.getConceptAttributes().getVersion(vc);
//					ConceptAttributeAB attrBlueprint = attr.makeBlueprint(vc,  IdDirective.PRESERVE,  RefexDirective.EXCLUDE);
//					attrBlueprint.setRetired();
//					
//					builderService.construct(attrBlueprint);
					
					// is there a more general API interface for this?
					if (activePart instanceof TaxonomyView) {
						((TaxonomyView)activePart).getViewer().refresh(conceptItem.getParent());
					}
					
					//TODO refresh Properties view
					
				} catch (IOException | ContradictionException | InvalidCAB e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		conceptItems = new ArrayList<ConceptItem>();
		boolean isCommitted = true;
		
		if (currentSelection != null) {
			for (Object element : ((IStructuredSelection) currentSelection).toList()) {
				ConceptVersionBI conceptVersion = ((ConceptItem)element).getConceptVersion();
				if (conceptVersion != null) {
					if (conceptVersion.isUncommitted()) {
						isCommitted = false;
						break;
					}
					conceptItems.add((ConceptItem)element);
				}
			}
		}
		action.setEnabled(isCommitted);
	}

}
