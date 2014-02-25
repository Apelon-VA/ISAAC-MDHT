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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRfx;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

/**
 * Create new terminology components.
 * 
 */
public class ConceptBuilderService {
	private AppBdbTerminologyStore appTermStore;
	private TerminologyStoreService storeService;
	
	public ConceptBuilderService(AppBdbTerminologyStore appDb) {
		this.appTermStore = appDb;
		this.storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	}

	public ConceptCB createConcept(ConceptVersionBI parent, String fsn, String prefTerm) 
			throws IOException, InvalidCAB, ContradictionException {
		List<ConceptVersionBI> parents = Collections.singletonList(parent);
		return createConcept(parents, fsn, prefTerm);
	}
	
	public ConceptCB createConcept(List<ConceptVersionBI> parents, String fsn, String prefTerm) 
			throws IOException, InvalidCAB, ContradictionException {
		LanguageCode lang = LanguageCode.EN_US;
		UUID isA = Snomed.IS_A.getUuids()[0];
		IdDirective idDir = IdDirective.GENERATE_HASH;
        UUID module = Snomed.CORE_MODULE.getLenient().getPrimordialUuid();
        
        UUID parentsUUID[] = new UUID[parents.size()];
        int index = 0;
        for (ConceptVersionBI parent : parents) {
            parentsUUID[index++] = parent.getPrimordialUuid();
		}

		ConceptCB blueprint = new ConceptCB(fsn, prefTerm, lang, isA, idDir, module, parentsUUID);

		return blueprint;
	}

	public DescriptionCAB createSynonymDescription(ConceptVersionBI concept, String descText, LanguageCode lang) 
			throws IOException, InvalidCAB, ContradictionException {
		
		DescriptionCAB blueprint = new DescriptionCAB(concept.getConceptNid(), SnomedMetadataRfx.getDES_SYNONYM_NID(), 
				lang, descText, false, IdDirective.GENERATE_HASH);
		return blueprint;
	}

	public DescriptionCAB createSynonymDescription(ConceptCB concept, String descText, LanguageCode lang) 
			throws IOException, InvalidCAB, ContradictionException {
		
		DescriptionCAB blueprint = new DescriptionCAB(concept.getComponentNid(), SnomedMetadataRfx.getDES_SYNONYM_NID(), 
				lang, descText, false, IdDirective.GENERATE_HASH);
		return blueprint;
	}

	public DescriptionCAB modifyDescription(DescriptionVersionBI description) 
			throws IOException, InvalidCAB, ContradictionException {

		DescriptionCAB blueprint = description.makeBlueprint(
				storeService.getSnomedStatedLatest(),
				IdDirective.GENERATE_HASH,
				RefexDirective.INCLUDE);
		return blueprint;
	}

	/*
	 * 
		UUID relTypeUUID = Snomed.ASSOCIATED_WITH.getLenient().getPrimordialUuid();
	 */
	public RelationshipCAB createRelationship(ConceptVersionBI source, ConceptVersionBI target, UUID typeUUID) 
			throws IOException, InvalidCAB, ContradictionException {
		int group = 0;
		RelationshipType relType = RelationshipType.STATED_ROLE;
		IdDirective idDir = IdDirective.GENERATE_HASH;

		RelationshipCAB blueprint = new RelationshipCAB(source.getPrimordialUuid(), typeUUID, target.getPrimordialUuid(), 
				group, relType, idDir);
		return blueprint;
	}

	public RelationshipCAB createRelationship(ConceptVersionBI source, ConceptVersionBI target, ConceptVersionBI type) 
			throws IOException, InvalidCAB, ContradictionException {
		int group = 0;
		RelationshipType relType = RelationshipType.STATED_ROLE;
		IdDirective idDir = IdDirective.GENERATE_HASH;

		RelationshipCAB blueprint = new RelationshipCAB(source.getPrimordialUuid(), type.getPrimordialUuid(), target.getPrimordialUuid(), 
				group, relType, idDir);
		return blueprint;
	}

	public RelationshipCAB createRelationship(ConceptCB source, ConceptVersionBI target, UUID typeUUID) 
			throws IOException, InvalidCAB, ContradictionException {
		int group = 0;
		RelationshipType relType = RelationshipType.STATED_ROLE;
		IdDirective idDir = IdDirective.GENERATE_HASH;

		RelationshipCAB blueprint = new RelationshipCAB(source.getComponentUuid(), typeUUID, target.getPrimordialUuid(), 
				group, relType, idDir);
		return blueprint;
	}

	public RelationshipCAB createRelationship(ConceptCB source, ConceptCB target, UUID typeUUID) 
			throws IOException, InvalidCAB, ContradictionException {
		int group = 0;
		RelationshipType relType = RelationshipType.STATED_ROLE;
		IdDirective idDir = IdDirective.GENERATE_HASH;

		RelationshipCAB blueprint = new RelationshipCAB(source.getComponentUuid(), typeUUID, target.getComponentUuid(), 
				group, relType, idDir);
		return blueprint;
	}

	public RefexCAB createRefex(ConceptVersionBI collection, ComponentVersionBI referencedComponent) 
			throws IOException, InvalidCAB, ContradictionException {
		RefexType refexType = RefexType.MEMBER;
		IdDirective idDir = IdDirective.GENERATE_HASH;
		RefexDirective refexDir = RefexDirective.INCLUDE;

		RefexCAB blueprint = new RefexCAB(refexType, referencedComponent.getPrimordialUuid(), collection.getPrimordialUuid(), idDir, refexDir);
		return blueprint;
	}

