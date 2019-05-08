package steed.hibernatemaster.domain;

import java.io.Serializable;
import java.lang.reflect.Field;

import steed.ext.util.reflect.ReflectUtil;


/**
 * 联合主键ID基础类,重写hashCode和equals方法，子类无需再重写，
 * 联合主键ID类必须继承该类。
 * @author 战马
 *
 */
public class DomainID implements Serializable{
	private static final long serialVersionUID = -4546569714287162857L;

	@Override
	public int hashCode() {
		int hashCode = 0;
		for(Field f:ReflectUtil.getNotFinalFields(this)){
			try {
				f.setAccessible(true);
				Object temp = f.get(this);
				if (temp != null) {
					hashCode += temp.hashCode();
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} 
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		for(Field f:ReflectUtil.getNotFinalFields(this)){
			try {
				f.setAccessible(true);
				Object temp = f.get(this);
				Object temp2 = f.get(obj);
				if (temp == null && temp2 == null) {
					continue;
				}else if (temp != null && temp2 != null) {
					boolean tempEquals = temp.equals(temp2);
					if (!tempEquals) {
						return false;
					}
				}else {
					return false;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			} 
		}
		return true;
	}
}
