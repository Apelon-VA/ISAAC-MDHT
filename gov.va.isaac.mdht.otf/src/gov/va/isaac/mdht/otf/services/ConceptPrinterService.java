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
import java.io.PrintStream;
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

	public void printConcept(ConceptVersionBI concept, PrintStream output) {
		try {
			output.println();
			output.println("**************************************************************");
			output.println("***** Concept: " + concept.getFullySpecifiedDescription().getText() + " *****");
			output.println("**************************************************************");
			
			printConceptAttributes(concept, output);

			printDescriptions(concept, output);
			output.println();

			printRelationships(concept, output);
			
			RefsetPrinter refsetPrinter = new RefsetPrinter(output);
			refsetPrinter.printMembers(concept);
			
		} catch (IOException | ContradictionException e) {
			e.printStackTrace();
		}		
	}

	private void printConceptAttributes(ConceptVersionBI concept, PrintStream output) throws IOException, ContradictionException {
		printBasicIds(concept, output);

		try {
			ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
			output.println("Concept Fully Defined: " + concept.getConceptAttributes().getVersion(vc).isDefined());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		printStamp(concept, output);

	}

	private void printDescriptions(ConceptChronicleBI concept, PrintStream output) throws IOException, ContradictionException {
		Collection<? extends DescriptionChronicleBI> allDesc = concept.getDescriptions();
		ViewCoordinate vc = appTermStore.getSnomedStatedLatest();

		for (DescriptionChronicleBI descAllVersions : allDesc) {
			DescriptionVersionBI<?> desc = descAllVersions.getVersion(vc);
			output.println();
			output.println("Description: ");

			if (desc != null) {
				printDescription(desc, output);
			}
			else {
				printAllVersions(descAllVersions, output);
			}
		}
	}

	public void printAllVersions(DescriptionChronicleBI fullDesc, PrintStream output) throws IOException, ContradictionException {
		int i = 1;
		
		output.println("*** " + fullDesc.getVersions().size() + " versions: ");
		for (DescriptionVersionBI<?> desc : fullDesc.getVersions()) {
			output.println("Version #" + i++);
			printDescription(desc, output);
		}

	}

	private void printDescription(DescriptionVersionBI<?> desc, PrintStream output) throws IOException, ContradictionException {
		String text = desc.getText();
		String type = getDescription(desc.getTypeNid());
		boolean initCap = desc.isInitialCaseSignificant();
		String lang = desc.getLang();

		printBasicIds(desc, output);
		printStamp(desc, output);

		output.println("Desc Text: " + text);
		output.println("Desc Type: " + type);
		output.println("Desc Initial Cap Status: " + initCap);
		output.println("Desc Language: " + lang + "\n");
	}
	
	private void printBasicIds(ComponentVersionBI component, PrintStream output) throws IOException {
		output.print("primUuid: " + component.getPrimordialUuid().toString());
		output.println("  sctid: " + queryService.getSctid(component));
	}

	private void printIds(ComponentVersionBI component, PrintStream output) throws IOException {
		// Ids
		int nid = component.getNid();
		
		UUID primUuid = component.getPrimordialUuid();
		List<UUID> uuids = component.getUUIDs();
		Collection<? extends IdBI> allIds = component.getAllIds();

		output.println("sctid = " + queryService.getSctid(component));
		
		output.println("Nid: " + nid + " and primUuid: " + primUuid.toString());
		
		output.print("Other UUIDs: ");
		if (uuids.size() == 1) {
			output.println("None");
		} else {
			output.println();
			for (UUID uid : uuids) {
				output.println(uid);
			}
		}		
		
		output.println("All Ids");
		if (allIds.size() == 0) {
			output.println("   No other Ids");
		} else {
			for (IdBI idBI : allIds) {
				output.println("   id = " + idBI.getDenotation());
			}
		}
		
	}

	private void printStamp(ComponentVersionBI comp, PrintStream output) throws IOException, ContradictionException {
		// STAMP
		Status status = comp.getStatus();
		String time = translateTime(comp.getTime());
		String author = getDescription(comp.getAuthorNid());
		String module = getDescription(comp.getModuleNid());
		String path = getDescription(comp.getPathNid());
		
		output.println("Stamp: " + status + " - " + time + " - " + author + " - " + module + " - " + path);
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

	private void printRelationships(ConceptVersionBI concept, PrintStream output) throws IOException, ContradictionException {
		Collection<? extends ConceptVersionBI> children = concept.getRelationshipsIncomingOriginsActiveIsa();
		Collection<? extends ConceptVersionBI> parents = concept.getRelationshipsOutgoingDestinationsActiveIsa();
		Collection<? extends RelationshipVersionBI> incomingRels = concept.getRelationshipsIncomingActive();
		Collection<? extends RelationshipVersionBI> outgoingRels = concept.getRelationshipsOutgoingActive();
		
//		for (ConceptVersionBI parent : parents) {
//			output.println("Concept Parent :  " + parent.getPreferredDescription().getText());
//		}
//
//		output.println();
//		for (ConceptVersionBI child : children) {
//			output.println("Concept Child :  " + child.getPreferredDescription().getText());
//		}

		int i = 0;
		for (RelationshipVersionBI<?> rel : outgoingRels) {
			if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
				output.print("Concept Parent #" + ++i + ":  ");
				output.println(getDescription(rel.getDestinationNid()));
			}
		}

		i = 0;
		for (RelationshipVersionBI<?> rel : incomingRels) {
			if (rel.getTypeNid() == Snomed.IS_A.getNid()) {
				output.print("Concept Child #" + ++i + ":  ");
				output.println(getDescription(rel.getOriginNid()));
			}
		}

		if (outgoingRels.size() > 0) {
			output.println();
		}
		i = 0;
		for (RelationshipVersionBI<?> rel : outgoingRels) {
			if (rel.getTypeNid() != Snomed.IS_A.getNid()) {
				output.println("Source Role #" + ++i);
				printRelationship(rel, output);
			}
		}
		
		if (incomingRels.size() > 0) {
			output.println();
		}
		i = 0;
		for (RelationshipVersionBI<?> rel : incomingRels) {
			if (rel.getTypeNid() != Snomed.IS_A.getNid()) {
				output.println("Destination Role #" + ++i);
				printRelationship(rel, output);
			}
		}
	}

	private void printRelationship(RelationshipVersionBI<?> rel, PrintStream output) throws IOException, ContradictionException {
		printBasicIds(rel, output);
		printStamp(rel, output);
		
		// Relationship Information
		String type = getDescription(rel.getTypeNid());
		String origin = getDescription(rel.getOriginNid());
		String dest = getDescription(rel.getDestinationNid());

		// Relationship-based attributes
		String charId = getDescription(rel.getCharacteristicNid());
		int group = rel.getGroup();
		String refine = getDescription(rel.getRefinabilityNid());
		boolean stated = rel.isStated();

		output.println("Relationship Origin Concept: " + origin);
		output.println("Relationship Type: " + type);
		output.println("Relationship Destination Concept: " + dest);

		output.println("Relationship Characteristic Id: " + charId);
		output.println("Relationship Group Id: " + group);
		output.println("Relationship Refinability: " + refine);
		output.println("Relationship is Stated?: " + stated);

		
	}
}
