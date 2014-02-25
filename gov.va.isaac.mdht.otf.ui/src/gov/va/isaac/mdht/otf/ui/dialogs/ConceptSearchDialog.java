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

import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.ui.internal.l10n.Messages;
import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * A dialog to select a concept from a list of concepts. The dialog allows
 * multiple selections.
 */
public class ConceptSearchDialog extends ElementListSelectionDialog {
	
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	
	private String inputTitle;
	private String inputMessage;

	private IRunnableContext fRunnableContext;

	/*
	private ICTS2SearchScope fScope;

	public ConceptSearchDialog(Shell shell, IRunnableContext context, ICTS2SearchScope scope) {
		super(shell, new ComponentLabelProvider(true));

		setMultipleSelection(true);
		setTitle(Messages.ConceptSelection_dialog_title);
		setMessage(Messages.ConceptSelection_dialog_message);

		Assert.isNotNull(context);
		Assert.isNotNull(scope);

		fRunnableContext = context;
		fScope = scope;
	}
	*/

	public ConceptSearchDialog(Shell shell) {
		this(shell, Messages.ConceptSelection_input_title, Messages.ConceptSelection_input_message);
	}

	public ConceptSearchDialog(Shell shell, String inputTitle, String inputMessage) {
		super(shell, new ComponentLabelProvider(true));

		setMultipleSelection(false);
		setTitle(Messages.ConceptSelection_dialog_title);
		setMessage(Messages.ConceptSelection_dialog_message);
		this.inputTitle = inputTitle;
		this.inputMessage = inputMessage;
	}

	public String getMatchValue() {
		InputDialog input = new InputDialog(getParentShell(), inputTitle, inputMessage, "", null);
		int result = input.open();
		if (result == InputDialog.CANCEL) {
			return null;
		}
		return input.getValue();
	}

	/*
	 * @see Window#open()
	 */
	@Override
	public int open() {
		String matchvalue = getMatchValue();
		if (matchvalue != null && matchvalue.length() > 0) {
			try {
				matchvalue = URLEncoder.encode(matchvalue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// should not occur, but use original matchvalue
			}
			
			try {
//				List<ComponentVersionBI> concepts = queryService.getLuceneMatch(matchvalue);
				List<ConceptVersionBI> concepts = queryService.searchActiveConcepts(matchvalue);
		
				setElements(concepts.toArray());

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return super.open();
		}
		else {
			return Dialog.CANCEL;
		}
	}

}
