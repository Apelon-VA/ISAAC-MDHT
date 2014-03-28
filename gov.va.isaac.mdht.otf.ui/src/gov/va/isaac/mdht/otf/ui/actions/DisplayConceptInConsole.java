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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * This action displays a debug dump of a concept in the Console.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class DisplayConceptInConsole extends AbstractAction {
	
	private MessageConsole messageConsole = null;

	private ConceptPrinterService printService = TerminologyStoreFactory.INSTANCE.createConceptPrinterService();
	private List<ComponentBI> components = null;
	
	/**
	 * Constructor for Action1.
	 */
	public DisplayConceptInConsole() {
		super();
	}
	
	public MessageConsole getConsole() {
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		
		if (messageConsole == null) {
			IConsole[] consoles = consoleManager.getConsoles();
			for (int i = 0; i < consoles.length; i++) {
				if (consoles[i] instanceof MessageConsole && "ISAAC".equals(consoles[i].getName())) {
					messageConsole = (MessageConsole) consoles[i];
					break;
				}
			}
			if (messageConsole == null) {
				messageConsole = new MessageConsole("ISAAC", null);
				IConsole[] isaacConsoles = { messageConsole };
				consoleManager.addConsoles(isaacConsoles);
			}
		}
		
		consoleManager.showConsoleView(messageConsole);
		return messageConsole;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (components != null) {
			MessageConsole isaacConsole = getConsole();
			IOConsoleOutputStream output = isaacConsole.newOutputStream();
			PrintStream printStream = new PrintStream(output);
			
			for (ComponentBI component : components) {
				if (component instanceof ConceptVersionBI) {
					ConceptVersionBI concept = (ConceptVersionBI) component;
					printService.printConcept(concept, printStream);
				}
			}
			
			try {
				output.flush();
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
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
