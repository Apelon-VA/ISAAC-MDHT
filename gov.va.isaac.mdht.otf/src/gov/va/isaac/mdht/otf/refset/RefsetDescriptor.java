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
package gov.va.isaac.mdht.otf.refset;

import java.util.UUID;

import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class RefsetDescriptor {
	
	public static final UUID REFSET_DESCRIPTOR_CONCEPT = null;
	
	private ConceptVersionBI refset;
	
	private RefsetMember attributeDescription;
	private RefsetMember attributeType;
	private Integer attributeOrder;

	public RefsetDescriptor(ConceptVersionBI refset) {
		this.refset = refset;
		
		// TODO search for descriptor members in REFSET_DESCRIPTOR_CONCEPT
		// attributeDescription =
	}

	public ConceptVersionBI getRefset() {
		return refset;
	}
	
	public void setRefset(ConceptVersionBI refset) {
		this.refset = refset;
	}
	
}
