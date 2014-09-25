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
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;

/**
 * The properties section for concept descriptions.
 * 
 * @author dcarlson
 */
public class DescriptionSection extends AbstractPropertySection {

	private TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	private ConceptBuilderService builderService = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
	
	protected ConceptVersionBI conceptVersion;
	
	private List<DescriptionCAB> newDescriptions = new ArrayList<DescriptionCAB>();

	private List<String> allLanguageCodes = null;
	
	private boolean dirty = false;
	
	private OTFTableViewer descriptionViewer = null;

	private Button addButton = null;

	private Button removeButton = null;

	private Button editButton = null;

	protected Button saveButton = null;
	
	private void buildAndCommit() {
		try {
			dirty = false;
			
			// build chronicle for any blueprints
			List<DescriptionCAB> newDescriptionsCopy = new ArrayList<DescriptionCAB>(newDescriptions);
			for (DescriptionCAB description : newDescriptionsCopy) {
				description.recomputeUuid();
				builderService.construct(description);
				newDescriptions.remove(description);
			}
			
			// commit enclosing concept
			storeService.addUncommitted(conceptVersion);
			
		} catch (IOException | ContradictionException | InvalidCAB | NoSuchAlgorithmException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot commit description(s)", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	private void addDescription() {
		try {
			DescriptionCAB blueprint = builderService.createSynonymDescription(conceptVersion, "New Synonym", LanguageCode.EN_US);
			newDescriptions.add(blueprint);
			
		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot add description", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private void retireDescription(final DescriptionVersionBI<?> description) {
		try {
			DescriptionCAB blueprint = description.makeBlueprint(
					storeService.getViewCoordinate(),
					IdDirective.PRESERVE,
					RefexDirective.INCLUDE);
			
			blueprint.setRetired();
			builderService.construct(blueprint);
			
			dirty = true;

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot retire description", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private void editDescription(final DescriptionVersionBI<?> description) {
		try {
//			if (description.isUncommitted()) {
//				storeService.forget(description);
//			}
//			else {
//				retireDescription(description);
//			}
			retireDescription(description);

			DescriptionCAB modified = builderService.modifyDescription(description);
			newDescriptions.add(modified);

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot edit description", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	private List<String> getAllLanguageCodes() {
		if (allLanguageCodes == null) {
			allLanguageCodes = new ArrayList<String>();
			LanguageCode[] allCodes = LanguageCode.values();
			for (int i = 0; i < allCodes.length; i++) {
				LanguageCode languageCode = allCodes[i];
				//TODO setLanguage() parse requires lower case, why does name() return upper?
				allLanguageCodes.add(languageCode.name().toLowerCase());
			}
			Collections.sort(allLanguageCodes);
		}
		
		return allLanguageCodes;
	}
	
	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

//		Composite composite = getWidgetFactory().createGroup(parent, "Descriptions");
		Composite composite = getWidgetFactory().createComposite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		composite.setLayout(layout);

		addButton = getWidgetFactory().createButton(composite, null, SWT.PUSH);
		Image addImage = Activator.getDefault().getBundledImage("icons/eview16/add.gif");
		addButton.setImage(addImage);
		addButton.setToolTipText("Add description");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addDescription();
				descriptionViewer.refresh();
			}
		});

		removeButton = getWidgetFactory().createButton(composite, null, SWT.PUSH);
		Image removeImage = Activator.getDefault().getBundledImage("icons/eview16/delete.gif");
		removeButton.setImage(removeImage);
		removeButton.setToolTipText("Retire selected description(s)");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = descriptionViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof DescriptionVersionBI) {
							retireDescription((DescriptionVersionBI<?>)selected);
						}
						else if (selected instanceof DescriptionCAB) {
							newDescriptions.remove((DescriptionCAB)selected);
						}
						descriptionViewer.refresh();
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
				ISelection selection = descriptionViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof DescriptionVersionBI) {
							editDescription((DescriptionVersionBI<?>)selected);
						}
					}
					descriptionViewer.refresh();
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
				descriptionViewer.refresh();
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
		
//		final Composite tableComposite = getWidgetFactory().createComposite(composite, SWT.NONE);
//		final ScrolledComposite tableComposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
//		tableComposite.setExpandHorizontal(true);
//		tableComposite.setExpandVertical(true);
		
