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

import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

public class OTFSheetLabelProvider extends DecoratingLabelProvider {

	public OTFSheetLabelProvider() {
		super(new ComponentLabelProvider(), null);
	}

	@Override
	public String getText(Object element) {
		Object selected = unwrap(element);
		if (selected instanceof ConceptVersionBI) {
			try {
				return ((ConceptVersionBI)selected).getFullySpecifiedDescription().getText();
			} catch (Exception e) {
			}
		}

		return super.getText(selected);
	}

	@Override
	public Image getImage(Object element) {
		return super.getImage(unwrap(element));
	}

	private Object unwrap(Object element) {
		if (element instanceof IStructuredSelection) {
			return unwrap(((IStructuredSelection) element).getFirstElement());
		}
		if (element instanceof IAdaptable) {
			ComponentVersionBI component = (ComponentVersionBI) ((IAdaptable) element).getAdapter(ComponentVersionBI.class);
			if (component != null) {
				return component;
			}
		}
		return element;
	}

}
