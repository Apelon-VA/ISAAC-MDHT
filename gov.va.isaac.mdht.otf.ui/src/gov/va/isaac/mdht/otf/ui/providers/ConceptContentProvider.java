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

import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;
import gov.va.isaac.mdht.otf.ui.internal.Activator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.statushandlers.StatusManager;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;

public class ConceptContentProvider implements IStructuredContentProvider, ITreeContentProvider {
	private ConceptQueryService conceptQueryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();

	public ConceptContentProvider() {
	}
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		if (parent instanceof IViewSite || parent == null) {
			List<ConceptItem> rootItems = new ArrayList<ConceptItem>();
			List<ConceptVersionBI> rootConcepts = conceptQueryService.getRootConcepts();
			for (ConceptVersionBI rootConcept : rootConcepts) {
				rootItems.add(new ConceptItem(rootConcept, null));
			}
			return rootItems.toArray();
		}
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof ConceptItem) {
			return ((ConceptItem) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof ConceptItem) {
			ConceptVersionBI conceptVersion = ((ConceptItem) parent).getConceptVersion();
			if (conceptVersion != null) {
				try {
					Collection<? extends ConceptVersionBI> children = conceptVersion.getRelationshipsIncomingOriginsActiveIsa();
					List<ConceptItem> childItems = new ArrayList<ConceptItem>();
					for (ConceptVersionBI child : children) {
						ConceptItem item = new ConceptItem(child, (ConceptItem) parent);
						childItems.add(item);
					}
					return childItems.toArray();
				} catch (Exception e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get concept children", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof ConceptItem) {
			ConceptVersionBI conceptVersion = ((ConceptItem) parent).getConceptVersion();
			if (conceptVersion != null) {
				try {
					Collection<? extends ConceptVersionBI> children = conceptVersion.getRelationshipsIncomingOriginsActiveIsa();
					return children.size() > 0;
				} catch (Exception e) {
					StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get concept children", e), 
							StatusManager.SHOW | StatusManager.LOG);
				}
			}
		}
		return false;
	}

}
