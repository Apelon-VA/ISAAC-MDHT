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

import gov.va.isaac.mdht.otf.refset.RefsetAttributeType;
import gov.va.isaac.mdht.otf.refset.RefsetMember;
import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.ui.dialogs.ConceptListDialog;
import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public abstract class GenericRefexTableViewer extends OTFTableViewer {

	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	
	private List<TableViewerColumn> refexColumns;
	
	// need to keep track of editing support instances because not accessible from TableEditingColumn (which is final)
	private List<OTFTableEditingSupport> refexColumnEditors;
	
	private PrimitiveInputDialog primitiveInputDialog;
	
    public GenericRefexTableViewer(Table table)  {
        super(table);
    }
    
    protected abstract IWorkbenchPart getActivePart();

    protected String[] getColumnTitles() {
        String[] titles = { "Refers To", "Value", "Component 1", "Component 2", "Component 3" };
        return titles;
    }

    protected int[] getColumnWidths() {
        int[] widths = { 200, 100, 150, 150, 150 };
        return widths;
    }

    protected RefsetAttributeType[] getColumnTypes() {
    	RefsetAttributeType[] memberKinds = { RefsetAttributeType.Component, RefsetAttributeType.String, RefsetAttributeType.Concept, RefsetAttributeType.Concept, RefsetAttributeType.Concept };
        return memberKinds;
    }

	protected Object getPrimitiveValue(final RefsetAttributeType memberKind) {
		if (primitiveInputDialog == null) {
			primitiveInputDialog = new PrimitiveInputDialog(
					getActivePart().getSite().getShell(), "Extension Value",  "", "", null);
		}
		
		primitiveInputDialog.setValueKind(memberKind);
		primitiveInputDialog.setValueString("");

		Object value = null;
		if (primitiveInputDialog.open() == Window.OK) {
			value = primitiveInputDialog.getValue();
		}
		
		return value;
	}
    
	protected ComponentVersionBI getMemberComponent(RefsetAttributeType memberKind) {
		ComponentVersionBI component = null;
		
		if (memberKind == RefsetAttributeType.Concept) {
			ConceptListDialog searchDialog = new ConceptListDialog(getActivePart().getSite().getShell());
			int result = searchDialog.open();
			if (Dialog.OK == result && searchDialog.getResult().length == 1) {
				component = (ConceptVersionBI) searchDialog.getResult()[0];
			}
		}
		else {
			// prompt for component UUID
			InputDialog inputDialog = new InputDialog(
					getActivePart().getSite().getShell(), "Component UUID", "Enter " + memberKind.name() + " UUID", "", null);
			if (inputDialog.open() == Window.OK) {
				String uuidString = inputDialog.getValue();
				if (uuidString != null && uuidString.length() > 0) {
					component = queryService.getComponent(uuidString);
				}
			}
		}
		
		if (component != null) {
			if ((RefsetAttributeType.Concept == memberKind && !(component instanceof ConceptVersionBI))
					|| (RefsetAttributeType.Description == memberKind && !(component instanceof DescriptionVersionBI))
					|| (RefsetAttributeType.Relationship == memberKind && !(component instanceof RelationshipVersionBI))
					|| (RefsetAttributeType.RefsetMember == memberKind && !(component instanceof RefexVersionBI))) {
				
				component = null;
				MessageDialog.open(IStatus.ERROR, getActivePart().getSite().getShell(), "Invalid Member", "Member must be a " + memberKind.toString(), SWT.NONE);
			}
		}
		
		return component;
	}
	
	protected ComponentVersionBI getFirstColumnComponent(RefexVersionBI<?> refex) {
		ComponentVersionBI component = null;
		int nid = refex.getReferencedComponentNid();
		if (nid != 0) {
			component = queryService.getComponent(nid);
		}
		return component;
	}

	protected ComponentVersionBI getFirstColumnComponent(RefsetMember refex) {
		return refex.getReferencedComponent();
	}

	@Override
	protected ILabelProvider createLabelProvider() {
		return new ComponentLabelProvider(true) {
			@Override
			public String getColumnText(Object obj, int columnIndex) {
				Object element = unwrap(obj);
				
				try {
					if (element instanceof RefexVersionBI) {
						RefexVersionBI<?> refex = (RefexVersionBI<?>) element;
			
						switch (columnIndex) {
							case 0: {
								return getText(getFirstColumnComponent(refex));
							}
							case 1: {
								if (refex instanceof RefexStringAnalogBI<?>) {
									RefexStringAnalogBI<?> stringRefex = (RefexStringAnalogBI<?>) refex;
									return stringRefex.getString1();
								}
								else if (refex instanceof RefexBooleanAnalogBI<?>) {
									RefexBooleanAnalogBI<?> booleanRefex = (RefexBooleanAnalogBI<?>) refex;
									return new Boolean(booleanRefex.getBoolean1()).toString();
								}
								else if (refex instanceof RefexIntAnalogBI<?>) {
									RefexIntAnalogBI<?> intRefex = (RefexIntAnalogBI<?>) refex;
									return new Integer(intRefex.getInt1()).toString();
								}
								else if (refex instanceof RefexLongAnalogBI<?>) {
									RefexLongAnalogBI<?> longRefex = (RefexLongAnalogBI<?>) refex;
									return new Long(longRefex.getLong1()).toString();
								}
								else if (refex instanceof RefexFloatVersionBI<?>) {
									RefexFloatVersionBI<?> floatRefex = (RefexFloatVersionBI<?>) refex;
									return new Float(floatRefex.getFloat1()).toString();
								}
								else {
									return "";
								}
							}
							case 2: {
								if (refex instanceof RefexNidVersionBI<?>) {
									RefexNidVersionBI<?> nidRefex = (RefexNidVersionBI<?>) refex;
									if(nidRefex.getNid1() != 0) {
										return getText(queryService.getComponent(nidRefex.getNid1()));
									}
								}
								return null;
							}
							case 3: {
								if (refex instanceof RefexNidNidVersionBI<?>) {
									RefexNidNidVersionBI<?> nidRefex = (RefexNidNidVersionBI<?>) refex;
									if(nidRefex.getNid2() != 0) {
										return getText(queryService.getComponent(nidRefex.getNid2()));
									}
								}
								return null;
							}
							case 4: {
								if (refex instanceof RefexNidNidNidVersionBI<?>) {
									RefexNidNidNidVersionBI<?> nidRefex = (RefexNidNidNidVersionBI<?>) refex;
									if(nidRefex.getNid3() != 0) {
										return getText(queryService.getComponent(nidRefex.getNid3()));
									}
								}
								return null;
							}
							default:
								return null;
						}
					}
					else if (element instanceof RefsetMember) {
						RefsetMember refsetMember = (RefsetMember) element;
			
						switch (columnIndex) {
							case 0: {
								return getText(getFirstColumnComponent(refsetMember));
							}
							case 1: {
								return getText(refsetMember.getExtensionValue());
							}
							case 2: {
								return getText(refsetMember.getExtensionComponents()[0]);
							}
							case 3: {
								return getText(refsetMember.getExtensionComponents()[1]);
							}
							case 4: {
								return getText(refsetMember.getExtensionComponents()[2]);
							}
							default:
								return null;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
		};
	}
	
	protected void updateColumns() {
        final String[] titles = getColumnTitles();
        final RefsetAttributeType[] columnTypes = getColumnTypes();
		
        int index = 0;
        for (TableViewerColumn refexColumn : refexColumns) {
			refexColumn.getColumn().setText(titles[index++]);
		}
        index = 0;
        for (OTFTableEditingSupport editingSupport : refexColumnEditors) {
        	editingSupport.setAttributeType(columnTypes[index++]);
		}
	}

	@Override
    protected void createColumns() {
		// this is called by superclass before subclass fields are initialized
		refexColumns = new ArrayList<TableViewerColumn>();
		refexColumnEditors = new ArrayList<OTFTableEditingSupport>();
		
        final String[] titles = getColumnTitles();
        final int[] bounds = getColumnWidths();
        final RefsetAttributeType[] columnTypes = getColumnTypes();
        
        for (int i = 0; i < titles.length; i++) {
			final RefsetAttributeType columnType = columnTypes[i];
			final int columnIndex = i;
			
            OTFTableEditingSupport refexEditor = new OTFTableEditingSupport(this, columnType) {
            	private CellEditor cellEditor = null;
            	
    			@Override
    			protected String getOperationLabel() {
    				return "Set refex field";
    			}

    			@Override
    			protected CellEditor getCellEditor(final Object element) {
    				if (RefsetMember.isPrimitiveType(attributeType)) {
	    				cellEditor = new DialogCellEditor(tableViewer.getTable()) {
	    					@Override
	    					protected Object openDialogBox(Control cellEditorWindow) {
	    						Object value = getPrimitiveValue(attributeType);
	    						
	    						//change the column type if set in the dialog
	    						attributeType = primitiveInputDialog.getValueKind();
	    						
	    						return value;
	    					}
	    				};
    					
    				}
    				else {
	    				cellEditor = new DialogCellEditor(tableViewer.getTable()) {
	    					@Override
	    					protected Object openDialogBox(Control cellEditorWindow) {
	    						ComponentVersionBI component = getMemberComponent(attributeType);
	    						return component;
	    					}
	    				};
    				}
    				
    				return cellEditor;
    			}

    			@Override
    			protected IStatus doSetValue(Object element, Object value) {
					RefsetMember refsetMember = (RefsetMember) element;

					if (columnIndex == 0) {
	    				Object dialogValue = cellEditor.getValue();
	    				if (dialogValue instanceof ComponentVersionBI) {
	    					ComponentVersionBI memberComponent = (ComponentVersionBI) dialogValue;
	    					refsetMember.setReferencedComponent(memberComponent);
							refresh();
	    				}
					}
					else if (columnIndex == 1 && RefsetMember.isPrimitiveType(attributeType)) {
						Object cellValue = cellEditor.getValue();
    					if (!cellValue.equals(refsetMember.getExtensionValue())) {
    						try {
    							refsetMember.setExtensionValue(cellValue);
    						} catch (NumberFormatException e) {
    							MessageDialog.open(MessageDialog.ERROR, getActivePart().getSite().getShell(), "Invalid Value", 
    									"Value must be type: " + attributeType.name(), SWT.NONE);
    						}
    					}
    				}
    				else if (columnIndex > 1) {
	    				Object dialogValue = cellEditor.getValue();
	    				if (dialogValue instanceof ComponentVersionBI) {
	    					ComponentVersionBI memberComponent = (ComponentVersionBI) dialogValue;
	    					refsetMember.getExtensionComponents()[columnIndex - 2] = memberComponent;
							refresh();
	    				}
    				}

    				return Status.OK_STATUS;
    			}

    			@Override
    			protected Object getValue(Object element) {
					RefsetMember refex = (RefsetMember) element;

					if (columnIndex == 0) {
						return getFirstColumnComponent(refex);
					}
					else if (columnIndex == 1 && RefsetMember.isPrimitiveType(attributeType)) {
						if (refex.getExtensionValue() != null) {
							return refex.getExtensionValue().toString();
						}
						else {
							return "";
						}
    				}
    				else if (columnIndex > 1) {
    					return refex.getExtensionComponents()[columnIndex - 2];
    				}
					
					return null;
    			}
            };

            TableViewerColumn refexColumn = createTableViewerColumn(titles[columnIndex], bounds[columnIndex], columnIndex);
            refexColumns.add(columnIndex, refexColumn);
            
            refexColumn.setEditingSupport(refexEditor);
            refexColumnEditors.add(columnIndex, refexEditor);
            
        }
    }
}
