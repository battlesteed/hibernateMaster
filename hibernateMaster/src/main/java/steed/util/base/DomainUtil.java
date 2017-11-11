package steed.util.base;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.IdClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import steed.hibernatemaster.annotation.FuzzyQuery;
import steed.hibernatemaster.annotation.FuzzyQuery.FuzzyQuerystrategy;
import steed.hibernatemaster.domain.BaseDomain;
import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.domain.UnionKeyDomain;
import steed.hibernatemaster.exception.DomainIdAnnotationNotFoundException;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.reflect.ReflectUtil;
/**
 * 实体类工具类
 * @author 战马
 *
 */
public class DomainUtil{
	private static Logger logger = LoggerFactory.getLogger(DomainUtil.class);
	/**
	 * 获取实体类的hashcode
	 * @param baseDomain
	 * @return
	 */
	public static int domainHashCode(BaseDomain baseDomain){
		if (baseDomain == null) {
			return 0;
		}
		try {
			Object tempObject = getDomainId(baseDomain);
			if (tempObject == null) {
				return -1;
			}else {
				return tempObject.hashCode();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	/**
	 * 净化,把domain关联的实体类设置成空,一般用于给前端返回json数据
	 * 
	 */
	public static void purify(BaseDomain domain){
		List<Field> allFields = ReflectUtil.getAllFields(domain);
		for(Field temp:allFields){
			if (BaseDomain.class.isAssignableFrom(temp.getType())) {
				temp.setAccessible(true);
				try {
					temp.set(domain, null);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					steed.util.logging.LoggerFactory.getLogger().warn("净化实体类出错",e);
				}
			}
		}
	}
	
	
	/**
	 * 获取实体类ID的名字
	 * @param clazz
	 * @return
	 */
	public static String getDomainIDName(Class<? extends BaseDomain> clazz){
		/**
		 * 如果是联合主键实体类则返回domainID，因为联合主键类必须实现getDomainID()方法
		 */
		if (clazz.getAnnotation(IdClass.class) != null) {
			return "domainID";
		}
		try {
			Method m = getIDmethod(clazz);
			if (m != null) {
				String name = m.getName();
				if (name.startsWith("is")) {
					return name.substring(2, 3).toLowerCase() + name.substring(3) ;
				}else {
					return name.substring(3, 4).toLowerCase() + name.substring(4) ;
				}
			}
		} catch (Exception e) {
			throw createFindIdException(clazz, e);
		}
		
		try {
			Field f = getIDfield(clazz);
			if (f != null) {
				return f.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw createFindIdException(clazz, e);
		}  
		throw createIDNotfoundException(clazz);
	}
	/**
	 * 获取实体类ID Class
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> getDomainIDClass(Class<? extends BaseDomain> clazz){
		/**
		 * 如果是联合主键实体类则返回domainID，因为联合主键类必须实现getDomainID()方法
		 */
		IdClass annotation = clazz.getAnnotation(IdClass.class);
		if (annotation != null) {
			return annotation.value();
		}
		try {
			Field f = ReflectUtil.getDeclaredField(clazz, getDomainIDName(clazz));
			if (f != null) {
				return (Class<? extends Serializable>) f.getType();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw createFindIdException(clazz, e);
		}  
		throw createIDNotfoundException(clazz);
	}
	
	public static BaseDomain fillID2Domain(Serializable id,BaseDomain baseDomain){
		Class<? extends BaseDomain> domainClass = baseDomain.getClass();
		Field iDfield = getIDfield(domainClass);
		try {
			if (iDfield != null) {
				iDfield.setAccessible(true);
				iDfield.set(baseDomain, id);
				return baseDomain;
			}else {
				Method iDmethod = getIDmethod(domainClass);
				if (iDmethod != null) {
						domainClass.getMethod(iDmethod.getName().replaceFirst("get", "set"),iDmethod.getReturnType()).invoke(baseDomain, id);
						return baseDomain;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(domainClass.getName()+"中没有id字段或ID的set方法！！");
		}
		throw new RuntimeException(domainClass.getName()+"中没有id字段或ID的set方法！！");
	}
	
	/**
	 * 获取实体类主键
	 * @param baseDomain
	 * @return
	 */
	public static Serializable getDomainId(BaseDomain baseDomain){
		if (baseDomain instanceof UnionKeyDomain) {
			return ((UnionKeyDomain)baseDomain).getDomainID();
		}
		Class<? extends BaseDomain> clazz = baseDomain.getClass();
		try {
			Method m = getIDmethod(clazz);
			if (m != null) {
   				return (Serializable) m.invoke(baseDomain);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw createFindIdException(clazz, e);
		}
		
		try {
			Field f = getIDfield(clazz);
			if (f != null) {
				return (Serializable) f.get(baseDomain);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw createFindIdException(clazz, e);
		}  
		throw createIDNotfoundException(clazz);
	}

	private static RuntimeException createFindIdException(Class<? extends BaseDomain> clazz,
			Exception e) {
		logger.error("在"+
				clazz.getName()+"找到含有"+Id.class.getName()+"注解的get方法,或字段时出现异常。", e);
		return new RuntimeException("在"+
				clazz.getName()+"找到含有"+Id.class.getName()+"注解的get方法,或字段时出现异常。", e);
	}

	private static DomainIdAnnotationNotFoundException createIDNotfoundException(Class<? extends Object> clazz) {
		logger.error("在"+clazz.getName()+"中找不到含有"+Id.class.getName()+"注解的get方法,或字段。");
		DomainIdAnnotationNotFoundException exception = new DomainIdAnnotationNotFoundException("在"+
				clazz.getName()+"中找不到含有"+Id.class.getName()+"注解的get方法,或字段。");
		exception.printStackTrace();
		return exception;
	}
	
	/**
	 * 判断实体类和另一个对象是否相等
	 * @param baseDomain
	 * @param obj2
	 * @return
	 */
	public static boolean domainEquals(BaseDomain baseDomain,Object obj2){
		if (baseDomain == null) {
			return obj2 == null;
		}else if (obj2 == null) {
			return false;
		}
//		Class<? extends BaseDomain> clazz = baseDomain.getClass();
		Class<? extends Object> clazz2 = obj2.getClass();
		if (BaseDomain.class.isAssignableFrom(clazz2)) {
			/*Method iDmethod = getIDmethod(clazz);
			if (iDmethod != null) {
				Object tempObject;
				try {
					tempObject = iDmethod.invoke(baseDomain);
					return BaseUtil.objectEquals(iDmethod.invoke(obj2),tempObject);
				} catch (Exception e) {
					e.printStackTrace();
					throw createFindIdException(clazz, e);
				} 
			}
			Field f = getIDfield(clazz);
			if (f != null) {
				try {
					Object tempObject = f.get(baseDomain);
					return BaseUtil.objectEquals(f.get(obj2),tempObject);
				} catch (Exception e) {
					e.printStackTrace();
					throw createFindIdException(clazz, e);
				} 
			}
			createIDNotfoundException(clazz2);*/
			
			Serializable domainId = getDomainId(baseDomain);
			Serializable id2 = getDomainId((BaseDomain) obj2);
			
			if (domainId == null) {
				return id2 == null;
			}else if(id2 == null){
				return false;
			}else {
				return id2.equals(domainId);
			}
			
		}else if(ReflectUtil.isObjBaseData(obj2)){
			Serializable domainId = getDomainId(baseDomain);
			return obj2.equals(domainId);
		}else {
			return false;
		}
		
	}
	
	
	/**
	 * 获取ID字段
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Field getIDfield(Class<? extends BaseDomain> clazz) {
		if (clazz.getAnnotation(IdClass.class) != null) {
			StringBuffer sb = new StringBuffer("按照约定含有");
			sb.append(IdClass.class.getName());
			sb.append("注解的domain不能有ID字段!!!只能有getDomainID()方法。");
			throw new RuntimeException(sb.toString());
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field f:fields) {
			f.setAccessible(true);
			Annotation temp = f.getAnnotation(Id.class);
			if (temp == null) {
				continue;
			}
			return f;
		}
		if (clazz == BaseDomain.class) {
			return null;
		}
		return getIDfield((Class<? extends BaseDomain>)clazz.getSuperclass());
	}
	
	@SuppressWarnings("unchecked")
	private static Method getIDmethod(Class<? extends BaseDomain> clazz) {
		if (clazz.getAnnotation(IdClass.class) != null) {
			try {
				return clazz.getDeclaredMethod("getDomainID");
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("含有"+IdClass.class.getName()+
						"的domain必须实现"+UnionKeyDomain.class.getName()+"接口!!!",e);
			}
		}
		
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m:methods) {
			Annotation aa = m.getAnnotation(Id.class);
			if (aa == null) {
				continue;
			}
			return m;
		}
		if (clazz == BaseDomain.class) {
			return null;
		}
		return getIDmethod((Class<? extends BaseDomain>)clazz.getSuperclass());
	}
	/**
	 * 把fill中不为null的字段填充给filled
	 * @param filled
	 * @param fill
	 * @param fieldsNotSkip 即使为null也不跳过的字段,如果没有可以传个null
	 * @return 
	 * @return filled
	 */
	public static <T> T fillDomain(T filled,T fill,Collection<String> fieldsNotSkip){
		return fillDomain(filled, fill, fieldsNotSkip, false);
	}
	/**
	 * 把fill中不为null的字段填充给filled
	 * @param filled
	 * @param fill
	 * @param fieldsNotSkip 即使为null也不跳过的字段,如果没有可以传个null
	 *  @param strictlyMode 严格模式，如果为true则 字段==null才算空，
	 * 	否则调用BaseUtil.isObjEmpty判断字段是否为空
	 * @see BaseUtil#isObjEmpty
	 * @return 
	 * @return filled
	 */
	public static <T> T fillDomain(T filled,T fill,Collection<String> fieldsNotSkip,boolean strictlyMode){
		List<Field> fields = ReflectUtil.getAllFields(fill);
		try {
			if(fieldsNotSkip == null){
				fieldsNotSkip = new ArrayList<String>();
			}
			for (Field f:fields) {
				if(ReflectUtil.isFieldFinal(f)){
					continue;
				}
				f.setAccessible(true);
				Object temp = ReflectUtil.getFieldValueByGetter(fill, f.getName());
				if (temp == null) {
					temp = f.get(fill);
				}
				boolean isNull;
				if (strictlyMode) {
					isNull = temp == null;
				}else {
					isNull = BaseUtil.isObjEmpty(temp);
				}
				if (!isNull || fieldsNotSkip.contains(f.getName())) {
					try {
						f.set(filled, temp);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			return filled;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
			return null;
		} 
	}
	
	private static void fuzzyQueryInitialize(String prefix,BaseDomain obj,boolean skipId,String ...fieldsSkip){
		List<String> fieldsSkipList = new ArrayList<String>();
		Collections.addAll(fieldsSkipList, fieldsSkip);
		List<Field> allFields = ReflectUtil.getAllFields(obj);
		for (Field f:allFields) {
			try {
				if (DaoUtil.isSelectIndex(f.getName()) > 0) {
					continue;
				}
				f.setAccessible(true);
				Object value = f.get(obj);
				if (!fieldsSkipList.contains(prefix+f.getName())) {
					if (value instanceof String && !StringUtil.isStringEmpty((String) value)) {
						if (skipId && ReflectUtil.getAnnotation(Id.class, obj.getClass(), f) != null) {
							continue;
						}
						FuzzyQuery annotation = ReflectUtil.getAnnotation(FuzzyQuery.class, obj.getClass(), f);
						if (annotation == null) {
							f.set(obj, "%"+value+"%");
						}else {
							FuzzyQuerystrategy value2 = annotation.value();
							switch (value2) {
							case left:
								f.set(obj, "%"+value);
								break;
							case right:
								f.set(obj, value+"%");
								break;

							default:
								break;
							}
						}
					}else if (value instanceof BaseDomain && BaseUtil.isObjEmpty(DomainUtil.getDomainId((BaseDomain) value))) {
						fuzzyQueryInitialize(f.getName()+".", (BaseDomain) value,skipId, fieldsSkip);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 初始化查询实体类模糊查询
	 * @param obj
	 * @param fieldsSkip 跳过的字段，不跳请传空
	 * 
	 * @see FuzzyQuery
	 */
	public static void fuzzyQueryInitialize(BaseDomain obj,String ...fieldsSkip){
		fuzzyQueryInitialize(obj, true, fieldsSkip);
	}
	/**
	 * 初始化查询实体类模糊查询
	 * @param obj
	 * @param fieldsSkip 跳过的字段，不跳请传空
	 * 
	 * @see FuzzyQuery
	 */
	public static void fuzzyQueryInitialize(BaseDomain obj,boolean skipId,String ...fieldsSkip){
		fuzzyQueryInitialize("", obj,skipId, fieldsSkip);
	}
	
	/**
	 *  把obj中非空字段放到map
	 * @param obj
	 * @return map
	 */
	public static Map<String, Object> putField2Map(Object obj) {
		Map<String, Object> map = new HashMap<>();
		putField2Map(obj, map, "");
		return map;
	}
	
	/**
	 * 把obj中非空字段放到map
	 * @return
	 */
	public static void putField2Map(Object obj,Map<String, Object> map,String prefixName) {
		putField2Map(obj, map, prefixName, true);
	}
	public static void putField2Map(Object obj,Map<String, Object> map,String prefixName,boolean getFieldByGetter) {
		try {
 			List<Field> Fields = ReflectUtil.getNotFinalFields(obj);
			for (Field f:Fields) {
				String fieldName = f.getName();
				
				Object value = null;
				if (getFieldByGetter) {
					value = ReflectUtil.getFieldValueByGetter(obj, fieldName);
				}
				
				if(value == null){
					f.setAccessible(true);
					value = f.get(obj);
				}
				if (value == null) {
					continue;
				}
				if (!(value instanceof BaseRelationalDatabaseDomain) ) {
						map.put(prefixName + fieldName, value);
				}else {
					putField2Map(value, map,prefixName + fieldName +".");
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			steed.util.logging.LoggerFactory.getLogger().debug("putField2Map出错",e);
		}
	}
	
	public static void setDomainId(BaseDomain baseDomain,
			Serializable serializable) {
		Class<? extends BaseDomain> class1 = baseDomain.getClass();
		String domainIDName = getDomainIDName(class1);
		String fieldSetterName = StringUtil.getFieldSetterName(domainIDName);
		try {
			Method method = class1.getMethod(fieldSetterName, serializable.getClass());
			method.invoke(baseDomain, serializable);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(class1+"中没有"+fieldSetterName+"方法", e);
		} catch (SecurityException
					| IllegalAccessException 
					| InvocationTargetException 
					| IllegalArgumentException e) {
			throw new RuntimeException(class1+"中的"+fieldSetterName+"方法有误", e);
		} 
	}
}
