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


import gov.va.isaac.mdht.otf.services.ConceptBuilderService;
import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.dialogs.ConceptListDialog;
import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.providers.DescriptionComparator;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * The properties section for concept relationships.
 * 
 * @author dcarlson
 */
public class RelationshipSection extends AbstractPropertySection {
	
	public static UUID CONCEPT_MODEL_ATTRIBUTE_UUID = UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99");

	private TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	private ConceptBuilderService builderService = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
	
	protected ConceptVersionBI conceptVersion;
	
	private boolean dirty = false;
	
	private List<RelationshipCAB> newRelationships = new ArrayList<RelationshipCAB>();

	private List<ConceptVersionBI> allConceptModelAttributes = null;
	
	private ConceptVersionBI defaultType = null;

	private ConceptVersionBI defaultDestination = null;

	private OTFTableViewer relationshipViewer = null;

	private Button addButton = null;

	private Button removeButton = null;

	private Button editButton = null;

	protected Button saveButton = null;
	
	private void buildAndCommit() {
		try {
			dirty = false;
			
			// build chronicle for any blueprints
			List<RelationshipCAB> newRelationshipsCopy = new ArrayList<RelationshipCAB>(newRelationships);
			for (RelationshipCAB relationship : newRelationshipsCopy) {
				relationship.recomputeUuid();
				builderService.construct(relationship);
				newRelationships.remove(relationship);
			}
			
			// commit enclosing concept
			storeService.addUncommitted(conceptVersion);
			
		} catch (IOException | ContradictionException | InvalidCAB | NoSuchAlgorithmException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot commit relationship(s)", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private ConceptVersionBI getTargetConcept() {
		ConceptVersionBI concept = null;
		
		ConceptListDialog searchDialog = new ConceptListDialog(getPart().getSite().getShell(),
				"Search Concepts", "Enter match string for target concept");
		int result = searchDialog.open();
		if (Dialog.OK == result && searchDialog.getResult().length == 1) {
			concept = (ConceptVersionBI) searchDialog.getResult()[0];
		}
		
		return concept;
	}
	
	private void addRelationship() {
		try {
			ConceptVersionBI target = getTargetConcept();
			if (target != null) {
				RelationshipCAB relationship = builderService.createRelationship(conceptVersion, target, getDefaultType());
				newRelationships.add(relationship);
			}
			
		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot add relationship", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private void retireRelationship(final RelationshipVersionBI<?> relationship) {
		try {
			RelationshipCAB blueprint = relationship.makeBlueprint(
					storeService.getSnomedStatedLatest(),
					IdDirective.PRESERVE,
					RefexDirective.INCLUDE);
			
			blueprint.setRetired();
			builderService.construct(blueprint);
			
			dirty = true;

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot retire relationship", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private void editRelationship(final RelationshipVersionBI<?> relationship) {
		try {
//			if (relationship.isUncommitted()) {
//				storeService.forget(relationship);
//			}
//			else {
//				retireRelationship(relationship);
//			}
			retireRelationship(relationship);

			RelationshipCAB modified = builderService.modifyRelationship(relationship);
			newRelationships.add(modified);

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot edit relationship", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	protected ConceptVersionBI getDefaultType() {
		if (defaultType == null) {
			try {
				UUID relTypeUUID = Snomed.ASSOCIATED_WITH.getLenient().getPrimordialUuid();
				defaultType = queryService.getConcept(relTypeUUID);
			} catch (IOException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get default relationship type", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		return defaultType;
	}

	protected ConceptVersionBI getDefaultDestination() {
		if (defaultDestination == null) {
			try {
				UUID destinationUUID = Snomed.CLINICAL_FINDING.getLenient().getPrimordialUuid();
				defaultDestination = queryService.getConcept(destinationUUID);
			} catch (IOException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get default relationship destination", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		return defaultDestination;
	}
	
	private List<ConceptVersionBI> getAllRelationshipTypes() {
		if (allConceptModelAttributes == null) {
			allConceptModelAttributes = new ArrayList<ConceptVersionBI>();
			
			ConceptVersionBI conceptModelAttribute = queryService.getConcept(CONCEPT_MODEL_ATTRIBUTE_UUID);
			allConceptModelAttributes = queryService.getAllChildren(conceptModelAttribute);
			
			// sort by label
			Collections.sort(allConceptModelAttributes, new DescriptionComparator());
		}
		
		return allConceptModelAttributes;
	}

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		Composite composite = getWidgetFactory().createGroup(parent, "Outgoing Relationships");
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		composite.setLayout(layout);

		addButton = getWidgetFactory().createButton(composite, null, SWT.PUSH);
		Image addImage = Activator.getDefault().getBundledImage("icons/eview16/add.gif");
		addButton.setImage(addImage);
		addButton.setToolTipText("Add relationship");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addRelationship();
				relationshipViewer.refresh();
			}
		});

		removeButton = getWidgetFactory().createButton(composite, null, SWT.PUSH);
		Image removeImage = Activator.getDefault().getBundledImage("icons/eview16/delete.gif");
		removeButton.setImage(removeImage);
		removeButton.setToolTipText("Retire selected Relationship(s)");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = relationshipViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof RelationshipVersionBI) {
							retireRelationship((RelationshipVersionBI<?>)selected);
						}
						else if (selected instanceof RelationshipCAB) {
							newRelationships.remove((RelationshipCAB)selected);
						}
						relationshipViewer.refresh();
					}
				}
			}
		});

		editButton = getWidgetFactory().createButton(composite, null, SWT.PUSH);
		Image editImage = Activator.getDefault().getBundledImage("icons/eview16/write.gif");
		editButton.setImage(editImage);
		editButton.setToolTipText("Edit selected relationship");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = relationshipViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof RelationshipVersionBI) {
							editRelationship((RelationshipVersionBI<?>)selected);
						}
					}
					relationshipViewer.refresh();
				}
			}
		});

		saveButton = getWidgetFactory().createButton(composite, null, SWT.PUSH);
		Image saveImage = Activator.getDefault().getBundledImage("icons/eview16/save.gif");
		saveButton.setImage(saveImage);
		saveButton.setToolTipText("Save all changes");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buildAndCommit();
				relationshipViewer.refresh();
			}
		});

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		addButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(addButton, 0);
		removeButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(removeButton, 0);
		editButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(editButton, 0);
		saveButton.setLayoutData(data);
		
		Table table = getWidgetFactory().createTable(composite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		data = new FormData();
		data.left = new FormAttachment(addButton, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		table.setLayoutData(data);

		relationshipViewer = new OTFTableViewer(table) {
			@Override
			protected void fireSelectionChanged(SelectionChangedEvent event) {
				super.fireSelectionChanged(event);
				
				boolean enabled = !event.getSelection().isEmpty();
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();	
				removeButton.setEnabled(enabled);
				editButton.setEnabled(enabled && !(selection instanceof CreateOrAmendBlueprint));
			}
			
			@Override
			protected IContentProvider createContentProvider() {
		        return new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) {
						try {
							List<Object> relationships = new ArrayList<Object>();
							relationships.addAll(conceptVersion.getRelationshipsOutgoingActive());
							relationships.addAll(newRelationships);
							saveButton.setEnabled(!newRelationships.isEmpty());
							return relationships.toArray();
							
						} catch (Exception e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get concept relationships", e), 
									StatusManager.SHOW | StatusManager.LOG);
							return new Object[0];
						}
					}
					
					public void dispose() { }
					
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
						if (!removeButton.isDisposed()) {
							removeButton.setEnabled(false);
							editButton.setEnabled(false);
						}

						if (!saveButton.isDisposed()) {
							saveButton.setEnabled(!newRelationships.isEmpty());
						}
					}
		        	
		        };
			}

			@Override
		    protected void createColumns() {
		        String[] titles = { "Type", "Destination", "Stated" };
		        int[] bounds = { 200, 300, 100 };
		
		        TableViewerColumn typeColumn = createTableViewerColumn(titles[0], bounds[0], 0);
		        TableViewerColumn destinationColumn = createTableViewerColumn(titles[1], bounds[1], 1);
		        TableViewerColumn statedColumn = createTableViewerColumn(titles[2], bounds[2], 2);
		        
		        typeColumn.setEditingSupport(new ConceptComboBoxEditingSupport(this, getAllRelationshipTypes()) {
					@Override
					protected String getOperationLabel() {
						return "Set relationship type";
					}

					@Override
					protected CellEditor getCellEditor(Object element) {
						// refresh list if attributes have changed
						allConceptModelAttributes = null;
						setConceptList(getAllRelationshipTypes());
						
						return super.getCellEditor(element);
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						RelationshipCAB relationship = (RelationshipCAB) element;
						
						ConceptVersionBI typeConcept = getSelectedConcept();
						if (typeConcept != null) {
							try {
								RelationshipCAB editedRelationship = new RelationshipCAB(relationship.getSourceNid(), typeConcept.getConceptNid(), 
										relationship.getTargetNid(), relationship.getGroup(), builderService.getRelationshipType(relationship), IdDirective.GENERATE_RANDOM);
								newRelationships.remove(relationship);
								newRelationships.add(editedRelationship);
								relationshipViewer.refresh();
								
							} catch (IOException | InvalidCAB | ContradictionException e) {
								StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot set relationship type", e), 
										StatusManager.SHOW | StatusManager.LOG);
							}
						}

						return Status.OK_STATUS;
					}

					@Override
					protected Object getValue(Object element) {
						// must return Integer index into comboBox list
						RelationshipCAB relationship = (RelationshipCAB) element;
						int index = 0;
						try {
							ConceptVersionBI typeConcept = queryService.getConcept(relationship.getTypeNid());
							if (typeConcept != null) {
								index = getAllRelationshipTypes().indexOf(typeConcept);
							}
						} catch (IOException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get relationship type", e), 
									StatusManager.SHOW | StatusManager.LOG);
						}
						return new Integer(index);
					}
		        });
		        
		        destinationColumn.setEditingSupport(new OTFTableEditingSupport(this) {
		        	private DialogCellEditor dialogCellEditor = null;
		        	
					@Override
					protected String getOperationLabel() {
						return "Set relationship destination";
					}

					@Override
					protected CellEditor getCellEditor(final Object element) {
						dialogCellEditor = new DialogCellEditor(tableViewer.getTable()) {
							@Override
							protected Object openDialogBox(Control cellEditorWindow) {
								ConceptVersionBI concept = getTargetConcept();

								return concept;
							}
						};
						
						return dialogCellEditor;
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						Object dialogValue = dialogCellEditor.getValue();
						if (dialogValue instanceof ConceptVersionBI && element instanceof RelationshipCAB) {
							RelationshipCAB relationship = (RelationshipCAB) element;
							ConceptVersionBI targetConcept = (ConceptVersionBI) dialogValue;

							try {
								RelationshipCAB editedRelationship = new RelationshipCAB(relationship.getSourceNid(), relationship.getTypeNid(), 
										targetConcept.getConceptNid(), relationship.getGroup(), builderService.getRelationshipType(relationship), IdDirective.GENERATE_HASH);
								newRelationships.remove(relationship);
								newRelationships.add(editedRelationship);
								relationshipViewer.refresh();
								
							} catch (IOException | InvalidCAB | ContradictionException e) {
								StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot set relationship destination", e), 
										StatusManager.SHOW | StatusManager.LOG);
							}
						}

						return Status.OK_STATUS;
					}

					@Override
					protected Object getValue(Object element) {
						RelationshipCAB relationship = (RelationshipCAB) element;
						try {
							ConceptVersionBI typeConcept = queryService.getConcept(relationship.getTargetNid());
							if (typeConcept != null) {
									return typeConcept.getPreferredDescription().getText();
							}
						} catch (IOException | ContradictionException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get relationship destination", e), 
									StatusManager.SHOW | StatusManager.LOG);
						}
						return null;
					}
		        });

		        statedColumn.setEditingSupport(new BooleanEditingSupport(this) {
		        	@Override
		        	protected boolean canEdit(Object element) {
		        		return false;
		        	}

					@Override
					protected String getOperationLabel() {
						return null;
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						return null;
					}

					@Override
					protected Object getValue(Object element) {
						return null;
					}
		        });
		    }
		};

		//TODO sort by: language
//		relationshipViewer.setSorter(new DescriptionSorter());

	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		
		if (dirty || newRelationships.size() > 0) {
			buildAndCommit();
		}
		
		conceptVersion = null;
		newRelationships = new ArrayList<RelationshipCAB>();
		
		Object selected = ((IStructuredSelection)selection).getFirstElement();
		if (selected instanceof ConceptVersionBI) {
			conceptVersion = (ConceptVersionBI) selected;
		}
		if (selected instanceof IAdaptable) {
			Object adapted = (ComponentVersionBI) ((IAdaptable)selected).getAdapter(ComponentVersionBI.class);
			if (adapted instanceof ConceptVersionBI) {
				conceptVersion = (ConceptVersionBI) adapted;
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();

	}

	@Override
	public void refresh() {
		relationshipViewer.setInput(conceptVersion);
	}

}
