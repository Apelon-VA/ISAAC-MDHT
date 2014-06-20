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
import java.util.UUID;

import org.ihtsdo.otf.query.implementation.Clause;
import org.ihtsdo.otf.query.implementation.Query;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class IdentifierSearch extends LuceneSearch {

	public ComponentVersionBI getComponent(String uuidString) {
		return getComponent(UUID.fromString(uuidString));
	}

	public ComponentVersionBI getComponent(UUID uuid) {
		ComponentVersionBI componentVersion = null;
		
		try {
			componentVersion = queryService.getComponent(uuid);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return componentVersion;
	}


	public ConceptVersionBI getConceptFromUUID(UUID uuid) {
		ConceptVersionBI conceptVersion = queryService.getConcept(uuid);
		return conceptVersion;
	}

	public ConceptVersionBI getConceptFromUUID(String uuidString) {
		return getConceptFromUUID(UUID.fromString(uuidString));
	}

	public Query getQueryForSnomedId(final String sctId) throws IOException {
		final ViewCoordinate vc = storeService.getSnomedStatedLatest();
        Query query = new Query(vc) {
            @Override
            protected NativeIdSetBI For() throws IOException {
                return storeService.getTerminologyStore().getAllConceptNids();
            }

            @Override
            public void Let() throws IOException {
            	let("sctid", sctId);
            }

            @Override
            public Clause Where() {
            	return RefsetLuceneMatch("sctid");
            }
        };

        return query;
	}

}
