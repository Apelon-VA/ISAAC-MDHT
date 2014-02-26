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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

public class CancelConceptChanges extends AbstractAction {

	private List<ConceptItem> conceptItems = null;
	
	/**
	 * Constructor for Action1.
	 */
	public CancelConceptChanges() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (conceptItems != null) {
			for (ConceptItem conceptItem : conceptItems) {
				try {
					ConceptItem parentItem = conceptItem.getParent();
					ConceptVersionBI conceptVersion = conceptItem.getConceptVersion();
					
					TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
					storeService.forget(conceptVersion);
					
					// is there a more general API interface for this?
					if (activePart instanceof TaxonomyView) {
						((TaxonomyView)activePart).getViewer().setSelection(new StructuredSelection(parentItem));
						((TaxonomyView)activePart).getViewer().refresh(parentItem);
					}
					
					//TODO refresh Properties view
					
				} catch (IOException e) {
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
		boolean isUncommitted = true;
		
		if (currentSelection != null) {
			for (Object element : ((IStructuredSelection) currentSelection).toList()) {
				ConceptVersionBI conceptVersion = ((ConceptItem)element).getConceptVersion();
				if (conceptVersion != null) {
					if (!conceptVersion.isUncommitted()) {
						isUncommitted = false;
						break;
					}
					conceptItems.add((ConceptItem)element);
				}
			}
		}
		action.setEnabled(isUncommitted);
	}

}
