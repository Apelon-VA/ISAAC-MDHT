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

import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

/**
 * Utility methods to get preference values.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class PreferenceUtil {

	public static ConceptVersionBI getRootModule() {
		return getConceptFromUUID(PreferenceConstants.MODULE_ROOT_UUID);
	}

	public static ConceptVersionBI getModule() {
		return getConceptFromUUID(PreferenceConstants.MODULE_UUID);
	}

	public static ConceptVersionBI getUser() {
		return getConceptFromUUID(PreferenceConstants.USER_UUID);
	}

	public static ConceptVersionBI getPath() {
		return getConceptFromUUID(PreferenceConstants.PATH_UUID);
	}
	
	public static List<ConceptVersionBI> getAllModules() {
		List<ConceptVersionBI> modules = new ArrayList<ConceptVersionBI>();
		try {
			ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
			List<ConceptVersionBI> allModules = queryService.getAllModules();
			modules.addAll(allModules);
		} catch (IOException | ContradictionException e) {
			// no modules
		}
		
		return modules;
	}

	private static ConceptVersionBI getConceptFromUUID(String preferenceName) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
		
		ConceptVersionBI concept = null;
		String uuid = store.getString(preferenceName);
		if (uuid != null) {
			concept = queryService.getConcept(uuid);
		}
		return concept;
	}
}
