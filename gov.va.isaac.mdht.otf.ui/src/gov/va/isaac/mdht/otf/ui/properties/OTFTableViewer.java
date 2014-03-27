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

import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;
import gov.va.isaac.mdht.otf.ui.providers.ConceptContentProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;

public abstract class OTFTableViewer extends TableViewer {
		private IContentProvider contentProvider = null;
		private ILabelProvider labelProvider = null;
		
	    public OTFTableViewer(Table table)  {
	        super(table);

	        createColumns();
	        table.setHeaderVisible(true);
	        table.setLinesVisible(true);

	        contentProvider = createContentProvider();
	        labelProvider = createLabelProvider();
	        setContentProvider(contentProvider);
	        setLabelProvider(labelProvider);
	    }

		public IContentProvider getContentProvider() {
			if (contentProvider == null) {
				contentProvider = createContentProvider();
			}
			return contentProvider;
		}

		protected IContentProvider createContentProvider() {
			return new ConceptContentProvider();
		}

		public ILabelProvider getLabelProvider() {
			if (labelProvider == null) {
				labelProvider = createLabelProvider();
			}
			return labelProvider;
		}

		protected ILabelProvider createLabelProvider() {
			return new ComponentLabelProvider();
		}

	    protected abstract void createColumns();
	
	    protected TableViewerColumn createTableViewerColumn(String header, int width, int idx)  {
	        TableViewerColumn column = new TableViewerColumn(this, SWT.LEFT, idx);
	        column.getColumn().setText(header);
	        column.getColumn().setWidth(width);
	        column.getColumn().setResizable(true);
	        column.getColumn().setMoveable(true);
	
	        return column;
	    }
	    
	    public List<ComponentBI> getSelectedComponents() {
	    	List<ComponentBI> selectedComponents = new ArrayList<ComponentBI>();
			ISelection selection = getSelection();
			if (selection instanceof IStructuredSelection) {
				for (Object selected : ((IStructuredSelection)selection).toList()) {
					if (selected instanceof ComponentBI) {
						selectedComponents.add((ComponentBI)selected);
					}
				}
			}
	    	
	    	return selectedComponents;
	    }
	}