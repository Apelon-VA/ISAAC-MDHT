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
package gov.va.isaac.mdht.otf.internal.store;

import gov.va.isaac.mdht.otf.services.ConceptBuilderService;
import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppBdbTerminologyStore {
	private static final Logger LOG = LoggerFactory.getLogger(AppBdbTerminologyStore.class);

    public static final String BDB_LOCATION_PROPERTY = "org.ihtsdo.otf.tcc.datastore.bdb-location";
    
    public static final String TEST_DATA_BUNDLE = "gov.va.isaac.otf.test-data";
    public static final String BDB_FOLDER = "berkeley-db";
    
    // root concepts
	public static final String ISAAC_ROOT_UUID = "c767a452-41e3-5835-90b7-439f5b738035";
	public static final String SNOMED_CT_CONCEPT_UUID = "ee9ac5d2-a07c-3981-a57a-f7f26baf38d8";
	public static final String LOINC_ROOT_UUID = "3958d043-9e8c-508e-bf6d-fd9c83a856da";

	public static final String MODULE_ROOT_UUID = "40d1c869-b509-32f8-b735-836eac577a67";
	
	//TODO we need to look up paths dynamically in the DB, let the user choose - store the setting / default somewhere in the DB (or user profile?)
	private static ConceptSpec ISAAC_DEV_PATH = new ConceptSpec("ISAAC development path", "f5c0a264-15af-5b94-a964-bb912ea5634f");
	
	public static AppBdbTerminologyStore INSTANCE = new AppBdbTerminologyStore();
	private TerminologyStoreDI store = null;
	private TerminologyBuilderBI builder = null;
	
	private String databasePath = null;
	private ConceptChronicleBI editModule = null;
	private ConceptChronicleBI currentUser = null;
	private ConceptChronicleBI currentEditPath = null;
	
	private ViewCoordinate viewCoordinate = null;
	private EditCoordinate editCoordinate = null;
	
	private AppBdbTerminologyStore() {
		
	}
	
	/**
	 * Get terminology store from the default location.
	 * @return
	 */
	public static AppBdbTerminologyStore getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AppBdbTerminologyStore();
		}
		return INSTANCE;
	}
	
	public String getDatabasePath() {
		if (databasePath != null) {
			return databasePath;
		}

		return getUserHomePath();
	}
	
	public void setDatabasePath(String path) {
		databasePath = path;
	}
	
	protected void setBbdSystemProperty() {
		String bdbPath = getDatabasePath();
		
		if (bdbPath == null) {
			bdbPath = getBundlePath();
		}
		
		if (bdbPath != null) {
			System.setProperty(BDB_LOCATION_PROPERTY, bdbPath);
		}
	}

	private String getUserHomePath() {
		StringBuffer bdbPath = new StringBuffer();
		bdbPath.append(System.getProperty("user.home"));
		bdbPath.append("/");
		bdbPath.append(BDB_FOLDER);
		
		File bdbPathFile = new File(bdbPath.toString());
		if (!bdbPathFile.exists()) {
			System.err.println("Cannot find test data file path: " + bdbPath);
			return null;
		}
		
		return bdbPath.toString();
	}
	
	protected String getBundlePath() {
		Bundle termStoreBundle = Platform.getBundle(TEST_DATA_BUNDLE);
		if (termStoreBundle == null) {
			System.err.println("Cannot find test-data bundle.");
			return null;
		}
		
		URL bundleURL = FileLocator.find(termStoreBundle, new Path(BDB_FOLDER), null);
		URL bundleFileURL = null;
		try {
			bundleFileURL = FileLocator.resolve(bundleURL);
		} catch (IOException e) {
			System.err.println("Cannot find test data file path: " + bundleURL);
			return null;
		}
		
		// strip file:/ prefix, need absolute file system path
		String bdbPath = bundleFileURL.toString().substring(5);
		
		return bdbPath;
	}

	public ConceptChronicleBI getEditModule() throws ValidationException, IOException {
		ConceptChronicleBI moduleConcept = null;
		if (editModule != null) {
			moduleConcept =  editModule;
		}
		else {
			moduleConcept = Snomed.CORE_MODULE.getLenient();
		}
		
		return moduleConcept;
	}

	public ConceptChronicleBI getEditUser() throws ValidationException, IOException {
		ConceptChronicleBI userConcept = null;
		if (currentUser != null) {
			userConcept =  currentUser;
		}
		else {
			userConcept = TermAux.USER.getLenient();
		}
		
		return userConcept;
	}

	public ConceptChronicleBI getEditPath() throws ValidationException, IOException {
		ConceptChronicleBI pathConcept = null;
		if (currentEditPath != null) {
			pathConcept =  currentEditPath;
		}
		else {
			pathConcept = TermAux.SNOMED_CORE.getLenient();

			//If the ISAAC_DEV_PATH concept exists, use it.
			if (getConcept(ISAAC_DEV_PATH.getUuids()[0]).getUUIDs().size() > 0) {
				LOG.info("Using path " + ISAAC_DEV_PATH.getDescription() + " as the Edit Coordinate");
				// Override edit path nid with "ISAAC development path"
				pathConcept = ISAAC_DEV_PATH.getLenient();
			}
		}
		
		return pathConcept;
	}

	public void setEditModule(UUID moduleUUID) throws IOException {
		editModule = getStore().getConcept(moduleUUID);
		editCoordinate = null;
		builder = null;
	}

	public void setEditUser(UUID userUUID) throws IOException {
		currentUser = getStore().getConcept(userUUID);
		editCoordinate = null;
		builder = null;
	}

	public void setEditPath(UUID pathUUID) throws IOException {
		currentEditPath = getStore().getConcept(pathUUID);
		editCoordinate = null;
		builder = null;
	}
	
	private EditCoordinate getEditCoordinate() throws ValidationException, IOException {
		if (editCoordinate == null) {
	        int authorNid   = getEditUser().getNid();
	        int module = getEditModule().getNid();
	        int editPathNid = getEditPath().getNid();
	
	        editCoordinate =  new EditCoordinate(authorNid, module, editPathNid);
		}
		
		return editCoordinate;
	}
	
	public void shutdown() {
		if (store != null) {
			store.shutdown();
		}
	}
	
	public void restart() {
		shutdown();
		getStore();
	}

	public TerminologyStoreDI getStore() {
		// TODO run as background job.  does this need to be in UI plug-in?
		if (store == null) {
			setBbdSystemProperty();
			store = new BdbTerminologyStore();
		}
		
		return store;
	}
	
	/**
	 * Currently configured to return InferredThenStatedLatest + INACTIVE status
	 */
	public ViewCoordinate getViewCoordinate() {
		if (viewCoordinate == null) {
			try {
				// store must be initialized
				getStore();
				viewCoordinate = StandardViewCoordinates.getSnomedStatedLatest();
				
				//If the ISAAC_DEV_PATH concept exists, use it.
				if (getConcept(ISAAC_DEV_PATH.getUuids()[0]).getUUIDs().size() > 0) {
					LOG.info("Using path " + ISAAC_DEV_PATH.getDescription() + " as the View Coordinate");
					// Start with standard view coordinate and override the path setting to use the ISAAC development path
					Position position = getStore().newPosition(getStore().getPath(ISAAC_DEV_PATH.getLenient().getConceptNid()), Long.MAX_VALUE);
					viewCoordinate.setViewPosition(position);
				}
			} catch (IOException e) {
				LOG.error("Unexpected error fetching view coordinates!", e);
			}
		}
		
		return viewCoordinate;
	}

	public TerminologyBuilderBI getBuilder() {
		if (builder == null) {
			try {
				builder = new BdbTermBuilder(getEditCoordinate(), getViewCoordinate());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return builder;
	}

	public List<ConceptVersionBI> getRootConcepts() {
		List<ConceptVersionBI> rootConcepts = new ArrayList<ConceptVersionBI>();

//		ConceptVersionBI concept = getConcept(UUID.fromString(ISAAC_ROOT_UUID));
//		if (concept != null) {
//			rootConcepts.add(concept);
//		}

		ConceptVersionBI concept = getConcept(UUID.fromString(SNOMED_CT_CONCEPT_UUID));
		if (concept != null) {
			rootConcepts.add(concept);
		}

		concept = getConcept(UUID.fromString(LOINC_ROOT_UUID));
		if (concept != null) {
			rootConcepts.add(concept);
		}

		return rootConcepts;
	}

	public ConceptVersionBI getOrCreateRootConcept(String uuid, String name) {
		ConceptQueryService query = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
		ConceptVersionBI rootConcept = null;
		if (uuid != null) {
			rootConcept = query.getConcept(uuid);
		}

		try {
			if (rootConcept == null || rootConcept.getFullySpecifiedDescription() == null) {
				ConceptBuilderService builder = TerminologyStoreFactory.INSTANCE.createConceptBuilderService();
				List<ConceptVersionBI> parents = new ArrayList<ConceptVersionBI>();
				ConceptCB childBlueprint;
					childBlueprint = builder.createConcept(parents, name, name);
					rootConcept = builder.construct(childBlueprint);

					TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
					// TODO bug in this API
//					storeService.commit(fhirConcept.getChronicle());

					storeService.commitAll();
			}
		} catch (IOException | InvalidCAB | ContradictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rootConcept;
	}

	private ConceptVersionBI getConcept(UUID uuid) {
		ConceptVersionBI conceptVersion = null;
		
		try {
			ViewCoordinate vc = getViewCoordinate();
			
			ConceptChronicleBI conceptChronicle = getStore().getConcept(uuid);
			conceptVersion = conceptChronicle.getVersion(vc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return conceptVersion;
	}

}
