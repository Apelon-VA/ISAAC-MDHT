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
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

/**
 * The properties section for Refset members.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class RefsetSpecificationSection extends RefsetMemberSection {

	private boolean isAnnotationStyle = false;

	private Button annotationStyleButton = null;
	
	private Button conceptKindButton = null;
	private Button descriptionKindButton = null;
	private Button relationshipKindButton = null;

	private CCombo valueKindButton = null;
	
	@Override
	protected void updateContentState(ConceptVersionBI concept) {
		if (!annotationStyleButton.isDisposed()) {
			try {
				annotationStyleButton.setSelection(conceptVersion.isAnnotationStyleRefex());
			} catch (IOException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get refset annotation style", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
	}

	@Override
	protected void updateContentState(Collection<? extends RefexVersionBI<?>> members) {
		// use first member to determine refset member kind for all members
		if (members.size() > 0) {
			RefexVersionBI<?> member = (RefexVersionBI<?>) members.iterator().next();
			ComponentVersionBI component = queryService.getComponent(member.getReferencedComponentNid());
			if (component instanceof ConceptVersionBI) {
				referencedComponentKind = RefsetAttributeType.Concept;
				conceptKindButton.setSelection(true);
				descriptionKindButton.setSelection(false);
				relationshipKindButton.setSelection(false);
			}
			else if (component instanceof DescriptionVersionBI) {
				referencedComponentKind = RefsetAttributeType.Description;
				descriptionKindButton.setSelection(true);
				conceptKindButton.setSelection(false);
				relationshipKindButton.setSelection(false);
			}
			else if (component instanceof RelationshipVersionBI) {
				referencedComponentKind = RefsetAttributeType.Relationship;
				relationshipKindButton.setSelection(true);
				conceptKindButton.setSelection(false);
				descriptionKindButton.setSelection(false);
			}

			conceptKindButton.setEnabled(false);
			descriptionKindButton.setEnabled(false);
			relationshipKindButton.setEnabled(false);
			
			RefsetAttributeType attrType = RefsetMember.getPrimitiveType(member);
			if (attrType != null) {
				valueKind = attrType;
			}
			else {
				valueKind = RefsetAttributeType.String;
			}
			valueKindButton.setText(valueKind.name());
			refexViewer.getColumnTypes()[1] = valueKind;
			refexViewer.getColumnTitles()[1] = valueKind.name();
			refexViewer.updateColumns();
			
		}
		else {
			conceptKindButton.setEnabled(true);
			descriptionKindButton.setEnabled(true);
			relationshipKindButton.setEnabled(true);
		}
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
		
		conceptKindButton = getWidgetFactory().createButton(optionsComposite, RefsetAttributeType.Concept.toString(), SWT.RADIO);
		conceptKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				if (conceptKindButton.getSelection()) {
					referencedComponentKind = RefsetAttributeType.Concept;
				}
			}
		});
		
		descriptionKindButton = getWidgetFactory().createButton(optionsComposite, RefsetAttributeType.Description.toString(), SWT.RADIO);
		descriptionKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				if (descriptionKindButton.getSelection()) {
					referencedComponentKind = RefsetAttributeType.Description;
				}
			}
		});
		
		relationshipKindButton = getWidgetFactory().createButton(optionsComposite, RefsetAttributeType.Relationship.toString(), SWT.RADIO);
		relationshipKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				if (relationshipKindButton.getSelection()) {
					referencedComponentKind = RefsetAttributeType.Relationship;
				}
			}
		});

		valueKindButton = getWidgetFactory().createCCombo(optionsComposite, SWT.BORDER | SWT.READ_ONLY);
		for (RefsetAttributeType attrType : RefsetMember.getPrimitiveTypes()) {
			valueKindButton.add(attrType.name());
		}
		valueKindButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				widgetDefaultSelected(event);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				RefsetAttributeType attributeType = null;
				try {
					String selectedKind = valueKindButton.getText();
					attributeType = RefsetAttributeType.valueOf(selectedKind);
				}
				catch (Exception e) {
					// should not occur for selected type
				}
				if (attributeType != null) {
					valueKind = attributeType;
					refexViewer.getColumnTypes()[1] = attributeType;
					refexViewer.getColumnTitles()[1] = attributeType.name();
					refexViewer.updateColumns();
				}
			}
		});

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		annotationStyleButton.setLayoutData(data);
		
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

		Label valueTypeLabel = getWidgetFactory().createLabel(optionsComposite, "Value Type: ");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(conceptKindButton, 0);
		valueTypeLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(valueTypeLabel, 0);
		data.top = new FormAttachment(conceptKindButton, 0);
		valueKindButton.setLayoutData(data);

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
		
		getWidgetFactory().createLabel(definitionComposite, "Not implemented, coming soon!!!");
		
		/*
		 * Members tab
		 */
		Composite membersComposite = createMembersComposite(tabFolder);
		
		membersTab.setControl(membersComposite);
		
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(optionsComposite, 0);
		membersComposite.setLayoutData(data);
	}

}
