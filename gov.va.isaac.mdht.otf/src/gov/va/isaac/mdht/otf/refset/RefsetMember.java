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

import gov.va.isaac.mdht.otf.services.ConceptQueryService;
import gov.va.isaac.mdht.otf.services.TerminologyStoreFactory;

import java.io.IOException;

import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_float.RefexFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

/**
 * 
 * @author <a href="mailto:dcarlson@xmlmodeling.com">Dave Carlson (XMLmodeling.com)</a> 
 */
public class RefsetMember {
	private ConceptQueryService queryService = TerminologyStoreFactory.INSTANCE.createConceptQueryService();

	private RefsetDescriptor refsetDescriptor = null;
	
//	private RefexType refexType = null;
	
	private ConceptVersionBI refsetConcept = null;

	//TODO one List of extension attributes, mix of primitive and components
	// 0 index must be referencedComponent
	// List<Object> extensionAttributes = new ArrayList<Object)();
	
	private ComponentVersionBI referencedComponent = null;
	
	private Object extValue = null;
	
	private ComponentVersionBI[] extComponents = new ComponentVersionBI[3];
	
	public RefsetMember(RefsetDescriptor refsetDescriptor) {
		this.refsetDescriptor = refsetDescriptor;
	}

	public RefsetMember(ConceptVersionBI refset) {
		this.refsetConcept = refset;
	}

	public RefsetMember(RefexVersionBI<?> refex) {
//		refexType = refex.getRefexType();
		refsetConcept = queryService.getConcept(refex.getAssemblageNid());
		referencedComponent = queryService.getConcept(refex.getReferencedComponentNid());

		if (refex instanceof RefexNidVersionBI<?>) {
			RefexNidVersionBI<?> nidRefex = (RefexNidVersionBI<?>) refex;
			extComponents[0] = queryService.getConcept(nidRefex.getNid1());
		}
		if (refex instanceof RefexNidNidVersionBI<?>) {
			RefexNidNidVersionBI<?> nidRefex = (RefexNidNidVersionBI<?>) refex;
			extComponents[1] = queryService.getConcept(nidRefex.getNid2());
		}
		if (refex instanceof RefexNidNidNidVersionBI<?>) {
			RefexNidNidNidVersionBI<?> nidRefex = (RefexNidNidNidVersionBI<?>) refex;
			extComponents[2] = queryService.getConcept(nidRefex.getNid3());
		}

		if (refex instanceof RefexStringAnalogBI<?>) {
			RefexStringAnalogBI<?> stringRefex = (RefexStringAnalogBI<?>) refex;
			extValue = stringRefex.getString1();
		}
		else if (refex instanceof RefexIntAnalogBI<?>) {
			RefexIntAnalogBI<?> intRefex = (RefexIntAnalogBI<?>) refex;
			extValue =  new Integer(intRefex.getInt1());
		}
		else if (refex instanceof RefexLongAnalogBI<?>) {
			RefexLongAnalogBI<?> longRefex = (RefexLongAnalogBI<?>) refex;
			extValue =  new Long(longRefex.getLong1());
		}
		else if (refex instanceof RefexFloatVersionBI<?>) {
			RefexFloatVersionBI<?> floatRefex = (RefexFloatVersionBI<?>) refex;
			extValue =  new Float(floatRefex.getFloat1());
		}
		else if (refex instanceof RefexBooleanAnalogBI<?>) {
			RefexBooleanAnalogBI<?> booleanRefex = (RefexBooleanAnalogBI<?>) refex;
			extValue =  new Boolean(booleanRefex.getBoolean1());
		}
	}

	public RefexCAB createBlueprint() throws IllegalArgumentException, IOException, InvalidCAB, ContradictionException {
		RefexCAB refexCAB = new RefexCAB(getRefexType(), referencedComponent.getPrimordialUuid(), refsetConcept.getPrimordialUuid(), 
				IdDirective.GENERATE_REFEX_CONTENT_HASH, RefexDirective.INCLUDE);
		
		setValueProperty(refexCAB);
		setComponentProperties(refexCAB);
		
		return refexCAB;
	}
	
	public boolean validateRefex() throws Exception {
		boolean isValid = true;

		if (refsetConcept == null) {
			throw new Exception("Refset concept must be specified.");
		}
		if (referencedComponent == null) {
			throw new Exception("Referenced component must be specified.");
		}
		
		try {
			// validate attribute combination is supported by OTF
			getRefexType();
		} catch (IllegalArgumentException e) {
			throw new Exception(e.getMessage());
		}
		
		return isValid;
	}

	public ConceptVersionBI getRefsetConcept() {
		return refsetConcept;
	}

	public ComponentVersionBI getReferencedComponent() {
		return referencedComponent;
	}

	public void setReferencedComponent(ComponentVersionBI component) {
		referencedComponent = component;
	}

	public RefexType getRefexType() {
//		if (refexType == null) {
//			refexType = deriveTypeFromContent();
//		}
//		return refexType;
		
		RefexType type = deriveTypeFromContent();
		
		return type;
	}

