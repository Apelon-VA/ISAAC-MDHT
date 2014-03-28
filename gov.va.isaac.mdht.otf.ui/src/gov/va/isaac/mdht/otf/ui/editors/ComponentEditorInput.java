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
package gov.va.isaac.mdht.otf.ui.editors;

import java.util.UUID;

import gov.va.isaac.mdht.otf.ui.providers.ComponentLabelProvider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

/**
 * Input for editors on OTF components.
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class ComponentEditorInput implements IEditorInput, IPersistableElement {

	private LabelProvider labelProvider = new ComponentLabelProvider();
	
	private UUID componentUUID = null;
	private ComponentVersionBI componentVerision = null;
	private String name;

	public ComponentEditorInput(ComponentVersionBI componentVerision) {
		this.componentVerision = componentVerision;
	}

	public ComponentEditorInput(IMemento memento) {
		loadState(memento);
	}
	
	public ComponentVersionBI getComponentVersion() {
		return componentVerision;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		return componentVerision != null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = labelProvider.getText(componentVerision);
		}
		return name;
	}

	@Override
	public String getToolTipText() {
		LabelProvider labelProvider = new ComponentLabelProvider(true);
		String fsn = labelProvider.getText(componentVerision);
		return fsn;
	}

	@Override
	public String getFactoryId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveState(IMemento memento) {
		// TODO Auto-generated method stub
		
	}

	protected void loadState(IMemento memento) {

	}
}
