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
package gov.va.isaac.mdht.otf.ui.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

/**
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class ConceptItem implements IAdaptable {
	private ConceptVersionBI conceptVersion = null;
	private ConceptItem parent = null;
	private String label = null;
	
	public ConceptItem(ConceptVersionBI concept, ConceptItem parent) {
		this.conceptVersion = concept;
		this.parent = parent;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConceptItem && ((ConceptItem)obj).getConceptVersion().getPrimordialUuid()
				.equals(conceptVersion.getPrimordialUuid())) {
			return true;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return conceptVersion.hashCode();
	}
	
	public ConceptVersionBI getConceptVersion() {
		return conceptVersion;
	}

	public ConceptItem getParent() {
		return parent;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class key) {
		if (ComponentBI.class.isAssignableFrom(key)) {
			return conceptVersion;
		}
		return null;
	}
	
	public void setVersion(ConceptVersionBI concept) {
		this.conceptVersion = concept;
	}

}
