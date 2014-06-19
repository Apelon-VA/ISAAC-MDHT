/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.mdht.otf.ui.dialogs;

import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.internal.l10n.Messages;
import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * A dialog to select a concept from a list of concepts. The dialog does not allow
 * multiple selections.
 */
public class ConceptListDialog extends ElementListSelectionDialog {
	
	private String inputTitle;
	private String inputMessage;
	
	private List<ConceptVersionBI> concepts = null;
	
	private IFilter filter = null;

	public ConceptListDialog(Shell shell) {
		this(shell, Messages.ConceptSelection_input_title, Messages.ConceptSelection_input_message, null);
	}

	public ConceptListDialog(Shell shell, String inputTitle, String inputMessage) {
		this(shell, inputTitle, inputMessage, null);
	}

	public ConceptListDialog(Shell shell, String inputTitle, String inputMessage, IFilter filter) {
		super(shell, new ComponentLabelProvider(true));

		setMultipleSelection(false);
		setTitle(Messages.ConceptSelection_dialog_title);
		setMessage(Messages.ConceptSelection_dialog_message);
		this.inputTitle = inputTitle;
		this.inputMessage = inputMessage;
		this.filter = filter;
	}
	
	public void setConceptList(List<ConceptVersionBI> concepts) {
		this.concepts = concepts;
	}

	/*
	 * @see Window#open()
	 */
	@Override
	public int open() {
		if (concepts == null) {
			try {
				ConceptSearchDialog searchDialog = new ConceptSearchDialog(getParentShell());
				searchDialog.open();
				concepts = searchDialog.getResults();
	
			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in Query Services", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		
		if (concepts == null || concepts.isEmpty()) {
			return Dialog.CANCEL;
		}
			
		List<ConceptVersionBI> filteredConcepts = new ArrayList<ConceptVersionBI>();
		if (filter != null) {
			for (ConceptVersionBI conceptVersionBI : concepts) {
				if (filter.select(conceptVersionBI)) {
					filteredConcepts.add(conceptVersionBI);
				}
			}
		}
		else {
			filteredConcepts = concepts;
		}

		setElements(filteredConcepts.toArray());
		
		return super.open();
	}

}
