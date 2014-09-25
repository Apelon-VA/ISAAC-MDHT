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
package gov.va.isaac.mdht.otf.ui.preferences;

import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.statushandlers.StatusManager;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

/**
 * Preference page for ISAAC terminology editing.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */

public class IsaacPreferencePage extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public IsaacPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for ISAAC terminology editing.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.DATABASE_PATH, 
				"&Database location:", getFieldEditorParent()));
		
		List<ConceptVersionBI> modules = PreferenceUtil.getAllModules();
		String[][] moduleArray = new String[modules.size()][2];
		for (int i=0; i<modules.size(); i++) {
			try {
				moduleArray[i][0] = modules.get(i).getPreferredDescription().getText();
				moduleArray[i][1] = modules.get(i).getPrimordialUuid().toString();
				
			} catch (IOException | ContradictionException e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get module description", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		
		addField(new ComboFieldEditor(PreferenceConstants.MODULE_UUID,
				"Current &module", moduleArray, getFieldEditorParent()));
		
		addField(new StringFieldEditor(PreferenceConstants.USER_UUID, 
				"Current &user", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceConstants.PATH_UUID, 
				"Current &path", getFieldEditorParent()));

//		addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN,
//				"&An example of a boolean preference", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}