	public static RefsetAttributeType[] getPrimitiveTypes() {
		RefsetAttributeType[] primitiveTypes = { RefsetAttributeType.String, RefsetAttributeType.Boolean,
				RefsetAttributeType.Integer, RefsetAttributeType.Long, RefsetAttributeType.Float};
		return primitiveTypes;
	}
	
    public static boolean isPrimitiveType(RefsetAttributeType attributeType) {
    	for (RefsetAttributeType type : getPrimitiveTypes()) {
			if (type == attributeType) {
				return true;
			}
		}
    	return false;
    }
    
    public static RefsetAttributeType getPrimitiveType(RefexVersionBI<?> refex) {
    	RefsetAttributeType type = null;
		if (refex instanceof RefexStringVersionBI<?>) {
			type = RefsetAttributeType.String;
		}
		else if (refex instanceof RefexIntVersionBI<?>) {
			type = RefsetAttributeType.Integer;
		}
		else if (refex instanceof RefexLongVersionBI<?>) {
			type = RefsetAttributeType.Long;
		}
		else if (refex instanceof RefexFloatVersionBI<?>) {
			type = RefsetAttributeType.Float;
		}
		else if (refex instanceof RefexBooleanVersionBI<?>) {
			type = RefsetAttributeType.Boolean;
		}
    	
    	return type;
    }
	
	public Object getExtensionValue() {
		return extValue;
	}
	
	public void setExtensionValue(Object value) {
		extValue = value;
	}

	public void setExtensionValue(Object value, RefsetAttributeType attributeType) throws NumberFormatException {
		String valueString = value.toString();
		if (RefsetAttributeType.String == attributeType) {
			extValue = valueString;
		}
		else if (RefsetAttributeType.Boolean == attributeType) {
			extValue = new Boolean(valueString);
		}
		else if (RefsetAttributeType.Integer == attributeType) {
			extValue = new Integer(valueString);
		}
		else if (RefsetAttributeType.Long == attributeType) {
			extValue = new Long(valueString);
		}
		else if (RefsetAttributeType.Float == attributeType) {
			extValue = new Float(valueString);
		}
	}
	
	public ComponentVersionBI[] getExtensionComponents() {
		return extComponents;
	}

	protected RefexType deriveTypeFromContent() {
		RefexType type = RefexType.MEMBER;
		StringBuffer typeString = new StringBuffer();
		
		if (extComponents[2] != null) {
			typeString.append("CID_CID_CID");
		}
		else if (extComponents[1] != null) {
			typeString.append("CID_CID");
		}
		else if (extComponents[0] != null) {
			typeString.append("CID");
		}
		
		if (extValue instanceof String) {
			if (typeString.length() > 0) {
				typeString.append("_");
			}
			typeString.append("STR");
		}
		else if (extValue instanceof Boolean) {
			if (typeString.length() > 0) {
				typeString.append("_");
			}
			typeString.append("BOOLEAN");
		}
		else if (extValue instanceof Long) {
			if (typeString.length() > 0) {
				typeString.append("_");
			}
			typeString.append("LONG");
		}
		else if (extValue instanceof Integer) {
			if (typeString.length() > 0) {
				typeString.append("_");
			}
			typeString.append("INT");
		}
		else if (extValue instanceof Float) {
			if (typeString.length() > 0) {
				typeString.append("_");
			}
			typeString.append("FLOAT");
		}
		
		if (typeString.length() > 0) {
			type = RefexType.valueOf(typeString.toString());
			
			if (type == null) {
				type = RefexType.MEMBER;
			}
		}
		
		return type;
	}
	
	protected void setValueProperty(RefexCAB refexCAB) {
		if (extValue instanceof String) {
			refexCAB.put(ComponentProperty.STRING_EXTENSION_1, (String)extValue);
		}
		else if (extValue instanceof Boolean) {
			refexCAB.put(ComponentProperty.BOOLEAN_EXTENSION_1, (Boolean)extValue);
		}
		else if (extValue instanceof Integer) {
			refexCAB.put(ComponentProperty.INTEGER_EXTENSION_1, (Integer)extValue);
		}
		else if (extValue instanceof Long) {
			refexCAB.put(ComponentProperty.LONG_EXTENSION_1, (Long)extValue);
		}
		else if (extValue instanceof Float) {
			refexCAB.put(ComponentProperty.FLOAT_EXTENSION_1, (Float)extValue);
		}
	}
	
	protected void setComponentProperties(RefexCAB refexCAB) {
		if (referencedComponent != null) {
			refexCAB.setReferencedComponentUuid(referencedComponent.getPrimordialUuid());
		}
		
		if (extComponents[0] != null) {
			refexCAB.put(ComponentProperty.COMPONENT_EXTENSION_1_ID, extComponents[0].getPrimordialUuid());
		}
		if (extComponents[1] != null) {
			refexCAB.put(ComponentProperty.COMPONENT_EXTENSION_2_ID, extComponents[1].getPrimordialUuid());
		}
		if (extComponents[2] != null) {
			refexCAB.put(ComponentProperty.COMPONENT_EXTENSION_3_ID, extComponents[2].getPrimordialUuid());
		}
	}
}
