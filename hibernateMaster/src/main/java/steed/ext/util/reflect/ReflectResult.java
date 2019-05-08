package steed.ext.util.reflect;

import java.lang.reflect.Field;

import steed.hibernatemaster.domain.BaseDomain;

public class ReflectResult extends BaseDomain{
	private static final long serialVersionUID = -8711808757385063141L;
	private Field field;
	private Class<?> target;
	private Object value;
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public Class<?> getTarget() {
		return target;
	}
	public void setTarget(Class<?> target) {
		this.target = target;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}
