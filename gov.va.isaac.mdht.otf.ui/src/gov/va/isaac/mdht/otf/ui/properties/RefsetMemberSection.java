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
import gov.va.isaac.mdht.otf.ui.dialogs.ConceptSearchDialog;
import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * The properties section for Refset members.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class RefsetMemberSection extends AbstractPropertySection {

	private TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	private ConceptBuilderService builderService = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
	
	private LabelProvider labelProvider = new ComponentLabelProvider();
	
	protected ConceptVersionBI conceptVersion;
	
	private boolean dirty = false;
	
	private List<RefexCAB> newMembers = new ArrayList<RefexCAB>();
	
	private OTFTableViewer refexViewer = null;
	
	private boolean isAnnotationStyle = false;

	private Button annotationStyleButton = null;
	
	private enum MemberKinds {
		Concept, Description, Relationship
	};
	
	private MemberKinds memberKind = MemberKinds.Concept;

	private Button conceptKindButton = null;
	private Button descriptionKindButton = null;
	private Button relationshipKindButton = null;
	
	private Button addButton = null;

	private Button removeButton = null;

	private Button editButton = null;

	private void buildAndCommit() {
		try {
			dirty = false;
			
			// build chronicle for any blueprints
			for (RefexCAB refex : newMembers) {
				refex.recomputeUuid();
				builderService.construct(refex);
			}
			
			// commit enclosing concept
			storeService.addUncommitted(conceptVersion);
			
		} catch (IOException | ContradictionException | InvalidCAB e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot commit refset member(s)", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	private ComponentVersionBI getMemberComponent() {
		ComponentVersionBI component = null;
		
		if (memberKind == MemberKinds.Concept) {
			ConceptSearchDialog searchDialog = new ConceptSearchDialog(getPart().getSite().getShell());
			int result = searchDialog.open();
			if (Dialog.OK == result && searchDialog.getResult().length == 1) {
				component = (ConceptVersionBI) searchDialog.getResult()[0];
			}
		}
		
		if (component == null) {
			// prompt for component UUID
			InputDialog inputDialog = new InputDialog(
				getPart().getSite().getShell(), "Component UUID", "Enter member " + memberKind.toString() + " UUID", "", null);
			if (inputDialog.open() == Window.OK) {
				String uuidString = inputDialog.getValue();
				if (uuidString != null && uuidString.length() > 0) {
					component = queryService.getComponent(uuidString);
				}
			}
		}
		
		if (component != null) {
			if ((MemberKinds.Concept == memberKind && !(component instanceof ConceptVersionBI))
					|| (MemberKinds.Description == memberKind && !(component instanceof DescriptionVersionBI))
					|| (MemberKinds.Relationship == memberKind && !(component instanceof RelationshipVersionBI))) {
				
				component = null;
				MessageDialog.open(IStatus.ERROR, getPart().getSite().getShell(), "Invalid Member", "Member must be a " + memberKind.toString(), SWT.NONE);
			}
		}
		
		return component;
	}
	
	private void addMember() {
		try {
			ComponentVersionBI component = getMemberComponent();
			
			if (component != null) {
				RefexCAB refex = builderService.createRefex(conceptVersion, component);
				newMembers.add(refex);
			}
			
		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot add refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private void retireMember(final RefexVersionBI<?> refex) {
		try {
			RefexCAB blueprint = refex.makeBlueprint(storeService.getSnomedStatedLatest(),
					IdDirective.PRESERVE, RefexDirective.INCLUDE);

			// TODO for now, required workaround to eliminate NPE
			if (blueprint.getMemberUUID() == null) {
				blueprint.setMemberUuid(refex.getPrimordialUuid());
			}
			
			blueprint.setRetired();
			builderService.construct(blueprint);
			
			dirty = true;

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot retire refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}

	private void editMember(final RefexVersionBI<?> refex) {
		try {
//			if (relationship.isUncommitted()) {
//				storeService.forget(relationship);
//			}
//			else {
//				retireRelationship(relationship);
//			}
			retireMember(refex);

			RefexCAB modified = builderService.modifyRefex(refex);
			newMembers.add(modified);

		} catch (Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot edit refset member", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
	}
	
	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		
		CTabFolder tabFolder = getWidgetFactory().createTabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new GridLayout());
		
		CTabItem optionsTab = getWidgetFactory().createTabItem(tabFolder, SWT.NONE);
		optionsTab.setText("Options");
		CTabItem definitionTab = getWidgetFactory().createTabItem(tabFolder, SWT.NONE);
		definitionTab.setText("Definition");
		CTabItem membersTab = getWidgetFactory().createTabItem(tabFolder, SWT.NONE);
		membersTab.setText("Members");
		
		/*
		 * Options tab
		 */
		Composite optionsComposite = new Composite(tabFolder, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		optionsComposite.setLayout(layout);
		optionsTab.setControl(optionsComposite);
		optionsComposite.setBackground(getWidgetFactory().getColors().getBackground());

		annotationStyleButton = getWidgetFactory().createButton(optionsComposite, "Is Annotation Style", SWT.CHECK);
		annotationStyleButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				isAnnotationStyle = annotationStyleButton.getSelection();
				try {
					if (isAnnotationStyle != conceptVersion.isAnnotationStyleRefex()) {
						conceptVersion.setAnnotationStyleRefex(isAnnotationStyle);
					}
				} catch (IOException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error setting concept annotation style", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
		});
		
		conceptKindButton = getWidgetFactory().createButton(optionsComposite, MemberKinds.Concept.toString(), SWT.RADIO);
		conceptKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				if (conceptKindButton.getSelection()) {
					memberKind = MemberKinds.Concept;
				}
			}
		});
		
		descriptionKindButton = getWidgetFactory().createButton(optionsComposite, MemberKinds.Description.toString(), SWT.RADIO);
		descriptionKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				if (descriptionKindButton.getSelection()) {
					memberKind = MemberKinds.Description;
				}
			}
		});
		
		relationshipKindButton = getWidgetFactory().createButton(optionsComposite, MemberKinds.Relationship.toString(), SWT.RADIO);
		relationshipKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				if (relationshipKindButton.getSelection()) {
					memberKind = MemberKinds.Relationship;
				}
			}
		});

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		annotationStyleButton.setLayoutData(data);
		
		//TODO until tested to work
		annotationStyleButton.setEnabled(false);
		
		Label memberKindLabel = getWidgetFactory().createLabel(optionsComposite, "Member Kind: ");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(annotationStyleButton, 0);
		memberKindLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(memberKindLabel, 0);
		data.top = new FormAttachment(annotationStyleButton, 0);
		conceptKindButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(conceptKindButton, 0);
		data.top = new FormAttachment(annotationStyleButton, 0);
		descriptionKindButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(descriptionKindButton, 0);
		data.top = new FormAttachment(annotationStyleButton, 0);
		relationshipKindButton.setLayoutData(data);

		/*
		 * Definition tab
		 */
		Composite definitionComposite = new Composite(tabFolder, SWT.NONE);
		layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		definitionComposite.setLayout(layout);
		definitionTab.setControl(definitionComposite);
		definitionComposite.setBackground(getWidgetFactory().getColors().getBackground());
		
		Label todoLabel = getWidgetFactory().createLabel(definitionComposite, "Not implemented, coming soon!!!");
		
		/*
		 * Members tab
		 */
		Composite membersComposite = new Composite(tabFolder, SWT.NONE);
		layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		membersComposite.setLayout(layout);
		membersTab.setControl(membersComposite);
		
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(optionsComposite, 0);
		membersComposite.setLayoutData(data);

		addButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image addImage = Activator.getDefault().getBundledImage("icons/eview16/add.gif");
		addButton.setImage(addImage);
		addButton.setToolTipText("Add refset member");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				addMember();
				refexViewer.refresh();
			}
		});

		removeButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image removeImage = Activator.getDefault().getBundledImage("icons/eview16/remove.gif");
		removeButton.setImage(removeImage);
		removeButton.setToolTipText("Retire selected member(s)");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = refexViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof RefexVersionBI) {
							retireMember((RefexVersionBI<?>)selected);
						}
						else if (selected instanceof RefexCAB) {
							newMembers.remove((RefexCAB)selected);
						}
						refexViewer.refresh();
					}
				}
			}
		});

		editButton = getWidgetFactory().createButton(membersComposite, null, SWT.PUSH);
		Image editImage = Activator.getDefault().getBundledImage("icons/eview16/write.gif");
		editButton.setImage(editImage);
		editButton.setToolTipText("Edit selected refset member");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ISelection selection = refexViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object selected : ((IStructuredSelection)selection).toList()) {
						if (selected instanceof RefexVersionBI) {
							editMember((RefexVersionBI<?>)selected);
						}
					}
					refexViewer.refresh();
				}
			}
		});

		data = new FormData();
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
		
		Table table = getWidgetFactory().createTable(membersComposite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		data = new FormData();
		data.left = new FormAttachment(addButton, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		table.setLayoutData(data);

		refexViewer = new OTFTableViewer(table) {
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
							List<Object> members = new ArrayList<Object>();
							
							members.addAll(conceptVersion.getRefsetMembersActive());
							// use first member to determine refset member kind for all members
							if (members.size() > 0) {
								RefexVersionBI<?> member = (RefexVersionBI<?>) members.get(0);
								ComponentVersionBI component = queryService.getComponent(member.getReferencedComponentNid());
								if (component instanceof ConceptVersionBI) {
									memberKind = MemberKinds.Concept;
									conceptKindButton.setSelection(true);
									descriptionKindButton.setSelection(false);
									relationshipKindButton.setSelection(false);
								}
								else if (component instanceof DescriptionVersionBI) {
									memberKind = MemberKinds.Description;
									descriptionKindButton.setSelection(true);
									conceptKindButton.setSelection(false);
									relationshipKindButton.setSelection(false);
								}
								else if (component instanceof RelationshipVersionBI) {
									memberKind = MemberKinds.Relationship;
									relationshipKindButton.setSelection(true);
									conceptKindButton.setSelection(false);
									descriptionKindButton.setSelection(false);
								}

								conceptKindButton.setEnabled(false);
								descriptionKindButton.setEnabled(false);
								relationshipKindButton.setEnabled(false);
							}
							else {
								conceptKindButton.setEnabled(true);
								descriptionKindButton.setEnabled(true);
								relationshipKindButton.setEnabled(true);
							}
							
							members.addAll(newMembers);
							
							return members.toArray();
							
						} catch (Exception e) {
							StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get refset members", e), 
									StatusManager.SHOW | StatusManager.LOG);
							return new Object[0];
						}
					}
					
					public void dispose() { }
					
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
						if (!annotationStyleButton.isDisposed()) {
							try {
								annotationStyleButton.setSelection(conceptVersion.isAnnotationStyleRefex());
							} catch (IOException e) {
								StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get refset annotation style", e), 
										StatusManager.SHOW | StatusManager.LOG);
							}
						}
						if (!removeButton.isDisposed()) {
							removeButton.setEnabled(false);
							editButton.setEnabled(false);
						}
					}
		        	
		        };
			}

			@Override
		    protected void createColumns() {
		        String[] titles = { "Component" };
		        int[] bounds = { 500 };
		
		        TableViewerColumn memberColumn = createTableViewerColumn(titles[0], bounds[0], 0);
		        
		        memberColumn.setEditingSupport(new OTFTableEditingSupport(this) {
		        	private DialogCellEditor dialogCellEditor = null;
		        	
					@Override
					protected String getOperationLabel() {
						return "Set refset component";
					}

					@Override
					protected CellEditor getCellEditor(final Object element) {
						dialogCellEditor = new DialogCellEditor(tableViewer.getTable()) {
							@Override
							protected Object openDialogBox(Control cellEditorWindow) {
								ComponentVersionBI component = getMemberComponent();
								return component;
							}
						};
						
						return dialogCellEditor;
					}

					@Override
					protected IStatus doSetValue(Object element, Object value) {
						Object dialogValue = dialogCellEditor.getValue();
						if (dialogValue instanceof ComponentVersionBI && element instanceof RefexCAB) {
							RefexCAB refex = (RefexCAB) element;
							ComponentVersionBI referencedComponent = (ComponentVersionBI) dialogValue;

							try {
								RefexCAB editedRefex = new RefexCAB(RefexType.MEMBER, referencedComponent.getPrimordialUuid(), refex.getRefexCollectionUuid(), 
										IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
								newMembers.remove(refex);
								newMembers.add(editedRefex);
								
								refexViewer.refresh();
								
							} catch (IOException | InvalidCAB | ContradictionException e) {
								StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot set refset member value", e), 
										StatusManager.SHOW | StatusManager.LOG);
							}
						}

						return Status.OK_STATUS;
					}

					@Override
					protected Object getValue(Object element) {
						RefexCAB refex = (RefexCAB) element;
						ComponentVersionBI referencedComponent = queryService.getComponent(refex.getReferencedComponentUuid());
						if (referencedComponent != null) {
							return labelProvider.getText(referencedComponent);
						}

						return null;
					}
		        });
		    }
		};

	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		
		if (dirty || newMembers.size() > 0) {
			buildAndCommit();
		}
		
		conceptVersion = null;
		newMembers = new ArrayList<RefexCAB>();
		
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
		refexViewer.setInput(conceptVersion);
	}

}
