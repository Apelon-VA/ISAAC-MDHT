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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

/**
 * Class used to initialize default preference values.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String BDB_FOLDER = "berkeley-db";
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.DATABASE_PATH, getUserHomePath());
		
		// module root is SNOMED CT 'Module' concept, allowed modules are all of its leaf children
		store.setDefault(PreferenceConstants.MODULE_ROOT_UUID, "40d1c869-b509-32f8-b735-836eac577a67");
		
		store.setDefault(PreferenceConstants.MODULE_UUID, Snomed.CORE_MODULE.getUuids()[0].toString());
		store.setDefault(PreferenceConstants.USER_UUID, TermAux.USER.getUuids()[0].toString());
		
//		store.setDefault(PreferenceConstants.PATH_UUID, TermAux.SNOMED_CORE.getUuids()[0].toString());
		// ISAAC_DEV_PATH
		store.setDefault(PreferenceConstants.PATH_UUID, "f5c0a264-15af-5b94-a964-bb912ea5634f");
		
		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		
		store.addPropertyChangeListener(new PropertyChangeListener());
	}

	private String getUserHomePath() {
		StringBuffer bdbPath = new StringBuffer();
		bdbPath.append(System.getProperty("user.home"));
		bdbPath.append("/");
		bdbPath.append(BDB_FOLDER);
		
		return bdbPath.toString();
	}
	
}
