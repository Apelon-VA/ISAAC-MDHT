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
package gov.va.isaac.mdht.otf.ui.filter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IFilter;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

/**
 * Filters OTF elements in user interface.
 */
public abstract class AbstractFilter implements IFilter {

	protected ComponentVersionBI getComponent(Object object) {
		ComponentVersionBI component = null;

		if (object instanceof ComponentVersionBI) {
			component = (ComponentVersionBI) object;
		}
		else if (object instanceof IAdaptable) {
			component = (ComponentVersionBI) ((IAdaptable) object).getAdapter(ComponentVersionBI.class);
		}

		return component;
	}

}
