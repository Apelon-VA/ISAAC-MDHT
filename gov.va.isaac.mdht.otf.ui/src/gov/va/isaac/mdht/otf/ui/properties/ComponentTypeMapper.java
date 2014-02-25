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
package gov.va.isaac.mdht.otf.ui.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

/**
 * Create a filter that is used assist in input filtering for the
 * propertySection extension point.
 * 
 */
public class ComponentTypeMapper implements ITypeMapper {

	/**
	 * Constructor for ComponentTypeMapper.
	 */
	public ComponentTypeMapper() {
		super();
	}

	public Class<?> mapType(Object input) {
		Object mapped = null;
		if (input instanceof IAdaptable) {
			mapped = ((IAdaptable) input).getAdapter(ComponentVersionBI.class);
		}
		if (mapped == null) {
			mapped = input;
		}

		return mapped.getClass();
	}

}
