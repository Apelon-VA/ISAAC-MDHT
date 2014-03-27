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

import gov.va.isaac.mdht.otf.refset.RefsetAttributeType;
import gov.va.isaac.mdht.otf.refset.RefsetMember;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;

public abstract class OTFTableEditingSupport extends EditingSupport {
	
	protected OTFTableViewer tableViewer = null;
	protected CellEditor cellEditor = null;
	protected RefsetAttributeType attributeType = null;

	public OTFTableEditingSupport(OTFTableViewer viewer, RefsetAttributeType attributeType) {
		super(viewer);
		this.tableViewer = viewer;
		this.attributeType = attributeType;
	}

	public OTFTableEditingSupport(OTFTableViewer viewer) {
		this(viewer, null);
	}

	@Override
	protected boolean canEdit(Object element) {
		return element instanceof CreateOrAmendBlueprint || element instanceof RefsetMember;
	}

	@Override
	protected void setValue(final Object element, final Object value) {

		doSetValue(element, value);
		tableViewer.update(element, null);
	}

	public RefsetAttributeType getAttributeType() {
		return attributeType;
	}
	
	public void setAttributeType(RefsetAttributeType attributeType) {
		this.attributeType = attributeType;
	}
	
	protected abstract String getOperationLabel();
	
	protected abstract IStatus doSetValue(Object element, final Object value);
	
}
