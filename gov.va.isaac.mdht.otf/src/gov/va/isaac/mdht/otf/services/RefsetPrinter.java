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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;


/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class RefsetPrinter {
	private TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	
	private PrintStream output = null;
	
	public RefsetPrinter(PrintStream output) {
		this.output = output;
	}

	public void printMembers(ConceptVersionBI concept) {
		try {
			output.println();
			output.println("******** Refset Members **********");
			printRefsetMembers(concept);

			output.println();
			output.println("******** Refset Annotations **********");
			printAnnotations(concept);
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printRefsetMembers(ConceptVersionBI concept) throws Exception {
		Collection<? extends RefexChronicleBI<?>> members = concept.getRefsetMembers();
		Collection<? extends RefexVersionBI<?>> activeMembers = concept.getRefsetMembersActive();
		
		output.println("Has " + members.size() + " Members, "+ activeMembers.size() + " active:");
		
		for (RefexChronicleBI<?> member : members) {
			printAllVersions(member);
		}
	}

	private void printAnnotations(ConceptVersionBI concept) throws Exception {
		Collection<? extends RefexChronicleBI<?>> annotations = concept.getAnnotations();
		Collection<? extends RefexVersionBI<?>> activeAnnotations = concept.getAnnotationsActive(storeService.getSnomedStatedLatest());
		
		output.println("Has " + annotations.size() + " Annotations, "+ activeAnnotations.size() + " active:");
		
		for (RefexChronicleBI<?> rChron : annotations) {
			printAllVersions(rChron);
		}
	}

	public void printAllVersions(RefexChronicleBI<?> refsetChron) throws Exception {
		for (int i = 0; i < refsetChron.getVersions().size(); i++) {
			output.println();
			output.println("Version #: " + (i + 1));
			RefexVersionBI<?> refex = (RefexVersionBI<?>)refsetChron.getVersions().toArray()[i];
			printRefex(refex);
		} 
	}

	private void printRefex(RefexVersionBI<?> refex) throws IOException, ContradictionException {
		output.println("Refex type: " + refex.getRefexType() + " with Status: " + refex.getStatus());
		output.println("Refset Assemblage: " + queryService.getConcept(refex.getAssemblageNid()).getFullySpecifiedDescription().getText());

		output.println("Referenced Component: " + getComponentLabel(queryService.getComponent(refex.getReferencedComponentNid())));
		
		if (refex instanceof RefexStringVersionBI<?>) {
			RefexStringVersionBI<?> extensionMember = (RefexStringVersionBI<?>)refex;
			output.println("String value: " + extensionMember.getString1());
		}
		if (refex instanceof RefexBooleanVersionBI<?>) {
			RefexBooleanVersionBI<?> extensionMember = (RefexBooleanVersionBI<?>)refex;
			output.println("Boolean value: " + extensionMember.getBoolean1());
		}
		if (refex instanceof RefexIntVersionBI<?>) {
			RefexIntVersionBI<?> extensionMember = (RefexIntVersionBI<?>)refex;
			output.println("Integer value: " + extensionMember.getInt1());
		}
		if (refex instanceof RefexLongVersionBI<?>) {
			RefexLongVersionBI<?> extensionMember = (RefexLongVersionBI<?>)refex;
			output.println("Long value: " + extensionMember.getLong1());
		}
		if (refex instanceof RefexFloatVersionBI<?>) {
			RefexFloatVersionBI<?> extensionMember = (RefexFloatVersionBI<?>)refex;
			output.println("Float value: " + extensionMember.getFloat1());
		}

		if (refex instanceof RefexNidVersionBI<?>) {
			RefexNidVersionBI<?> extensionMember = (RefexNidVersionBI<?>)refex;
			String label = null;
			if (extensionMember.getNid1() != 0) {
				label = getComponentLabel(queryService.getComponent(extensionMember.getNid1()));
			}
			output.println("Component 1: " + label);
		}
		if (refex instanceof RefexNidNidVersionBI<?>) {
			RefexNidNidVersionBI<?> extensionMember = (RefexNidNidVersionBI<?>)refex;
			String label = null;
			if (extensionMember.getNid2() != 0) {
				label = getComponentLabel(queryService.getComponent(extensionMember.getNid2()));
			}
			output.println("Component 2: " + label);
		}
		if (refex instanceof RefexNidNidNidVersionBI<?>) {
			RefexNidNidNidVersionBI<?> extensionMember = (RefexNidNidNidVersionBI<?>)refex;
			String label = null;
			if (extensionMember.getNid3() != 0) {
				label = getComponentLabel(queryService.getComponent(extensionMember.getNid3()));
			}
			output.println("Component 3: " + label);
		}
		
	}

	private String getComponentLabel(ComponentVersionBI element) throws IOException, ContradictionException {
		String text = null;

		if (element instanceof ConceptVersionBI) {
			ConceptVersionBI concept = (ConceptVersionBI) element;
			if (concept.getPreferredDescription() != null) {
				text = concept.getPreferredDescription().getText();
			}
			else {
				text = concept.getFullySpecifiedDescription().getText();
			}
		} else if (element instanceof DescriptionVersionBI) {
			text = ((DescriptionVersionBI<?>) element).getText();
		} else if (element instanceof RelationshipVersionBI) {
			RelationshipVersionBI<?> relationship = (RelationshipVersionBI<?>) element;
			ConceptVersionBI type = queryService.getConcept(relationship.getTypeNid());
			ConceptVersionBI source = queryService.getConcept(relationship.getOriginNid());
			ConceptVersionBI target = queryService.getConcept(relationship.getDestinationNid());
			text = getComponentLabel(source) + " -> " + getComponentLabel(type) + " -> " + getComponentLabel(target);
		}
		
		return text;
	}
}

