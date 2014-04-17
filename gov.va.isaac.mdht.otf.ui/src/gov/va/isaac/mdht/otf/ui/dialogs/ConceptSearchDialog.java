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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * A dialog to select a concept from a list of concepts. The dialog does not allow
 * multiple selections.
 */
public class ConceptSearchDialog extends ElementListSelectionDialog {
	
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	
	private String inputTitle;
	private String inputMessage;
	
	private IFilter filter = null;

	public ConceptSearchDialog(Shell shell) {
		this(shell, Messages.ConceptSelection_input_title, Messages.ConceptSelection_input_message, null);
	}

	public ConceptSearchDialog(Shell shell, String inputTitle, String inputMessage) {
		this(shell, inputTitle, inputMessage, null);
	}

	public ConceptSearchDialog(Shell shell, String inputTitle, String inputMessage, IFilter filter) {
		super(shell, new ComponentLabelProvider(true));

		setMultipleSelection(false);
		setTitle(Messages.ConceptSelection_dialog_title);
		setMessage(Messages.ConceptSelection_dialog_message);
		this.inputTitle = inputTitle;
		this.inputMessage = inputMessage;
		this.filter = filter;
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
