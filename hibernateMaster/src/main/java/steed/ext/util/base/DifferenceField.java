package steed.ext.util.base;

import java.lang.reflect.Field;

public class DifferenceField {
	private String fieldName;
	private Class<?> target;
	private Object newField;
	private Object oldField;
	private Field field;
	
	
	public DifferenceField(String fieldName, Class<?> target, Object newField, Object oldField,Field field) {
		super();
		this.fieldName = fieldName;
		this.target = target;
		this.newField = newField;
		this.oldField = oldField;
		this.field = field;
	}
	public String getFieldName() {
		return fieldName;
	}
	public Class<?> getTarget() {
		return target;
	}
	public Object getNewField() {
		return newField;
	}
	public Object getOldField() {
		return oldField;
	}
	public Field getField() {
		return field;
	}
	
}