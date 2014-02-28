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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.statushandlers.StatusManager;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;

public class ComponentLabelProvider extends LabelProvider implements ITableLabelProvider {

	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();
	
	private boolean useFSN = false;

	public ComponentLabelProvider() {
		
	}

	public ComponentLabelProvider(boolean useFSN) {
		this.useFSN = useFSN;
	}
	
	protected Object unwrap(Object object) {
		ComponentBI component = null;
		CreateOrAmendBlueprint blueprint = null;
		
		if (object instanceof ComponentBI) {
			component = (ComponentBI) object;
		} else if (object instanceof CreateOrAmendBlueprint) {
			blueprint = (CreateOrAmendBlueprint) object;
		} else if (object instanceof IAdaptable) {
			blueprint = (CreateOrAmendBlueprint) ((IAdaptable) object).getAdapter(CreateOrAmendBlueprint.class);
			if (blueprint == null) {
				component = (ComponentBI) ((IAdaptable) object).getAdapter(ComponentBI.class);
			}
		}
		
		// if object is both blueprint and component, return blueprint for editing
		return blueprint != null ? blueprint : component;
	}
	
	@Override
	public String getText(Object obj) {
		String text = null;
		
		if (obj instanceof ConceptItem && ((ConceptItem)obj).getLabel() != null) {
			text = ((ConceptItem)obj).getLabel();
		}
		
		if (text == null) {
			Object element = unwrap(obj);
	
			try {
				if (element instanceof ConceptVersionBI) {
					DescriptionVersionBI<?> desc = null;
					if (useFSN) {
						desc = ((ConceptVersionBI) element).getFullySpecifiedDescription();
					}
					else {
						desc = ((ConceptVersionBI) element).getPreferredDescription();
					}
					
					if (desc == null) {
						desc = ((ConceptVersionBI) element).getFullySpecifiedDescription();
					}
					text = desc.getText();
					
				} else if (element instanceof DescriptionVersionBI) {
					text = ((DescriptionVersionBI<?>) element).getText();
				} else if (element instanceof RelationshipVersionBI) {
					RelationshipVersionBI<?> relationship = (RelationshipVersionBI<?>) element;
					ConceptVersionBI type = queryService.getConcept(relationship.getTypeNid());
					ConceptVersionBI source = queryService.getConcept(relationship.getOriginNid());
					ConceptVersionBI target = queryService.getConcept(relationship.getDestinationNid());
					text = getText(source) + " -> " + getText(type) + " -> " + getText(target);
				}
					
				else if (element instanceof ConceptCB) {
					text = ((ConceptCB) element).getPreferredName();
				} else if (element instanceof DescriptionCAB) {
					text = ((DescriptionCAB) element).getText();
				} else if (element instanceof RelationshipCAB) {
					RelationshipCAB relationship = (RelationshipCAB) element;
					ConceptVersionBI type = queryService.getConcept(relationship.getTypeNid());
					ConceptVersionBI source = queryService.getConcept(relationship.getSourceNid());
					ConceptVersionBI target = queryService.getConcept(relationship.getTargetNid());
					text = getText(source) + " -> " + getText(type) + " -> " + getText(target);
					
				} else if (element instanceof RefexVersionBI<?>) {
					RefexVersionBI<?> refex = (RefexVersionBI<?>) element;
					
					// used as annotation
//					if (refex instanceof RefexNidVersionBI<?>) {
//						RefexNidVersionBI<?> nidRefex = (RefexNidVersionBI<?>) refex;
//						int nidComponent1 = nidRefex.getNid1();
//						ComponentVersionBI component1 = queryService.getComponent(nidComponent1);
//						text = getText(component1);
//					}
//					else {
						ConceptVersionBI refset = queryService.getConcept(refex.getAssemblageNid());
						text = refset.getPreferredDescription().getText();
//					}
				}

				if (obj instanceof ConceptItem) {
					((ConceptItem)obj).setLabel(text);
				}
			} catch (Exception e) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get label text", e), 
						StatusManager.SHOW | StatusManager.LOG);
			}
		}

		return text;
	}

	@Override
	public Image getImage(Object obj) {
		Object element = unwrap(obj);

		if (element instanceof CreateOrAmendBlueprint) {
			return Activator.getDefault().getBundledImage("icons/obj16/Blueprint.gif");
		}
		else if (element instanceof ComponentChronicleBI && ((ComponentChronicleBI<?>)element).isUncommitted()) {
			return Activator.getDefault().getBundledImage("icons/obj16/Uncommitted.gif");
		}
		else if (element instanceof ConceptVersionBI) {
			return Activator.getDefault().getBundledImage("icons/obj16/Concept.gif");
		}
		else if (element instanceof DescriptionVersionBI) {
			return Activator.getDefault().getBundledImage("icons/obj16/Description.gif");
		}
		else if (element instanceof RelationshipVersionBI) {
			return Activator.getDefault().getBundledImage("icons/obj16/Relationship.gif");
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object obj, int columnIndex) {
		if (columnIndex == 0) {
			Object element = unwrap(obj);

			if (element instanceof CreateOrAmendBlueprint) {
				//return a Blueprint image, if obj is a kind of CreateOrAmendBlueprint
				if (!org.ihtsdo.otf.tcc.api.coordinate.Status.ACTIVE.equals(((CreateOrAmendBlueprint)element).getStatus())) {
					return Activator.getDefault().getBundledImage("icons/obj16/Retired.gif");
				}
				return Activator.getDefault().getBundledImage("icons/obj16/Blueprint.gif");
			}
			else if (element instanceof RefexVersionBI) {
				RefexVersionBI<?> refex = (RefexVersionBI<?>) element;
				ComponentVersionBI referencedComponent = queryService.getComponent(refex.getReferencedComponentNid());
				return getImage(referencedComponent);
			}
		}
		
		return null;
	}

	@Override
	public String getColumnText(Object obj, int columnIndex) {
		Object element = unwrap(obj);
		
		try {
			if (element instanceof DescriptionVersionBI) {
				DescriptionVersionBI<?> description = (DescriptionVersionBI<?>) element;
	
				switch (columnIndex) {
					case 0: {
						return description.getText();
					}
					case 1: {
						return description.getLang();
					}
					case 2: {
						return Boolean.toString(description.isInitialCaseSignificant());
					}
					default:
						return null;
				}
			}
			else if (element instanceof DescriptionCAB) {
				DescriptionCAB description = (DescriptionCAB) element;
	
				switch (columnIndex) {
					case 0: {
						return description.getText();
					}
					case 1: {
						return description.getLang();
					}
					case 2: {
						return Boolean.toString(description.isInitialCaseSignificant());
					}
					default:
						return null;
				}
			}
			else if (element instanceof RelationshipVersionBI) {
				RelationshipVersionBI<?> relationship = (RelationshipVersionBI<?>) element;
	
				switch (columnIndex) {
					case 0: {
						return queryService.getConcept(relationship.getTypeNid()).getPreferredDescription().getText();
					}
					case 1: {
						return queryService.getConcept(relationship.getDestinationNid()).getPreferredDescription().getText();
					}
					case 2: {
						return Boolean.toString(relationship.isStated());
					}
					default:
						return null;
				}
			}
			else if (element instanceof RelationshipCAB) {
				RelationshipCAB relationship = (RelationshipCAB) element;
	
				switch (columnIndex) {
					case 0: {
						return queryService.getConcept(relationship.getTypeNid()).getPreferredDescription().getText();
					}
					case 1: {
						return queryService.getConcept(relationship.getTargetNid()).getPreferredDescription().getText();
					}
					case 2: {
						//TODO can't find isStated() in RelationshipCAB
						return Boolean.TRUE.toString();
					}
					default:
						return null;
				}
			}
			else if (element instanceof RefexVersionBI) {
				RefexVersionBI<?> refex = (RefexVersionBI<?>) element;
	
				switch (columnIndex) {
					case 0: {
						return getText(queryService.getComponent(refex.getReferencedComponentNid()));
					}
					default:
						return null;
				}
			}
			else if (element instanceof RefexCAB) {
				RefexCAB refex = (RefexCAB) element;
	
				switch (columnIndex) {
					case 0: {
						return getText(queryService.getComponent(refex.getReferencedComponentUuid()));
					}
					default:
						return null;
				}
			}
		}
		catch(Exception e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get column label text", e), 
					StatusManager.SHOW | StatusManager.LOG);
		}
		
		return getText(element);
	}

}