package gov.va.isaac.mdht.otf.refset;

import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_float.RefexFloatAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;


public enum RefsetAttributeType {
	Component,
	Concept, 
	Description, 
	Relationship,
	RefsetMember,
	
	String,
	Integer,
	Long,
	Boolean,
	Float;

	@SuppressWarnings("rawtypes")
	public static Class getPrimitiveForType(RefsetAttributeType type) {
		if (String == type) {
			return String.class;
		}
		else if (Integer == type) {
			return Integer.class;
		}
		else if (Long == type) {
			return Long.class;
		}
		else if (Boolean == type) {
			return Boolean.class;
		}
		else if (Float == type) {
			return Float.class;
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Class getOtfClassForType(RefsetAttributeType type) {
		if (Component == type) {
			return ComponentVersionBI.class;
		}
		else if (Concept == type) {
			return ConceptVersionBI.class;
		}
		else if (Description == type) {
			return DescriptionVersionBI.class;
		}
		else if (Relationship == type) {
			return RelationshipVersionBI.class;
		}
		else if (RefsetMember == type) {
			return ConceptVersionBI.class;
		}
		
		else if (String == type) {
			return RefexStringAnalogBI.class;
		}
		else if (Integer == type) {
			return RefexIntAnalogBI.class;
		}
		else if (Long == type) {
			return RefexLongAnalogBI.class;
		}
		else if (Boolean == type) {
			return RefexBooleanAnalogBI.class;
		}
		else if (Float == type) {
			return RefexFloatAnalogBI.class;
		}
		
		return null;
	}
}
