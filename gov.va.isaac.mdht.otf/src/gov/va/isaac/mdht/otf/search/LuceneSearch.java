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

import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.services.TerminologyStoreService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class LuceneSearch {
	protected TerminologyStoreService storeService = TerminologyStoreFactory.INSTANCE.createTerminologyStoreService();
	protected ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();

	public List<ComponentVersionBI> getQueryResults(Query query) throws IOException, Exception {
		return getQueryResults(query, 0);
	}
	
	public List<ComponentVersionBI> getQueryResults(Query query, int limit) throws IOException, Exception {
		List<ComponentVersionBI> results = new ArrayList<ComponentVersionBI>();
		
		NativeIdSetBI resultNids = query.compute();
		
		NativeIdSetItrBI resultItr = resultNids.getSetBitIterator();
		while (resultItr.next()) {
			int nid = resultItr.nid();
			ComponentVersionBI component = queryService.getComponent(nid);
			if (component != null) {
				results.add(component);
			}
			if (limit > 0 && results.size() > limit) {
				break;
			}
		}
		System.out.println("Components found = " + results.size());
		
		return results;
	}

	public List<ConceptVersionBI> getQueryResultConcepts(Query query) throws IOException, Exception {
		return getQueryResultConcepts(query, 0);
	}
	
	public List<ConceptVersionBI> getQueryResultConcepts(Query query, int limit) throws IOException, Exception {
		List<ConceptVersionBI> results = new ArrayList<ConceptVersionBI>();
		
		NativeIdSetBI resultNids = query.compute();
		NativeIdSetItrBI resultItr = resultNids.getSetBitIterator();
		
		while (resultItr.next()) {
			int nid = resultItr.nid();
			ComponentVersionBI component = queryService.getComponent(nid);

			if (component instanceof ConceptVersionBI) {
				results.add((ConceptVersionBI)component);
			}
			else if (component instanceof DescriptionVersionBI) {
				ConceptVersionBI concept = ((DescriptionVersionBI<?>)component).getEnclosingConcept().getVersion(storeService.getSnomedStatedLatest());
				if (concept != null && !results.contains(concept)) {
					results.add(concept);
				}
			}
			else if (component instanceof RefexVersionBI<?>) {
				ConceptVersionBI concept = queryService.getConcept(((RefexVersionBI<?>)component).getReferencedComponentNid());
				if (concept != null && !results.contains(concept)) {
					results.add(concept);
				}
			}

			if (limit > 0 && results.size() > limit) {
				break;
			}
		}
		System.out.println("Concepts found = " + results.size());
		
		return results;
	}
	
}