//		data = new FormData();
//		data.left = new FormAttachment(addButton, 0);
//		data.right = new FormAttachment(100, 0);
//		data.top = new FormAttachment(0, 0);
//		data.bottom = new FormAttachment(100, 0);
//		tableComposite.setLayoutData(data);
		
		final Table table = getWidgetFactory().createTable(composite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		data = new FormData();
		data.left = new FormAttachment(addButton, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		table.setLayoutData(data);

//		tableComposite.setContent(table);
//		tableComposite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

//		tableComposite.addControlListener(
//				new ControlAdapter() {
//					public void controlResized(ControlEvent e) {
//						tableComposite.layout();
//						table.setSize(tableComposite.getSize());
//					}
//				});

		descriptionViewer = new OTFTableViewer(table) {
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
							List<Object> descriptions = new ArrayList<Object>();
							descriptions.addAll(conceptVersion.getDescriptionsActive());
							descriptions.addAll(newDescriptions);
							saveButton.setEnabled(!newDescriptions.isEmpty());
							return descriptions.toArray();
							
						} catch (Exception e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get concept descriptions", e), 
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
							saveButton.setEnabled(!newDescriptions.isEmpty());
						}
					}
		        	
		        };
			}

			@Override
		    protected void createColumns() {
		        String[] titles = { "Text", "Language", "Initial Case Sig", "Module" };
		        int[] bounds = { 300, 100, 100, 200 };
		
		        TableViewerColumn textColumn = createTableViewerColumn(titles[0], bounds[0], 0);
		        TableViewerColumn languageColumn = createTableViewerColumn(titles[1], bounds[1], 1);
		        TableViewerColumn initialCaseColumn = createTableViewerColumn(titles[2], bounds[2], 2);
		        TableViewerColumn moduleColumn = createTableViewerColumn(titles[3], bounds[3], 3);
		        
		        textColumn.setEditingSupport(new TextEditingSupport(this) {
					@Override
					protected String getOperationLabel() {
						return "Set description text";
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						DescriptionCAB description = (DescriptionCAB) element;
						description.setText(value.toString());

						return Status.OK_STATUS;
					}

					@Override
					protected Object getValue(Object element) {
						DescriptionCAB description = (DescriptionCAB) element;
						return description.getText();
					}
		        });
		        
		        languageColumn.setEditingSupport(new ComboBoxEditingSupport(this, getAllLanguageCodes()) {
					@Override
					protected String getOperationLabel() {
						return "Set description language";
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						DescriptionCAB description = (DescriptionCAB) element;
						int index = ((Integer)value).intValue();
						try {
							LanguageCode langCode = null;
							try {
								String langString = getAllLanguageCodes().get(index);
								langCode = LanguageCode.getLangCode(langString.toUpperCase());
							} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
								StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid LanguageCode: " + value, e), 
										StatusManager.SHOW | StatusManager.LOG);
							}
							
							if (langCode != null && !langCode.name().equalsIgnoreCase(description.getLang())) {
								DescriptionCAB newDescription = new DescriptionCAB(description.getConceptNid(), description.getTypeNid(), langCode, 
										description.getText(), description.isInitialCaseSignificant(), IdDirective.GENERATE_HASH);
								newDescriptions.remove(description);
								newDescriptions.add(newDescription);
								descriptionViewer.refresh();
							}
							
						} catch (IOException | InvalidCAB | ContradictionException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot set description language", e), 
									StatusManager.SHOW | StatusManager.LOG);
						}

						return Status.OK_STATUS;
					}

					@Override
					protected Object getValue(Object element) {
						DescriptionCAB description = (DescriptionCAB) element;
						
						int index = getAllLanguageCodes().indexOf(description.getLang());
						index = (index == -1) ? 0 : index;
						return new Integer(index);
					}
		        });

		        initialCaseColumn.setEditingSupport(new BooleanEditingSupport(this) {
					@Override
					protected String getOperationLabel() {
						return "Set initial case sig";
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						DescriptionCAB description = (DescriptionCAB) element;
						try {
							LanguageCode langCode = LanguageCode.getLangCode(description.getLang());
							DescriptionCAB newDescription = new DescriptionCAB(description.getConceptNid(), description.getTypeNid(), langCode, 
									description.getText(), (Boolean)value, IdDirective.GENERATE_HASH);
							newDescriptions.remove(description);
							newDescriptions.add(newDescription);
							descriptionViewer.refresh();
							
						} catch (IOException | InvalidCAB | ContradictionException e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot set description initial case", e), 
									StatusManager.SHOW | StatusManager.LOG);
						}

						return Status.OK_STATUS;
					}

					@Override
					protected Object getValue(Object element) {
						DescriptionCAB description = (DescriptionCAB) element;
						return description.isInitialCaseSignificant();
					}
		        });

		    }
		};

		//TODO sort by: language
//		descriptionViewer.setSorter(new DescriptionSorter());

	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		if (dirty || newDescriptions.size() > 0) {
			buildAndCommit();
		}
		
		conceptVersion = null;
		newDescriptions = new ArrayList<DescriptionCAB>();
		
		
		Object selected = ((IStructuredSelection)selection).getFirstElement();
		if (selected instanceof ConceptVersionBI) {
			conceptVersion = (ConceptVersionBI) selected;
		}
		else if (selected instanceof IAdaptable) {
			ComponentVersionBI adapted = (ComponentVersionBI) ((IAdaptable)selected).getAdapter(ComponentVersionBI.class);
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
		if (conceptVersion != null) {
			descriptionViewer.setInput(conceptVersion);
		}
	}

}
