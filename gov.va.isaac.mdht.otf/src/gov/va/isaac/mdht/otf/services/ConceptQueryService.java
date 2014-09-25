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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;

public class ConceptQueryService {
	private AppBdbTerminologyStore appTermStore;
	
	public ConceptQueryService(AppBdbTerminologyStore appDb) {
		this.appTermStore = appDb;
	}

	public List<ConceptVersionBI> getRootConcepts() {
		return appTermStore.getRootConcepts();
	}
	
	/**
	 * Recursively collect all descendant children of given concept.
	 * 
	 * @param concept
	 * @return
	 */
	public List<ConceptVersionBI> getAllChildren(ConceptVersionBI concept) {
		List<ConceptVersionBI> children = new ArrayList<ConceptVersionBI>();
		
		try {
			for (ConceptVersionBI child : concept.getRelationshipsIncomingOriginsActiveIsa()) {
				children.add(child);
				children.addAll(getAllChildren(child));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return children;
	}

	/*
	 * TODO get modules by code system?
	 */
	public List<ConceptVersionBI> getAllModules() throws IOException, ContradictionException {
		List<ConceptVersionBI> modules = new ArrayList<ConceptVersionBI>();
		ConceptVersionBI rootModule = getConcept(UUID.fromString(AppBdbTerminologyStore.MODULE_ROOT_UUID));
		List<ConceptVersionBI> allModules = getAllChildren(rootModule);
		
		// collect only leaf concepts, those with no children
		for (ConceptVersionBI module : allModules) {
			if (module.getRelationshipsIncomingOriginsActiveIsa().isEmpty()) {
				modules.add(module);
			}
		}
		
		return modules;
	}

	/**
	 * Recursively collect all parents of given concept, but only first if multiple parents.
	 * 
	 * @param concept
	 * @return
	 */
	public List<ConceptVersionBI> getParentPath(ConceptVersionBI concept) {
		List<ConceptVersionBI> parents = new ArrayList<ConceptVersionBI>();
		
		try {
			for (ConceptVersionBI parent : concept.getRelationshipsOutgoingDestinationsActiveIsa()) {
				parents.add(parent);
				parents.addAll(getParentPath(parent));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return parents;
	}

	public ComponentVersionBI getComponent(String uuidString) {
		return getComponent(UUID.fromString(uuidString));
	}

	public ComponentVersionBI getComponent(UUID uuid) {
		ComponentVersionBI componentVersion = null;
		
		try {
			ViewCoordinate vc = appTermStore.getViewCoordinate();
			
			ComponentChronicleBI<?> componentChronicle = appTermStore.getStore().getComponent(uuid);
			if (componentChronicle instanceof ConceptAttributeVersionBI) {
				componentChronicle = appTermStore.getStore().getConcept(uuid);
			}
			componentVersion = componentChronicle.getVersion(vc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return componentVersion;
	}

	public ComponentVersionBI getComponent(int nid) {
		ComponentVersionBI componentVersion = null;
		
		try {
			ViewCoordinate vc = appTermStore.getViewCoordinate();
			
			ComponentChronicleBI<?> componentChronicle = appTermStore.getStore().getComponent(nid);
			if (componentChronicle instanceof ConceptAttributeVersionBI) {
				componentChronicle = appTermStore.getStore().getConcept(nid);
			}
			componentVersion = componentChronicle.getVersion(vc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return componentVersion;
	}

	public ConceptVersionBI getConcept(UUID uuid) {
		ConceptVersionBI conceptVersion = null;
		
		try {
			ViewCoordinate vc = appTermStore.getViewCoordinate();
			
			ConceptChronicleBI conceptChronicle = appTermStore.getStore().getConcept(uuid);
			if (conceptChronicle != null) {
				conceptVersion = conceptChronicle.getVersion(vc);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return conceptVersion;
	}

	public ConceptVersionBI getConcept(String uuidString) {
		return getConcept(UUID.fromString(uuidString));
	}

	public ConceptVersionBI getConcept(int nid) {
		ConceptVersionBI conceptVersion = null;

		try {
			ViewCoordinate vc = appTermStore.getViewCoordinate();
			
			ConceptChronicleBI conceptChronicle = appTermStore.getStore().getConcept(nid);
			conceptVersion = conceptChronicle.getVersion(vc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return conceptVersion;
	}

	public ConceptVersionBI getModuleFor(ComponentVersionBI component) {
		ConceptVersionBI module = getConcept(component.getModuleNid());
		
		return module;
	}
	
	public Long getSctid(ComponentBI component) {
		Long sctid = null;
		try {
			int snomedAssemblageNid = TermAux.SNOMED_IDENTIFIER.getNid();
			
			for (RefexChronicleBI<?> annotation : component.getAnnotations()) {
				if (annotation.getAssemblageNid() == snomedAssemblageNid) {
					RefexLongVersionBI<?> sctidVersion = (RefexLongVersionBI<?>) annotation.getPrimordialVersion();
					sctid = sctidVersion.getLong1();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sctid;
	}

	public ConceptVersionBI getOwnerOfRefsetMember(RefexChronicleBI<?> member) {
		return getConcept(member.getAssemblageNid());
	}

	public ConceptVersionBI getOwnerOfAnnotatedRefsetMember(RefexChronicleBI<?> member) {
		return getConcept(member.getReferencedComponentNid());
	}

}
