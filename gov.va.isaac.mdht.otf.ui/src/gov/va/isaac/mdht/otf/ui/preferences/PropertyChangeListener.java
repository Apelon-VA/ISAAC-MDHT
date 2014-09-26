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

import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.util.UUID;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Respond to changed preference values.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class PropertyChangeListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
		
		if (PreferenceConstants.MODULE_UUID.equals(event.getProperty())) {
			String uuidString = event.getNewValue().toString();
			try {
				UUID moduleUUID = UUID.fromString(uuidString);
				storeService.setEditModule(moduleUUID);
			}
			catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid Module UUID: " + uuidString, e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		else if (PreferenceConstants.USER_UUID.equals(event.getProperty())) {
			String uuidString = event.getNewValue().toString();
			try {
				UUID userUUID = UUID.fromString(uuidString);
				storeService.setEditUser(userUUID);
			}
			catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid User UUID: " + uuidString, e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
		else if (PreferenceConstants.PATH_UUID.equals(event.getProperty())) {
			String uuidString = event.getNewValue().toString();
			try {
				UUID pathUUID = UUID.fromString(uuidString);
				storeService.setEditPath(pathUUID);
			}
			catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid Path UUID: " + uuidString, e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}
	}

}
