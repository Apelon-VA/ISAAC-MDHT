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

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

public abstract class ConceptComboBoxEditingSupport extends OTFTableEditingSupport {
	ComboBoxCellEditor comboBoxEditor = null;
	private List<ConceptVersionBI> conceptList = null;
	private String[] conceptNames = null;
	
	public void setConceptList(List<ConceptVersionBI> conceptList) {
		this.conceptList = conceptList;
		conceptNames = new String[conceptList.size()];
		int index = 0;
		for (ConceptVersionBI conceptVersionBI : conceptList) {
			try {
				conceptNames[index++] = conceptVersionBI.getPreferredDescription().getText();
			} catch (IOException | ContradictionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ConceptComboBoxEditingSupport(OTFTableViewer viewer, List<ConceptVersionBI> conceptList) {
		super(viewer);
		setConceptList(conceptList);
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		comboBoxEditor = new ComboBoxCellEditor(tableViewer.getTable(), conceptNames);
		
		return comboBoxEditor;
	}

	protected ConceptVersionBI getSelectedConcept() {
		ConceptVersionBI concept = null;
		Integer selected = (Integer) comboBoxEditor.getValue();
		if (selected != null) {
			concept = conceptList.get(selected);
		}
		return concept;
	}
	
}
