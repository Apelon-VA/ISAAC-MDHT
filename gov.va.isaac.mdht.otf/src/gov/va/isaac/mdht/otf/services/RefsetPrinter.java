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
import java.util.Collection;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;


/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class RefsetPrinter {
	private TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	
	public RefsetPrinter() {

	}

	public void printMembers(ConceptVersionBI concept) {
		try {
			System.out.println();
			System.out.println("******** Refset Members **********");
			printRefsetMembers(concept);

			System.out.println();
			System.out.println("******** Annotated Members **********");
			printAnnotatedRefsetMembers(concept);
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printAnnotatedRefsetMembers(ConceptVersionBI concept) throws Exception {
		System.out.println("Refset Concept: " + concept.getPreferredDescription().getText());
		Collection<? extends RefexChronicleBI<?>> members = concept.getAnnotations();
		Collection<? extends RefexVersionBI<?>> activeMembers = concept.getAnnotationsActive(storeService.getSnomedStatedLatest());
		System.out.println("With " + members.size() + " Annotation Members, "+ activeMembers.size() + " active:");
		
		for (RefexChronicleBI<?> rChron : members) {
			printAllVersionsOfAnnotated(rChron);
		}
	}

	private void printRefsetMembers(ConceptVersionBI concept) throws Exception {
		System.out.println("Refset Concept: " + concept.getPreferredDescription().getText());
		Collection<? extends RefexChronicleBI<?>> members = concept.getRefsetMembers();
		Collection<? extends RefexVersionBI<?>> activeMembers = concept.getRefsetMembersActive();
		
		System.out.println("With " + members.size() + " Members, "+ activeMembers.size() + " active:");
		
		for (RefexChronicleBI<?> rChron : members) {
			System.out.println();
			printAllVersionsOfMember(rChron);
		}
	}

	public void printAllVersionsOfMember(RefexChronicleBI<?> refsetChron) throws Exception {
		int i = 1;
		for (RefexVersionBI<?> member : refsetChron.getVersions()) {
			System.out.println("Version #: " + i++);
			ComponentVersionBI refComp = queryService.getComponent(member.getReferencedComponentNid());
			 
			if (member.getRefexType() == RefexType.MEMBER) {
				System.out.println(getComponentLabel(refComp) + " with Status: " + member.getStatus());
				
			} else if (member.getRefexType() == RefexType.CID_STR) {
				RefexNidStringVersionBI<?> extensionMember = (RefexNidStringVersionBI<?>)member;
				String strExt = extensionMember.getString1();
				int cidExtNid = extensionMember.getNid1();
				ComponentVersionBI cidExtCon = queryService.getComponent(cidExtNid);
				String componentLabel = getComponentLabel(cidExtCon);

				System.out.println(getComponentLabel(refComp) + " of Member Type with Status: " + member.getStatus());
				System.out.println("Is extended with CID: " + componentLabel + " and String: " + strExt);
			} 
		}
	}

	public void printAllVersionsOfAnnotated(RefexChronicleBI<?> refsetChron) throws Exception {
		for (int i = 0; i < refsetChron.getVersions().size(); i++) {
			System.out.println("Version #: " + (i + 1));
			RefexVersionBI<?> member = (RefexVersionBI<?>)refsetChron.getVersions().toArray()[i];
			ConceptVersionBI refCon = queryService.getConcept(member.getAssemblageNid());

			if (member.getRefexType() == RefexType.MEMBER) {
				System.out.println(refCon.getPreferredDescription().getText() + " with Status: " + member.getStatus());
			} else if (member.getRefexType() == RefexType.STR) {
				RefexStringVersionBI<?> extensionMember = (RefexStringVersionBI<?>)member;
				String strExt = extensionMember.getString1();

				System.out.println(refCon.getPreferredDescription().getText() + " of STR Type with Status: " + member.getStatus());
				System.out.println("Is extended with String: " + strExt);
			} else if (member.getRefexType() == RefexType.LONG) {
				RefexLongVersionBI<?> extensionMember = (RefexLongVersionBI<?>)member;
				Long value = extensionMember.getLong1();

				System.out.println(refCon.getPreferredDescription().getText() + " of LONG Type with Status: " + member.getStatus());
				System.out.println("Is extended with Long: " + value);
			} else if (member.getRefexType() == RefexType.CID) {
				RefexNidVersionBI<?> extensionMember = (RefexNidVersionBI<?>)member;
				int cidExtNid = extensionMember.getNid1();
				ComponentVersionBI cidExtCon = queryService.getComponent(cidExtNid);
				String componentLabel = getComponentLabel(cidExtCon);

				System.out.println(refCon.getPreferredDescription().getText() + " of CID Type with Status: " + member.getStatus());
				System.out.println("Is extended with CID: " + componentLabel);
			} else if (member.getRefexType() == RefexType.CID_STR) {
				RefexNidStringVersionBI<?> extensionMember = (RefexNidStringVersionBI<?>)member;
				String strExt = extensionMember.getString1();
				int cidExtNid = extensionMember.getNid1();
				ComponentVersionBI cidExtCon = queryService.getComponent(cidExtNid);
				String componentLabel = getComponentLabel(cidExtCon);

				System.out.println(refCon.getPreferredDescription().getText() + " of CID_STR Type with Status: " + member.getStatus());
				System.out.println("Is extended with CID: " + componentLabel + " and String: " + strExt);
			} 
			else {
				System.out.println("Refex type: " + member.getRefexType());
			}
		} 
	}

	private String getComponentLabel(ComponentVersionBI element) throws IOException, ContradictionException {
		String text = null;

		if (element instanceof ConceptVersionBI) {
			text = ((ConceptVersionBI) element).getPreferredDescription().getText();
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

