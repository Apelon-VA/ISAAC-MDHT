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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.datastore.BdbTermBuilder;
import org.ihtsdo.otf.tcc.datastore.BdbTerminologyStore;
import org.osgi.framework.Bundle;

public class AppBdbTerminologyStore {
    public static final String BDB_LOCATION_PROPERTY = "org.ihtsdo.otf.tcc.datastore.bdb-location";
    
    public static final String TEST_DATA_BUNDLE = "gov.va.isaac.otf.test-data";
    public static final String BDB_FOLDER = "berkeley-db";
    
    // root concepts
	public static final String SNOMED_CT_CONCEPT_UUID = "ee9ac5d2-a07c-3981-a57a-f7f26baf38d8";
	public static final String LOINC_UUID = "3958d043-9e8c-508e-bf6d-fd9c83a856da";
	public static final String REFSET_AUXILLIARY_UUID = "1c698388-c309-3dfa-96f0-86248753fac5";
	
	// other referenced concepts
	public static final String REFSET_IDENTITY_UUID = "3e0cd740-2cc6-3d68-ace7-bad2eb2621da";
	
	public static AppBdbTerminologyStore INSTANCE = new AppBdbTerminologyStore();
	private TerminologyStoreDI store = null;
	private TerminologyBuilderBI builder = null;
	
	private ViewCoordinate snomedStatedLatest = null;
	private EditCoordinate editCoordinate = null;
    private int snomedAssemblageNid = 0;
	
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
	
	protected void setBbdSystemProperty() {
		String bdbPath = getUserHomePath();
		
		if (bdbPath == null) {
			bdbPath = getBundlePath();
		}
		
		if (bdbPath != null) {
			System.setProperty(BDB_LOCATION_PROPERTY, bdbPath);
		}
	}

	protected String getUserHomePath() {
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
	
	private EditCoordinate getEditCoordinate() throws ValidationException, IOException {
		if (editCoordinate == null) {
	        int authorNid   = TermAux.USER.getLenient().getConceptNid();
	        int module = Snomed.CORE_MODULE.getLenient().getNid();
	        int editPathNid = TermAux.SNOMED_CORE.getLenient().getConceptNid();
	
	        editCoordinate =  new EditCoordinate(authorNid, module, editPathNid);
		}
		
		return editCoordinate;
	}
	
	public void shutdown() {
		if (store != null) {
			store.shutdown();
		}
	}

	public TerminologyStoreDI getStore() {
		// TODO run as background job.  does this need to be in UI plug-in?
		if (store == null) {
			setBbdSystemProperty();
			store = new BdbTerminologyStore();
		}
		
		return store;
	}
	
	public ViewCoordinate getSnomedStatedLatest() throws IOException {
		if (snomedStatedLatest == null) {
			// store must be initialized
			getStore();
			snomedStatedLatest = StandardViewCoordinates.getSnomedStatedLatest();
		}
		return snomedStatedLatest;
	}
	
	public int getSnomedAssemblageNid() {
		if (snomedAssemblageNid == 0) {
			try {
				// store must be initialized
				getStore();
				snomedAssemblageNid = TermAux.SNOMED_IDENTIFIER.getNid();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
		return snomedAssemblageNid;
	}

	public TerminologyBuilderBI getBuilder() {
		if (builder == null) {
			try {
				builder = new BdbTermBuilder(getEditCoordinate(), getSnomedStatedLatest());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return builder;
	}

	public List<ConceptVersionBI> getRootConcepts() {
		List<ConceptVersionBI> rootConcepts = new ArrayList<ConceptVersionBI>();

		ConceptVersionBI concept = getConcept(UUID.fromString(SNOMED_CT_CONCEPT_UUID));
		if (concept != null) {
			rootConcepts.add(concept);
		}
		//TODO this returns an empty concept.  For any unknown UUID????
//		concept = getConcept(UUID.fromString(LOINC_UUID));
//		if (concept != null) {
//			rootConcepts.add(concept);
//		}
		concept = getConcept(UUID.fromString(REFSET_AUXILLIARY_UUID));
		if (concept != null) {
			rootConcepts.add(concept);
		}
		
		return rootConcepts;
	}

	private ConceptVersionBI getConcept(UUID uuid) {
		ConceptVersionBI conceptVersion = null;
		
		try {
			ViewCoordinate vc = getSnomedStatedLatest();
			
			ConceptChronicleBI conceptChronicle = getStore().getConcept(uuid);
			conceptVersion = conceptChronicle.getVersion(vc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return conceptVersion;
	}

}
