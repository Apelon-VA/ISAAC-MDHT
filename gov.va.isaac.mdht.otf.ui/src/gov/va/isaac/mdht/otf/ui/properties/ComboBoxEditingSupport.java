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

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;

public abstract class ComboBoxEditingSupport extends OTFTableEditingSupport {
	ComboBoxCellEditor comboBoxEditor = null;
	private String[] itemArray = null;

	public ComboBoxEditingSupport(OTFTableViewer viewer, List<String> itemList) {
		super(viewer);
		itemArray = new String[itemList.size()];
		itemList.toArray(itemArray);
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		comboBoxEditor = new ComboBoxCellEditor(tableViewer.getTable(), itemArray);
		
		return comboBoxEditor;
	}
	
}
