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
package gov.va.isaac.mdht.otf.services;

import gov.va.isaac.mdht.otf.internal.store.AppBdbTerminologyStore;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;

public class TerminologyStoreService {
	
	private AppBdbTerminologyStore appTermStore;
	
	public TerminologyStoreService(AppBdbTerminologyStore appDb) {
		this.appTermStore = appDb;
	}

	public void setEditModule(UUID moduleUUID) throws IOException {
		appTermStore.setEditModule(moduleUUID);
	}

	public void setEditUser(UUID userUUID) throws IOException {
		appTermStore.setEditUser(userUUID);
	}

	public void setEditPath(UUID pathUUID) throws IOException {
		appTermStore.setEditPath(pathUUID);
	}
	
	public TerminologyStoreDI getTerminologyStore() {
		return appTermStore.getStore();
	}

	public ViewCoordinate getViewCoordinate() throws IOException {
		return appTermStore.getViewCoordinate();
	}
	
	public List<ConceptVersionBI> getRootConcepts() {
		return appTermStore.getRootConcepts();
	}

    public Collection<? extends ConceptChronicleBI> getUncommittedConcepts() {
    	return appTermStore.getStore().getUncommittedConcepts();
    }

	public void addUncommitted(ConceptVersionBI concept) throws IOException {
		appTermStore.getStore().addUncommitted(concept);
	}

	public void addUncommitted(ConceptChronicleBI concept) throws IOException {
		appTermStore.getStore().addUncommitted(concept);
	}

	public void commitAll() throws IOException {
		appTermStore.getStore().commit();
	}

	public void commit(ConceptVersionBI concept) throws IOException {
		appTermStore.getStore().commit(concept);
	}

	public void commit(ConceptChronicleBI concept) throws IOException {
		appTermStore.getStore().commit(concept);
	}

	public void forget(ConceptVersionBI concept) throws IOException {
		appTermStore.getStore().forget(concept.getChronicle());
	}

	public void forget(DescriptionVersionBI<?> description) throws IOException {
		appTermStore.getStore().forget(description);
	}

	public void forget(RelationshipVersionBI<?> relationship) throws IOException {
		appTermStore.getStore().forget(relationship);
	}

	public void forget(RefexChronicleBI<?> refex) throws IOException {
		appTermStore.getStore().forget(refex);
	}
	
	public void index() throws IOException {
		TerminologyStoreDI store = appTermStore.getStore();
		
		System.out.println("indexing BDB.....");
		store.index();
		System.out.println("finished indexing BDB.");

	}

}
