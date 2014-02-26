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

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.time.TimeHelper;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class ConceptPrinterService {
	private AppBdbTerminologyStore appTermStore;
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();

	public ConceptPrinterService(AppBdbTerminologyStore appTermStore) {
		this.appTermStore = appTermStore;
	}

	public void printConcept(ConceptVersionBI concept) {
		try {
			System.out.println();
			System.out.println("**************************************************************");
			System.out.println("***** Concept: " + concept.getFullySpecifiedDescription().getText() + " *****");
			System.out.println("**************************************************************");
			
			printConceptAttributes(concept);

			printDescriptions(concept);
			System.out.println();

			printRelationships(concept);
			
			RefsetPrinter refsetPrinter = new RefsetPrinter();
			refsetPrinter.printMembers(concept);
			
		} catch (IOException | ContradictionException e) {
			e.printStackTrace();
		}		
	}

	private void printConceptAttributes(ConceptVersionBI concept) throws IOException, ContradictionException {
		printBasicIds(concept);

		try {
			ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
			System.out.println("Concept Fully Defined: " + concept.getConceptAttributes().getVersion(vc).isDefined());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		printStamp(concept);

	}

	private void printDescriptions(ConceptChronicleBI concept) throws IOException, ContradictionException {
		Collection<? extends DescriptionChronicleBI> allDesc = concept.getDescriptions();
		ViewCoordinate vc = appTermStore.getSnomedStatedLatest();

		for (DescriptionChronicleBI descAllVersions : allDesc) {
			DescriptionVersionBI<?> desc = descAllVersions.getVersion(vc);
			System.out.println();
			System.out.println("Description: ");

			if (desc != null) {
				printDescription(desc);
			}
			else {
				printAllVersions(descAllVersions);
			}
		}
	}

	public void printAllVersions(DescriptionChronicleBI fullDesc) throws IOException, ContradictionException {
		int i = 1;
		
		System.out.println("*** " + fullDesc.getVersions().size() + " versions: ");
		for (DescriptionVersionBI<?> desc : fullDesc.getVersions()) {
			System.out.println("Version #" + i++);
			printDescription(desc);
		}

	}

	private void printDescription(DescriptionVersionBI<?> desc) throws IOException, ContradictionException {
		String text = desc.getText();
		String type = getDescription(desc.getTypeNid());
		boolean initCap = desc.isInitialCaseSignificant();
		String lang = desc.getLang();

		printBasicIds(desc);
		printStamp(desc);

		System.out.println("Desc Text: " + text);
		System.out.println("Desc Type: " + type);
		System.out.println("Desc Initial Cap Status: " + initCap);
		System.out.println("Desc Language: " + lang + "\n");
	}
	
	private void printBasicIds(ComponentVersionBI component) throws IOException {
		System.out.print("primUuid: " + component.getPrimordialUuid().toString());
		System.out.println("  sctid: " + queryService.getSctid(component));
	}

	private void printIds(ComponentVersionBI component) throws IOException {
		// Ids
		int nid = component.getNid();
		
		UUID primUuid = component.getPrimordialUuid();
		List<UUID> uuids = component.getUUIDs();
		Collection<? extends IdBI> allIds = component.getAllIds();

		System.out.println("sctid = " + queryService.getSctid(component));
		
		System.out.println("Nid: " + nid + " and primUuid: " + primUuid.toString());
		
		System.out.print("Other UUIDs: ");
		if (uuids.size() == 1) {
			System.out.println("None");
		} else {
			System.out.println();
			for (UUID uid : uuids) {
				System.out.println(uid);
			}
		}		
		
		System.out.println("All Ids");
		if (allIds.size() == 0) {
			System.out.println("   No other Ids");
		} else {
			for (IdBI idBI : allIds) {
				System.out.println("   id = " + idBI.getDenotation());
			}
		}
		
	}

	private void printStamp(ComponentVersionBI comp) throws IOException, ContradictionException {
		// STAMP
		Status status = comp.getStatus();
		String time = translateTime(comp.getTime());
		String author = getDescription(comp.getAuthorNid());
		String module = getDescription(comp.getModuleNid());
		String path = getDescription(comp.getPathNid());
		
		System.out.println("Stamp: " + status + " - " + time + " - " + author + " - " + module + " - " + path);
	}

	private String getDescription(int nid) throws IOException, ContradictionException {
		ConceptVersionBI con = queryService.getConcept(nid);
		
		String descText = null;
		for (DescriptionVersionBI<?> desc : con.getDescriptionsFullySpecifiedActive()) {
			descText = desc.getText();
			break;
		}
		
		if (descText == null && con.getPreferredDescription() != null) {
			descText = con.getPreferredDescription().getText();
		}
		return descText;
	}

	private String translateTime(long time) {
		return TimeHelper.formatDate(time);
	}

	private void printRelationships(ConceptVersionBI concept) throws IOException, ContradictionException {
		Collection<? extends ConceptVersionBI> children = concept.getRelationshipsIncomingOriginsActiveIsa();
		Collection<? extends ConceptVersionBI> parents = concept.getRelationshipsOutgoingDestinationsActiveIsa();
		Collection<? extends RelationshipVersionBI> incomingRels = concept.getRelationshipsIncomingActive();
		Collection<? extends RelationshipVersionBI> outgoingRels = concept.getRelationshipsOutgoingActive();
		
//		for (ConceptVersionBI parent : parents) {
//			System.out.println("Concept Parent :  " + parent.getPreferredDescription().getText());
//		}
//
//		System.out.println();
//		for (ConceptVersionBI child : children) {
//			System.out.println("Concept Child :  " + child.getPreferredDescription().getText());
//		}

		int i = 0;
		for (RelationshipVersionBI<?> rel : outgoingRels) {
			if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
				System.out.print("Concept Parent #" + ++i + ":  ");
				System.out.println(getDescription(rel.getDestinationNid()));
			}
		}

		i = 0;
		for (RelationshipVersionBI<?> rel : incomingRels) {
			if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
				System.out.print("Concept Child #" + ++i + ":  ");
				System.out.println(getDescription(rel.getOriginNid()));
			}
		}

		if (outgoingRels.size() > 0) {
			System.out.println();
		}
		i = 0;
		for (RelationshipVersionBI<?> rel : outgoingRels) {
			if (rel.getTypeNid() != Snomed.IS_A.getNid()) {
				System.out.println("Source Role #" + ++i);
				printRelationship(rel);
			}
		}
		
		if (incomingRels.size() > 0) {
			System.out.println();
		}
		i = 0;
		for (RelationshipVersionBI<?> rel : incomingRels) {
			if (rel.getTypeNid() != Snomed.IS_A.getNid()) {
				System.out.println("Destination Role #" + ++i);
				printRelationship(rel);
			}
		}
	}

	private void printRelationship(RelationshipVersionBI<?> rel) throws IOException, ContradictionException {
		printBasicIds(rel);
		printStamp(rel);
		
		// Relationship Information
		String type = getDescription(rel.getTypeNid());
		String origin = getDescription(rel.getOriginNid());
		String dest = getDescription(rel.getDestinationNid());

		// Relationship-based attributes
		String charId = getDescription(rel.getCharacteristicNid());
		int group = rel.getGroup();
		String refine = getDescription(rel.getRefinabilityNid());
		boolean stated = rel.isStated();

		System.out.println("Relationship Origin Concept: " + origin);
		System.out.println("Relationship Type: " + type);
		System.out.println("Relationship Destination Concept: " + dest);

		System.out.println("Relationship Characteristic Id: " + charId);
		System.out.println("Relationship Group Id: " + group);
		System.out.println("Relationship Refinability: " + refine);
		System.out.println("Relationship is Stated?: " + stated);

		
	}
}
