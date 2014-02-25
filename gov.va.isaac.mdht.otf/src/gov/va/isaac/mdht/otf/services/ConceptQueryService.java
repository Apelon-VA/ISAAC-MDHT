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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.query.lucene.LuceneDescriptionIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;

public class ConceptQueryService {
	private AppBdbTerminologyStore appTermStore;
	
	private IndexerBI descriptionIndexer = null;
	
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

	public ComponentVersionBI getComponent(String uuidString) {
		return getComponent(UUID.fromString(uuidString));
	}

	public ComponentVersionBI getComponent(UUID uuid) {
		ComponentVersionBI componentVersion = null;
		
		try {
			ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
			
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
			ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
			
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
			ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
			
			ConceptChronicleBI conceptChronicle = appTermStore.getStore().getConcept(uuid);
			conceptVersion = conceptChronicle.getVersion(vc);
			
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
			ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
			
			ConceptChronicleBI conceptChronicle = appTermStore.getStore().getConcept(nid);
			conceptVersion = conceptChronicle.getVersion(vc);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return conceptVersion;
	}

	public Long getSctid(ComponentBI component) {
		Long sctid = null;
		int snomedAssemblageNid = appTermStore.getSnomedAssemblageNid();

		try {
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

	public List<ComponentVersionBI> getLuceneMatch(String luceneMatchKey) {
		List<ComponentVersionBI> results = new ArrayList<ComponentVersionBI>();
		
		try {
			Query query = getQueryLucene(luceneMatchKey);

    		NativeIdSetBI resultNids = query.compute();
    		System.out.println("Search results = " + resultNids.size());
    		
    		NativeIdSetItrBI resultItr = resultNids.getSetBitIterator();
    		while (resultItr.next()) {
    			int nid = resultItr.nid();
    			ComponentVersionBI component = getComponent(nid);
    			if (component != null) {
    				results.add(component);
    			}
    			if (results.size() > 100) {
    				break;
    			}
    		}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	protected Query getQueryIsKindOf(ConceptVersionBI parent) throws IOException {
		ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
        Query query = new Query(vc) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return appTermStore.getStore().getAllConceptNids();
            }

            @Override
            public void Let() throws IOException {
                let("allergic-asthma", Snomed.ALLERGIC_ASTHMA);
                let("asthma", Snomed.ASTHMA);
                let("mild asthma", Snomed.MILD_ASTHMA);

                let("observable", Snomed.OBSERVABLE_ENTITY);
            }

            @Override
            public Clause Where() {
                return And(ConceptIsKindOf("observable"));
                
//                return And(ConceptIsKindOf("asthma"),
//                        Not(ConceptIsChildOf("allergic-asthma")),
//                        ConceptIs("allergic-asthma"));
                    
//                            Union(ConceptIsKindOf("allergic-asthma"),
//                            ConceptIsKindOf("mild asthma")));
            }
        };

        return query;
	}

	private List<SearchResult> searchDescriptionIndexer(String matchText) throws IOException, ParseException {
        // "Just strip out parens, which are common in FSNs, but also lucene
        // search operators (which our users likely won't use)"
		matchText = matchText.replaceAll("\\(", "");
		matchText = matchText.replaceAll("\\)", "");
		
		//TODO run as background job
        final String localQuery = matchText;

        IndexerBI descriptionIndexer = getDescriptionIndexer();
        // Look for description matches.
        ComponentProperty field = ComponentProperty.DESCRIPTION_TEXT;
        int limit = 1000;
        List<SearchResult> searchResults = descriptionIndexer.query(localQuery, field, limit);
        System.out.println("Description count = " + searchResults.size());
        
        return searchResults;
	}

	public List<DescriptionVersionBI<?>> searchActiveDescriptions(String matchText) throws IOException, ParseException {
		List<DescriptionVersionBI<?>> descriptions = new ArrayList<DescriptionVersionBI<?>>();
		
        List<SearchResult> searchResults = searchDescriptionIndexer(matchText);

        for (SearchResult searchResult : searchResults) {
            // Get the description object.
            ComponentChronicleBI<?> cc = appTermStore.getStore().getComponent(searchResult.getNid());

            if (cc instanceof DescriptionVersionBI && Status.ACTIVE == ((DescriptionVersionBI<?>) cc).getStatus()) {
            	descriptions.add((DescriptionVersionBI<?>)cc);
            }
        }

		return descriptions;
	}

	public List<ConceptVersionBI> searchActiveConcepts(String matchText) throws IOException, ParseException {
		List<ConceptVersionBI> results = new ArrayList<ConceptVersionBI>();
		Set<Integer> conceptNids = new HashSet<Integer>();
		
        List<SearchResult> searchResults = searchDescriptionIndexer(matchText);

        for (SearchResult searchResult : searchResults) {
            // Get the description object.
            ComponentChronicleBI<?> cc = appTermStore.getStore().getComponent(searchResult.getNid());

            // Create a search result for the owning concept.
            final int conceptNid = cc.getConceptNid();
            if (!conceptNids.contains(conceptNid)) {
            	ConceptVersionBI concept = getConcept(conceptNid);
            	if (Status.ACTIVE == concept.getStatus()) {
		            results.add(concept);
		            conceptNids.add(conceptNid);
            	}
            }
        }

        System.out.println("Concept count = " + results.size());
		return results;
	}

	/*
	 * TODO Throws error in DescriptionLuceneMatch, cannot find description indexer.
	 */
	protected Query getQueryLucene(final String matchText) throws IOException {
		getDescriptionIndexer();
		
		ViewCoordinate vc = appTermStore.getSnomedStatedLatest();
        Query query = new Query(vc) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return appTermStore.getStore().getAllConceptNids();
            }

            @Override
            public void Let() throws IOException {
                let("match-key", matchText);
            }

            @Override
            public Clause Where() {
                return DescriptionActiveLuceneMatch("match-key");
            }
        };

        return query;
	}

    private IndexerBI getDescriptionIndexer() throws IOException {
//        if (descriptionIndexer == null) {
//            List<IndexerBI> indexers = Hk2Looker.get().getAllServices(IndexerBI.class);
//            for (IndexerBI indexer : indexers) {
//                if (indexer.getIndexerName().equals("descriptions")) {
//                   descriptionIndexer = indexer;
//                }
//            }
//        }

        if (descriptionIndexer == null) {
        	descriptionIndexer = new LuceneDescriptionIndexer();
//        	Hk2Looker.get().inject(descriptionIndexer);
//        	Hk2Looker.get().create(LuceneDescriptionIndexer.class);
        }
        
        return descriptionIndexer;
    }


}
