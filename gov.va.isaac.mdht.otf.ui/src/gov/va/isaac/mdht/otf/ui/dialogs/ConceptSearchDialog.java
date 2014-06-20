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
package gov.va.isaac.mdht.otf.ui.dialogs;

import gov.va.isaac.mdht.otf.search.DescriptionSearch;
import gov.va.isaac.mdht.otf.search.IdentifierSearch;
import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.ui.internal.Activator;
import gov.va.isaac.mdht.otf.ui.providers.DescriptionComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;


/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class ConceptSearchDialog extends Dialog {
	protected ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();

	private List<ConceptVersionBI> parentConceptList = null;
	
	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE= 5;

	private List<String> searchTextHistory;

	private Combo searchParentField;
	
	private ConceptVersionBI parentConcept = null;
	
	private List<ConceptVersionBI> conceptResults = new ArrayList<ConceptVersionBI>();

	private Combo searchTextField;
	
	private Text integerIdField;

	private Text uuidField;

	private Button searchButton;
	
	private Rectangle fDialogPositionInit;

	private IDialogSettings fDialogSettings;
	
	/**
	 * Creates a new dialog with the given shell as parent.
	 * @param parentShell the parent shell
	 */
	public ConceptSearchDialog(Shell parentShell) {
		super(parentShell);

		fDialogPositionInit= null;
		searchTextHistory= new ArrayList<String>(HISTORY_SIZE - 1);

		readConfiguration();

//		setShellStyle(getShellStyle() ^ SWT.APPLICATION_MODAL | SWT.MODELESS);
		setBlockOnOpen(true);
	}
	
	public ConceptVersionBI getParentConcept() {
		return parentConcept;
	}
	
	public void setParentConcept(ConceptVersionBI parent) {
		parentConcept = parent;

		List<ConceptVersionBI> parents = getTopLevelConcepts();
		if (parentConcept != null && !parents.contains(parentConcept)) {
			parents.add(parentConcept);
		}

		if (searchParentField != null) {
			fillConceptList(searchParentField, parents);
			updateParentSelection();
		}
	}
	
	public List<ConceptVersionBI> getResults() {
		return conceptResults;
	}
	
	private void updateParentSelection() {
		if (searchParentField != null) {
			if (parentConcept == null) {
				searchParentField.select(0);
			}
			else {
				searchParentField.select(parentConceptList.indexOf(parentConcept) + 1);
			}
		}
	}

	private List<ConceptVersionBI> getTopLevelConcepts() {
		if (parentConceptList == null) {
			parentConceptList = new ArrayList<ConceptVersionBI>();
			
			UUID sctRootConceptUUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
			ConceptVersionBI sctRootConcept = queryService.getConcept(sctRootConceptUUID);
			if (sctRootConcept != null) {
				try {
					for (ConceptVersionBI child : sctRootConcept.getRelationshipsIncomingOriginsActiveIsa()) {
						parentConceptList.add(child);
					}
				} catch (IOException | ContradictionException e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error getting concept children", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
			
			// sort by label
			Collections.sort(parentConceptList, new DescriptionComparator());
		}
		
		return parentConceptList;
	}
	
	private void fillConceptList(Combo comboBox, List<ConceptVersionBI> concepts) {
		String[] items = new String[concepts.size() + 1];
		items[0] = "";
		
		try {
			int itemIndex = 1;
			for (ConceptVersionBI concept : concepts) {
				items[itemIndex++] = concept.getPreferredDescription().getText();
			}
			
		} catch (IOException | ContradictionException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error getting concept description", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
		
		comboBox.setItems(items);
	}

	protected boolean isResizable() {
		return true;
	}

	/**
	 * Returns <code>true</code> if control can be used.
	 *
	 * @param control the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}

	/*
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();

		Shell shell= getShell();

		// fill in combo contents
		updateCombo(searchTextField, searchTextHistory);

		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);

		shell.setText("Search Concepts");
		// shell.setImage(null);
	}

	/*
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite descriptionGroup = createDescriptionGroup(panel);
		setGridData(descriptionGroup, SWT.FILL, true, SWT.TOP, false);

		Composite identifierGroup = createIdentifierGroup(panel);
		setGridData(identifierGroup, SWT.FILL, true, SWT.TOP, false);

		Composite buttonPanelB = createButtonSection(panel);
		setGridData(buttonPanelB, SWT.FILL, true, SWT.BOTTOM, false);

		updateButtonState();

		applyDialogFont(panel);

		return panel;
	}

	/**
	 * Create a panel to search by description.
	 *
	 * @param parent the parent composite
	 * @return the description input panel
	 */
	private Composite createDescriptionGroup(Composite parent) {
		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText("Search by Description");
		GridLayout groupLayout= new GridLayout();
		groupLayout.numColumns= 2;
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateFieldsEnabled();
				updateButtonState();
			}
		};

		Label parentLabel= new Label(group, SWT.LEFT);
		parentLabel.setText("Parent");
		setGridData(parentLabel, SWT.LEFT, false, SWT.CENTER, false);

		searchParentField= new Combo(group, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		setGridData(searchParentField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(searchParentField);
		fillConceptList(searchParentField, getTopLevelConcepts());
		updateParentSelection();
		
		searchParentField.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = searchParentField.getSelectionIndex();
				if (idx == 0) {
					parentConcept = null;
				}
				else {
					parentConcept = parentConceptList.get(idx-1);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Label textLabel= new Label(group, SWT.LEFT);
		textLabel.setText("Text");
		setGridData(textLabel, SWT.LEFT, false, SWT.CENTER, false);

		searchTextField= new Combo(group, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(searchTextField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(searchTextField);
		searchTextField.addModifyListener(listener);

		return panel;
	}

	/**
	 * Create a panel to search by integer ID or UUID.
	 *
	 * @param parent the parent composite
	 * @return the ID input panel
	 */
	private Composite createIdentifierGroup(Composite parent) {

		Composite panel= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		Group group= new Group(panel, SWT.SHADOW_ETCHED_IN);
		group.setText("Search by ID");
		GridLayout groupLayout= new GridLayout();
		groupLayout.numColumns= 2;
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateFieldsEnabled();
				updateButtonState();
			}
		};

		Label intIdLabel= new Label(group, SWT.LEFT);
		intIdLabel.setText("Concept ID");
		setGridData(intIdLabel, SWT.LEFT, false, SWT.CENTER, false);

		integerIdField = new Text(group, SWT.BORDER);
		setGridData(integerIdField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(integerIdField);
		integerIdField.addModifyListener(listener);

		Label uuidLabel= new Label(group, SWT.LEFT);
		uuidLabel.setText("UUID");
		setGridData(uuidLabel, SWT.LEFT, false, SWT.CENTER, false);

		uuidField = new Text(group, SWT.BORDER);
		setGridData(uuidField, SWT.FILL, true, SWT.CENTER, false);
		addDecorationMargin(uuidField);
		uuidField.addModifyListener(listener);

		return panel;
	}

	/**
	 * Create Search and Close buttons.
	 *
	 * @param parent the parent composite
	 * @return the button panel
	 */
	private Composite createButtonSection(Composite parent) {
		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		searchButton= makeButton(panel, "Search", 102, true, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performSearch();
				updateFindHistory();
				close();
			}
		});
		setGridData(searchButton, SWT.LEFT, false, SWT.BOTTOM, false);

		Button closeButton = createButton(panel, 101, "Close", false);
		setGridData(closeButton, SWT.RIGHT, false, SWT.BOTTOM, false);

		return panel;
	}

	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
	}


	/**
	 * Returns the dialog's boundaries.
	 * @return the dialog's boundaries
	 */
	private Rectangle getDialogBoundaries() {
		if (okToUse(getShell()))
			return getShell().getBounds();
		return fDialogPositionInit;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point initialSize= super.getInitialSize();
		Point minSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (initialSize.x < minSize.x || initialSize.y < minSize.y)
			return minSize;
		return initialSize;
	}

	/**
	 * Returns the dialog's history.
	 * @return the dialog's history
	 */
	private List<String> getFindHistory() {
		return searchTextHistory;
	}
	/**
	 * Retrieves the string to search for from the appropriate text input field and returns it.
	 * @return the search string
	 */
	private String getFindString() {
		if (okToUse(searchTextField)) {
			return searchTextField.getText();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		handleDialogClose();
		return super.close();
	}

	/**
	 * Removes focus changed listener from browser and stores settings for re-open.
	 */
	private void handleDialogClose() {
		// store current settings in case of re-open
		storeSettings();

	}

	/**
	 * Stores the current state in the dialog settings.
	 */
	private void storeSettings() {
		fDialogPositionInit= getDialogBoundaries();

		writeConfiguration();
	}

	/**
	 * Creates a button.
	 * @param parent the parent control
	 * @param label the button label
	 * @param id the button id
	 * @param dfltButton is this button the default button
	 * @param listener a button pressed listener
	 * @return the new button
	 */
	private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
		Button button= createButton(parent, id, label, dfltButton);
		button.addSelectionListener(listener);
		return button;
	}
	
	private void performSearch() {
		if (searchTextField.getText().length() > 0) {
			performDescriptionSearch();
		}
		else if (integerIdField.getText().length() > 0) {
			performIntegerSearch();
		}
		else if (uuidField.getText().length() > 0) {
			performUuidSearch();
		}
	}

	/**
	 * Search for concept(s) using description.
	 */
	private void performDescriptionSearch() {
		String matchText = searchTextField.getText();
		if (matchText != null && matchText.length() > 0) {
			DescriptionSearch search = new DescriptionSearch();
			
			try {
				Query query = null;
				if (parentConcept != null) {
					query = search.getActiveDescriptionQuery(matchText, parentConcept);
				}
				else {
					query = search.getActiveDescriptionQuery(matchText);
				}
				
				conceptResults = search.getQueryResultConcepts(query);

			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in Query Services", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
	}

	/**
	 * Search for concept(s) using integer annotation.
	 */
	private void performIntegerSearch() {
		String idValue = integerIdField.getText();
		if (idValue != null && idValue.length() > 0) {
			IdentifierSearch search = new IdentifierSearch();
			
			try {
				Query query = search.getQueryForSnomedId(idValue);
				
				conceptResults = search.getQueryResultConcepts(query);

			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in Query Services", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
	}

	/**
	 * Search for concept(s) using uuid.
	 */
	private void performUuidSearch() {
		String uuidValue = uuidField.getText();
		if (uuidValue != null && uuidValue.length() > 0) {
			IdentifierSearch search = new IdentifierSearch();
			
			try {
				ConceptVersionBI concept = search.getConceptFromUUID(uuidValue);
				
				if (concept != null) {
					conceptResults = new ArrayList<ConceptVersionBI>();
					conceptResults.add(concept);
				}

			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error in Query Services", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
	}

	/**
	 * Attaches the given layout specification to the <code>component</code>.
	 *
	 * @param component the component
	 * @param horizontalAlignment horizontal alignment
	 * @param grabExcessHorizontalSpace grab excess horizontal space
	 * @param verticalAlignment vertical alignment
	 * @param grabExcessVerticalSpace grab excess vertical space
	 */
	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd;
		if (component instanceof Button && (((Button)component).getStyle() & SWT.PUSH) != 0) {
//			SWTUtil.setButtonDimensionHint((Button)component);
			gd= (GridData)component.getLayoutData();
		} else {
			gd= new GridData();
			component.setLayoutData(gd);
			gd.horizontalAlignment= horizontalAlignment;
			gd.grabExcessHorizontalSpace= grabExcessHorizontalSpace;
		}
		gd.verticalAlignment= verticalAlignment;
		gd.grabExcessVerticalSpace= grabExcessVerticalSpace;
	}

	/**
	 * Adds enough space in the control's layout data margin for the content assist
	 * decoration.
	 * @param control the control that needs a margin
	 */
	private void addDecorationMargin(Control control) {
		Object layoutData= control.getLayoutData();
		if (!(layoutData instanceof GridData))
			return;
		GridData gd= (GridData)layoutData;
		FieldDecoration dec= FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		gd.horizontalIndent= dec.getImage().getBounds().width;
	}

	/**
	 * Updates the enabled state of the fields.
	 */
	private void updateFieldsEnabled() {
		if (searchTextField.getText().length() > 0) {
			integerIdField.setEnabled(false);
			uuidField.setEnabled(false);
		}
		else if (integerIdField.getText().length() > 0) {
			searchTextField.setEnabled(false);
			uuidField.setEnabled(false);
		}
		else if (uuidField.getText().length() > 0) {
			searchTextField.setEnabled(false);
			integerIdField.setEnabled(false);
		}
		else {
			searchTextField.setEnabled(true);
			integerIdField.setEnabled(true);
			uuidField.setEnabled(true);
		}
	}

	/**
	 * Updates the enabled state of the buttons.
	 */
	private void updateButtonState() {
//		if (okToUse(getShell()) && okToUse(searchButton)) {
//
//			String str= getFindString();
//			boolean hasSearchText = str != null && str.length() > 0;
//
//			searchButton.setEnabled(hasSearchText);
//		}
		
		boolean enabled = searchTextField.getText().length() > 0
				|| integerIdField.getText().length() > 0
				|| uuidField.getText().length() > 0;
		searchButton.setEnabled(enabled);
	}

	/**
	 * Updates the given combo with the given content.
	 * @param combo combo to be updated
	 * @param content to be put into the combo
	 */
	private void updateCombo(Combo combo, List<String> content) {
		combo.removeAll();
		for (int i= 0; i < content.size(); i++) {
			combo.add(content.get(i));
		}
	}

	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		if (okToUse(searchTextField)) {
//			searchTextField.removeModifyListener(fFindModifyListener);
			updateHistory(searchTextField, searchTextHistory);
//			searchTextField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Updates the combo with the history.
	 * @param combo to be updated
	 * @param history to be put into the combo
	 */
	private void updateHistory(Combo combo, List<String> history) {
		String findString= combo.getText();
		int index= history.indexOf(findString);
		if (index != 0) {
			if (index != -1) {
				history.remove(index);
			}
			history.add(0, findString);
			Point selection= combo.getSelection();
			updateCombo(combo, history);
			combo.setText(findString);
			combo.setSelection(selection);
		}
	}

	//--------------- configuration handling --------------

	/**
	 * Returns the dialog settings object used to share state
	 * between several find/replace dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings= Activator.getDefault().getDialogSettings();
		fDialogSettings= settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings= Activator.getDefault().getDialogSettings();
		IDialogSettings section= settings.getSection(sectionName);
		if (section == null)
			section= settings.addNewSection(sectionName);
		return section;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
	 */
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
	}

	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();

//		fWrapInit= s.get("wrap") == null || s.getBoolean("wrap"); //$NON-NLS-1$ //$NON-NLS-2$
//		fCaseInit= s.getBoolean("casesensitive"); //$NON-NLS-1$
//		fWholeWordInit= s.getBoolean("wholeword"); //$NON-NLS-1$
//		fIncrementalInit= s.getBoolean("incremental"); //$NON-NLS-1$
//		fIsRegExInit= s.getBoolean("isRegEx"); //$NON-NLS-1$

		String[] findHistory= s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistory != null) {
			List<String> history= getFindHistory();
			history.clear();
			for (int i= 0; i < findHistory.length; i++)
				history.add(findHistory[i]);
		}

	}

	/**
	 * Stores its current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();

//		s.put("wrap", fWrapInit); //$NON-NLS-1$
//		s.put("casesensitive", fCaseInit); //$NON-NLS-1$
//		s.put("wholeword", fWholeWordInit); //$NON-NLS-1$
//		s.put("incremental", fIncrementalInit); //$NON-NLS-1$
//		s.put("isRegEx", fIsRegExInit); //$NON-NLS-1$

		List<String> history= getFindHistory();
		String findString= getFindString();
		if (findString.length() > 0)
			history.add(0, findString);
		writeHistory(history, s, "findhistory"); //$NON-NLS-1$
	}

	/**
	 * Writes the given history into the given dialog store.
	 *
	 * @param history the history
	 * @param settings the dialog settings
	 * @param sectionName the section name
	 * @since 3.2
	 */
	private void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
		int itemCount= history.size();
		Set<String> distinctItems= new HashSet<String>(itemCount);
		for (int i= 0; i < itemCount; i++) {
			String item = (String)history.get(i);
			if (distinctItems.contains(item)) {
				history.remove(i--);
				itemCount--;
			} else {
				distinctItems.add(item);
			}
		}

		while (history.size() > 8)
			history.remove(8);

		String[] names= new String[history.size()];
		history.toArray(names);
		settings.put(sectionName, names);

	}
}
