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

import gov.va.isaac.mdht.otf.services.ConceptPrinterService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * This action displays a debug dump of a concept in the Console.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class DisplayConceptInConsole extends AbstractAction {

	private ConceptPrinterService printService = TerminologyStoreFactory.INSTANCE.createConceptPrinterService();
	private List<ComponentBI> components = null;
	
	/**
	 * Constructor for Action1.
	 */
	public DisplayConceptInConsole() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (components != null) {
			for (ComponentBI component : components) {
				if (component instanceof ConceptVersionBI) {
					ConceptVersionBI concept = (ConceptVersionBI) component;
					printService.printConcept(concept);
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		components = getSelectedComponents();
	}

}
