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

import java.util.Comparator;

import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

public class DescriptionComparator implements Comparator<ConceptChronicleBI> {

	private ComponentLabelProvider labelProvider = new ComponentLabelProvider();
	
	@Override
	public int compare(ConceptChronicleBI c1, ConceptChronicleBI c2) {
		String label1 = labelProvider.getText(c1);
		String label2 = labelProvider.getText(c2);
		
		if (label1 != null && label2 != null) {
			return label1.compareToIgnoreCase(label2);
		}
		
		return 0;
	}

}