	public RelationshipCAB modifyRelationship(RelationshipVersionBI relationship) 
			throws IOException, InvalidCAB, ContradictionException {
		
//		RelationshipCAB blueprint = new RelationshipCAB(relationship.getOriginNid(), relationship.getTypeNid(), relationship.getDestinationNid(), relationship.getGroup(), 
//				RelationshipType.QUALIFIER, relationship, storeService.getSnomedStatedLatest(), IdDirective.GENERATE_RANDOM, RefexDirective.INCLUDE);
		
//		RelationshipCAB blueprint = new RelationshipCAB(relationship.getConceptNid(), relationship.getTypeNid(), relationship.getDestinationNid(), 1, 
//				RelationshipType.QUALIFIER, relationship, storeService.getSnomedStatedLatest(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
		
		RelationshipCAB blueprint = relationship.makeBlueprint(storeService.getSnomedStatedLatest(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
		return blueprint;
	}

	public RelationshipCAB modifyRelationship(RelationshipCAB relationship) 
			throws IOException, InvalidCAB, ContradictionException {
		
		RelationshipType relType = getRelationshipType(relationship);
		RelationshipCAB blueprint = new RelationshipCAB(relationship.getSourceNid(), relationship.getTypeNid(), relationship.getTargetNid(), relationship.getGroup(), 
				relType, IdDirective.GENERATE_HASH);
		
		return blueprint;
	}

	public RefexCAB modifyRefex(RefexVersionBI refex) 
			throws IOException, InvalidCAB, ContradictionException {
		
		RefexCAB blueprint = refex.makeBlueprint(storeService.getSnomedStatedLatest(), IdDirective.GENERATE_HASH, RefexDirective.INCLUDE);
		return blueprint;
	}

	public RelationshipType getRelationshipType(RelationshipCAB blueprint)
			throws ValidationException, IOException, InvalidCAB {
		// default, if cannot be computed from source
		RelationshipType relType = RelationshipType.QUALIFIER;

		// copied from Relationship.makeBlueprint()
		if ((blueprint.getCharacteristicNid() == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid())
				|| (blueprint.getCharacteristicNid() == SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid())
				|| (blueprint.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid())) {
			throw new InvalidCAB(
					"Inferred relationships can not be used to make blueprints");
		} else if ((blueprint.getCharacteristicNid() == SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient().getNid())
				|| (blueprint.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid())) {
			relType = RelationshipType.STATED_HIERARCHY;
		}
		
		/*
            case STATED_HIERARCHY:
                characteristicUuid = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()[0];
                refinabilityUuid = SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids()[0];
                break;
            case STATED_ROLE:
                characteristicUuid = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()[0];
                refinabilityUuid = SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()[0];
                break;
            case INFERRED_HIERARCY:
                characteristicUuid =SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids()[0];
                refinabilityUuid =SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids()[0];
                break;
            case QUALIFIER:
                characteristicUuid = SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getUuids()[0];
                refinabilityUuid =SnomedMetadataRf2.MANDATORY_REFINIBILITY_RF2.getUuids()[0];
                break;
            case INFERRED_ROLE:
                characteristicUuid =SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids()[0]; 
                refinabilityUuid = SnomedMetadataRf2.OPTIONAL_REFINIBILITY_RF2.getUuids()[0];
                break;
            case HISTORIC:
                characteristicUuid =SnomedMetadataRf2.HISTORICAL_REFSET_RF2.getUuids()[0]; 
                refinabilityUuid =SnomedMetadataRf2.NOT_REFINABLE_RF2.getUuids()[0];
                break;
		 */

		return relType;
	}

	public ConceptVersionBI construct(ConceptCB blueprint) throws IOException, InvalidCAB, ContradictionException {
		ConceptChronicleBI chronicle = appTermStore.getBuilder().construct(blueprint);
		appTermStore.getStore().addUncommitted(chronicle);
		ConceptVersionBI version = chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}

	public ConceptAttributeVersionBI construct(ConceptAttributeAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		ConceptAttributeChronicleBI chronicle = appTermStore.getBuilder().construct(blueprint);
		appTermStore.getStore().addUncommitted(chronicle.getEnclosingConcept());
		ConceptAttributeVersionBI version = chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}

	public DescriptionVersionBI construct(DescriptionCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		DescriptionChronicleBI chronicle = appTermStore.getBuilder().construct(blueprint);
		DescriptionVersionBI version = chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}

	public DescriptionVersionBI constructIfNotCurrent(DescriptionCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		DescriptionChronicleBI chronicle = appTermStore.getBuilder().constructIfNotCurrent(blueprint);
		DescriptionVersionBI version = chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}

	public RelationshipVersionBI construct(RelationshipCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		RelationshipChronicleBI chronicle = appTermStore.getBuilder().construct(blueprint);
		RelationshipVersionBI version = chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}

	public RelationshipVersionBI constructIfNotCurrent(RelationshipCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		RelationshipChronicleBI chronicle = appTermStore.getBuilder().constructIfNotCurrent(blueprint);
		RelationshipVersionBI version = chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}

	public RefexVersionBI construct(RefexCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		RefexChronicleBI chronicle = appTermStore.getBuilder().construct(blueprint);
		RefexVersionBI version = (RefexVersionBI) chronicle.getVersion(appTermStore.getSnomedStatedLatest());;
		
		return version;
	}

	public RefexVersionBI constructIfNotCurrent(RefexCAB blueprint) throws IOException, InvalidCAB, ContradictionException {
		RefexChronicleBI chronicle = appTermStore.getBuilder().constructIfNotCurrent(blueprint);
		RefexVersionBI version = (RefexVersionBI) chronicle.getVersion(appTermStore.getSnomedStatedLatest());
		
		return version;
	}
}
