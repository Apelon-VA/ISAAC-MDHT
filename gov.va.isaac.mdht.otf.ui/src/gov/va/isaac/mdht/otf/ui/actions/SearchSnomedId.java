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

import gov.va.isaac.mdht.otf.search.IdentifierSearch;
import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.providers.ConceptItem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.statushandlers.StatusManager;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class SearchSnomedId extends AbstractAction {

	private List<ConceptItem> conceptItems = null;
	
	public SearchSnomedId() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		String id = getIdentifier();
		if (id != null && id.length() > 0) {
			IdentifierSearch search = new IdentifierSearch();
			
			try {
				Query query = search.getQueryForSnomedId(id);
				List<ConceptVersionBI> results = search.getQueryResultConcepts(query);
				for (ConceptVersionBI concept : results) {
					System.out.println(concept.getPreferredDescription().getText());
				}
			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in Query Services", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		
	}

	protected String getIdentifier() {
		InputDialog input = new InputDialog(activePart.getSite().getShell(), "Search for ID", "Enter concept identifier", "", null);
		int result = input.open();
		if (result == InputDialog.CANCEL) {
			return null;
		}
		return input.getValue();
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
