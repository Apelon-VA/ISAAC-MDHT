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
package gov.va.isaac.mdht.otf.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.query.lucene.LuceneDescriptionIndexer;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;
import org.ihtsdo.otf.tcc.model.index.service.SearchResult;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class DescriptionSearch extends LuceneSearch {
	private IndexerBI descriptionIndexer = null;

	public Query getActiveDescriptionQuery(final String matchText) throws IOException {
		ViewCoordinate vc = storeService.getViewCoordinate();
        Query query = new Query(vc) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return storeService.getTerminologyStore().getAllConceptNids();
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

	public Query getActiveDescriptionQuery(final String matchText, ConceptVersionBI parent) throws IOException, ContradictionException {
		ConceptSpec parentSpec = new ConceptSpec(parent.getFullySpecifiedDescription().getText(),
                parent.getPrimordialUuid());
		return getActiveDescriptionQuery(matchText, parentSpec);
	}

	public Query getActiveDescriptionQuery(final String matchText, final ConceptSpec parentSpec) throws IOException, ContradictionException {
		ViewCoordinate vc = storeService.getViewCoordinate();
        Query query = new Query(vc) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return storeService.getTerminologyStore().getAllConceptNids();
            }

            @Override
            public void Let() throws IOException {
                let("parent", parentSpec);
                let("match-key", matchText);
            }

            @Override
            public Clause Where() {
                return And(ConceptIsKindOf("parent"),
                		ConceptForComponent(DescriptionActiveLuceneMatch("match-key")));
            }
        };

        return query;
	}

	protected Query getQueryIsKindOf(ConceptVersionBI parent) throws IOException {
		ViewCoordinate vc = storeService.getViewCoordinate();
        Query query = new Query(vc) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return storeService.getTerminologyStore().getAllConceptNids();
            }

            @Override
            public void Let() throws IOException {
                let("parent", Snomed.ALLERGIC_ASTHMA);
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
        // Just strip out parens, which are common in FSNs, but also lucene
        // 		search operators (which our users likely won't use)
		matchText = matchText.replaceAll("\\(", "");
		matchText = matchText.replaceAll("\\)", "");
		
        final String localQuery = matchText;

        IndexerBI descriptionIndexer = getDescriptionIndexer();
        // Look for description matches.
        ComponentProperty field = ComponentProperty.DESCRIPTION_TEXT;
        int limit = 1000;
        List<SearchResult> searchResults = descriptionIndexer.query(localQuery, field, limit);
//        System.out.println("Description count = " + searchResults.size());
        
        return searchResults;
	}

	public List<DescriptionVersionBI<?>> searchActiveDescriptions(String matchText) throws IOException, ParseException {
		List<DescriptionVersionBI<?>> descriptions = new ArrayList<DescriptionVersionBI<?>>();
		
        List<SearchResult> searchResults = searchDescriptionIndexer(matchText);

        for (SearchResult searchResult : searchResults) {
            // Get the description object.
            ComponentChronicleBI<?> cc = storeService.getTerminologyStore().getComponent(searchResult.getNid());

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
            ComponentChronicleBI<?> cc = storeService.getTerminologyStore().getComponent(searchResult.getNid());

            // Create a search result for the owning concept.
            final int conceptNid = cc.getConceptNid();
            if (!conceptNids.contains(conceptNid)) {
            	ConceptVersionBI concept = queryService.getConcept(conceptNid);
            	if (Status.ACTIVE == concept.getStatus()) {
		            results.add(concept);
		            conceptNids.add(conceptNid);
            	}
            }
        }

//        System.out.println("Concept count = " + results.size());
		return results;
	}

    private IndexerBI getDescriptionIndexer() throws IOException {
        if (descriptionIndexer == null) {
            List<IndexerBI> indexers = Hk2Looker.get().getAllServices(IndexerBI.class);
            for (IndexerBI indexer : indexers) {
                if (indexer.getIndexerName().equals("descriptions")) {
                   descriptionIndexer = indexer;
                }
            }
        }

        if (descriptionIndexer == null) {
        	descriptionIndexer = new LuceneDescriptionIndexer();
        	Hk2Looker.get().inject(descriptionIndexer);
        	
//        	descriptionIndexer = Hk2Looker.get().create(LuceneDescriptionIndexer.class);
        }
        
        return descriptionIndexer;
    }

}
