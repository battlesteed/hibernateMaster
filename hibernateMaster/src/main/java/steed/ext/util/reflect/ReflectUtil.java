package steed.ext.util.reflect;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.base.DateUtil;
import steed.ext.util.base.DomainUtil;
import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.LoggerFactory;

public class ReflectUtil {
	private static final Map<String, Field> fieldCache = new HashMap<>();
	private static final Map<String, Method> methodCache = new HashMap<>();
	
	public static void copySameField(Object copy,Object copyed){
		List<Field> fields = getNotFinalFields(copyed);
		for (Field f:fields) {
			try {
				f.setAccessible(true);
				Object value = f.get(copyed);
				if (value == null) {
					continue;
				}
				try {
					Field declaredField = getDeclaredField(copy.getClass(), f.getName());
					if (declaredField == null) {
						continue;
					}
					if (!declaredField.getType().isAssignableFrom(f.getType())) {
						value = ReflectUtil.convertFromString(declaredField.getType(), value.toString());
					}
					declaredField.setAccessible(true);
					declaredField.set(copy, value);
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				LoggerFactory.getLogger().debug("copyDomainSameField", e);
			} 
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T copyObj(T copyed){
		Class<?> clazz = copyed.getClass();
		T domain;
		try {
			domain = (T) clazz.newInstance();
			for (Field f:getNotFinalFields(copyed)) {
				f.setAccessible(true);
				try {
					Object value = f.get(copyed);
					//去除hibernate代理对象代码,防止反序列化时没有session导致抛异常
					/*if (value instanceof PersistentSet && (f.getType().isAssignableFrom(HashSet.class))) {
						PersistentSet temp = (PersistentSet) value;
						value = new HashSet<>();
						for (Object o:temp) {
							((HashSet)value).add(copyObj(o));
						}
					}*/
					f.set(domain, value);
				} catch (IllegalAccessException e) {
					LoggerFactory.getLogger().debug("copyObj", e);
				} 
			}
			return domain;
		} catch (InstantiationException | IllegalAccessException e) {
			LoggerFactory.getLogger().error("复制"+copyed.getClass().getName() + "失败!!",e);
			throw new RuntimeException(e);
		}
	}
	
	public static <T extends Date> T convertDate(Class<T> baseType,String str) {
		try {
			Date parseDate = DateUtil.parseDate(str);
			if (parseDate == null) {
				return null;
			}
			if (baseType == Date.class) {
				return (T) parseDate;
			}
			return baseType.getConstructor(long.class).newInstance(parseDate.getTime());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * 把string转换成基本ID类型
	 * @param baseType
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Serializable convertFromString(Class<?> baseType,String str){
		if (str == null) {
			return null;
		}
		//TODO 直接用struts内置日期转换器转换......我真他妈6,去除struts依赖
		//有sql.date util.date timestame等等....
		/*if (Date.class.isAssignableFrom(baseType)) {
			Map<String, Object> context = new HashMap<>();
			context.put(ActionContext.LOCALE, Locale.getDefault());
			return  (Serializable) new DateConverter().convertValue(context, null, null, null, str, baseType);
		}else{
			return (Serializable) new DefaultTypeConverter() {
			}.convertValue(str, baseType);
		}*/
		if (baseType == String.class) {
			return str;
		}
		if (Date.class.isAssignableFrom(baseType)) {
			return convertDate((Class<Date>) baseType, str);
		}
		if (baseType == Integer.class || baseType == int.class) {
			return Integer.parseInt(str);
		}
		if (baseType == Long.class || baseType == long.class) {
			return Long.parseLong(str);
		}
		if (baseType == Double.class || baseType == double.class) {
			return Double.parseDouble(str);
		}
		if (baseType == Float.class || baseType == float.class) {
			return Float.parseFloat(str);
		}
		if (baseType == Short.class || baseType == short.class) {
			return Short.parseShort(str);
		}
		if (baseType == Boolean.class || baseType == boolean.class) {
			return Boolean.parseBoolean(str);
		}
		return null;
		
	}
	public static boolean isClassBaseID(Class<?> clazz){
		return clazz == String.class || 
				clazz == Byte.class || clazz == byte.class || 
				clazz == Short.class || clazz == short.class ||
				clazz == Integer.class || clazz == int.class ||
				clazz == Float.class || clazz == float.class ||
				clazz == Boolean.class || clazz == boolean.class ||
				clazz == Character.class || clazz == char.class ||
				clazz == Double.class || clazz == double.class ||
				clazz == Long.class || clazz == long.class;
	}
	public static boolean isClassBaseType(Class<?> clazz){
		return clazz == Byte.class || clazz == byte.class || 
				clazz == Short.class || clazz == short.class ||
				clazz == Integer.class || clazz == int.class ||
				clazz == Float.class || clazz == float.class ||
				clazz == Boolean.class || clazz == boolean.class ||
				clazz == Character.class || clazz == char.class ||
				clazz == Double.class || clazz == double.class ||
				clazz == Long.class || clazz == long.class ||
				Date.class.isAssignableFrom(clazz);
	}
	
	public static boolean isObjBaseType(Object obj){
		return obj instanceof Byte || 
				obj instanceof Short ||
				obj instanceof Integer||
				obj instanceof Float ||
				obj instanceof Boolean ||
				obj instanceof Character ||
				obj instanceof Long ||
				obj instanceof Date ||
				obj instanceof Double;
	}
	
	
	public static boolean isFieldFinal(Field field){
		return (field.getModifiers()&Modifier.FINAL)==Modifier.FINAL;
	}
	
	public static Map<String, Object> field2Map(Object obj,boolean strictlyMode){
		Map<String, Object> map = new HashMap<>();
		return field2Map(0,obj, map, strictlyMode);
	}
	
	public static Map<String, Object> field2Map(Object obj,boolean strictlyMode, boolean onlyPutDatabaseField){
		Map<String, Object> map = new HashMap<>();
		return field2Map(0,obj, map, strictlyMode, onlyPutDatabaseField);
	}
	
	public static Map<String, Object> field2Map(Object obj){
		return field2Map(obj, false);
	}
	
	public static Map<String, Object> field2Map(int classDdeep,Object obj,Map<String, Object> map){
		return field2Map(classDdeep, obj, map, false);
	}
	
	public static Map<String, Object> field2Map(int classDdeep,Object obj,Map<String, Object> map,boolean strictlyMode){
		return field2Map(classDdeep, obj, map, strictlyMode, false);
	}
	
	public static Map<String, Object> field2Map(int classDdeep,Object obj,Map<String, Object> map,boolean strictlyMode,boolean onlyPutDatabaseField){
		Class<?> tempClass = obj.getClass();
		for (int i = 0; i < classDdeep; i++) {
			tempClass = tempClass.getSuperclass();
		}
		if (tempClass == Object.class) {
			return map;
		}
		for (Field temp:tempClass.getDeclaredFields()) {
			temp.setAccessible(true);
			try {
				Object obj2 = temp.get(obj);
				if (!isNull(strictlyMode, obj2)) {
					if (onlyPutDatabaseField && !DomainUtil.isDatabaseField(tempClass, temp)) {
						continue;
					}
					map.put(temp.getName(), obj2);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return field2Map(++classDdeep, obj, map, strictlyMode, onlyPutDatabaseField);
	}

	private static boolean isNull(boolean strictlyMode, Object obj2) {
		boolean isNull;
		if (strictlyMode) {
			isNull = obj2 == null;
		}else {
			isNull = BaseUtil.isObjEmpty(obj2);
		}
		return isNull;
	}
	
	
	public static void setValue(String fieldName,Object obj,Object value){
		try {
			Field declaredField = getDeclaredField(obj.getClass(), fieldName);
			if (value instanceof String) {
				value = ReflectUtil.convertFromString(declaredField.getType(), (String) value);
			}
			declaredField.setAccessible(true);
			declaredField.set(obj, value);
			return;
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LoggerFactory.getLogger().warn("把"+value+"设置到"+obj.getClass().getName()+"的"+fieldName+"字段失败", e);
		}
	}
	
	/**
	 * 获取obj里面的字段值
	 * @param fieldName 可以带点,比如"user.name"
	 * @param obj
	 * @return 若为null则说明没找到该字段或该字段值为null,请自行检讨,为什么会搞错字段名
	 * 
	 * @see #getValue(String, Object)
	 */
	public static Object getChainValue(String fieldName,Object obj){
		String[] fields = fieldName.split("\\.");
		Object target = obj;
		for(String temp:fields){
			target = getValue(temp, target);
			if (target == null) {
				return null;
			}
		}
		return target;
	}
	
	public static Object getValue(String fieldName,Object obj){
		try {
			Field declaredField = getDeclaredField(obj.getClass(), fieldName);
			declaredField.setAccessible(true);
			return declaredField.get(obj);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LoggerFactory.getLogger().warn("获取"+obj.getClass().getName()+"的"+fieldName+"字段失败-->", e.getMessage());
		}
		return null;
	}
	
	public static Object newInstance(String className){
		try {
			return Class.forName(className).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			LoggerFactory.getLogger().error(className+"实例化失败！！",e);
			throw new RuntimeException(className+"实例化失败！！",e);
		} 
	}
	
	public static <T extends Annotation> T getAnnotation(Class<T> annotationClass,
			Class<? extends Object> objClass,Field field){
		T annotation = field.getAnnotation(annotationClass);
		if (annotation != null) {
			return annotation;
		}
		
		String name = field.getName();
		String fieldGetterName = StringUtil.getFieldGetterName(name);
		T methodAnnotation = getMethodAnnotation(annotationClass, objClass, fieldGetterName); 
		if (methodAnnotation == null) {
			String fieldIsMethodName = StringUtil.getFieldIsMethodName(name);
			methodAnnotation = getMethodAnnotation(annotationClass, objClass, fieldIsMethodName);
		}
		
		return methodAnnotation;
		
	}

	private static <T extends Annotation> T getMethodAnnotation(Class<T> annotationClass,
			Class<? extends Object> objClass, String fieldIsMethodName) {
		try {
			Method declaredMethod3 = getDeclaredMethod(objClass,fieldIsMethodName);
			if (declaredMethod3 != null) {
				declaredMethod3.setAccessible(true);
				T annotation3 = declaredMethod3.getAnnotation(annotationClass);
				if (annotation3 != null) {
					return annotation3;
				}
			}
		} catch (SecurityException e) {
		}
		return null;
	}
	public static List<Annotation> getAnnotations(Class<? extends Object> objClass,Field field){
		List<Annotation> list = new ArrayList<>();
		Collections.addAll(list, field.getDeclaredAnnotations());
		String name = field.getName();
		String fieldGetterName = StringUtil.getFieldGetterName(name);
		try {
			Method declaredMethod = objClass.getDeclaredMethod(fieldGetterName);
			Collections.addAll(list, declaredMethod.getAnnotations());
		} catch (NoSuchMethodException | SecurityException e) {
		}
		
		String fieldIsMethodName = StringUtil.getFieldIsMethodName(name);
		try {
			Method declaredMethod3 = objClass.getDeclaredMethod(fieldIsMethodName);
			Collections.addAll(list, declaredMethod3.getAnnotations());
		} catch (NoSuchMethodException | SecurityException e) {
		}
		return list;
		
	}
	
	/**
	 * 获取类的方法(包括父类)
	 * @param clazz
	 * @param methodName
	 * @return 方法不存在则返回null
	 */
	public static Method getDeclaredMethod(Class<?> clazz,String methodName){
		return getMethod(clazz, methodName, false);
	}
	/**
	 * 获取类的方法(包括父类)
	 * @param clazz
	 * @param methodName
	 * @param onlyPublic 是否只获取public方法
	 * @return 方法不存在则返回null
	 */
	public static Method getMethod(Class<?> clazz,String methodName,boolean onlyPublic){
//		Class<?> target = clazz;
		Method declaredMethod = null;
		String key = clazz.getName()+"."+methodName;
		if (methodCache.containsKey(key)) {
			declaredMethod = methodCache.get(key);
			if (declaredMethod == null) {
				return null;
			}
		}
		while(clazz != Object.class && declaredMethod == null){
			try {
				declaredMethod = clazz.getDeclaredMethod(methodName);
			} catch (NoSuchMethodException | SecurityException e) {
				//BaseUtil.getLogger().info("获取方法出错!{}",e.getMessage());
			}
			clazz = clazz.getSuperclass();
		}
		if (declaredMethod != null && onlyPublic && !Modifier.isPublic(declaredMethod.getModifiers())) {
			return null;
		}
//		BaseUtil.getLogger().info("{}没有{}方法",new Object[]{target.getName(),methodName});
		return declaredMethod;
	}
	
	public static Object getFieldValueByGetter(Object obj,String fieldName){
		if (obj == null) {
			return null;
		}
		try {
			String fieldGetterName = StringUtil.getFieldGetterName(fieldName);
			Method declaredMethod = getDeclaredMethod(obj.getClass(),fieldGetterName);
			if (declaredMethod != null) {
				return declaredMethod.invoke(obj);
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LoggerFactory.getLogger().warn("获取字段值出错!{}",e);
		}
		
		try {
			String fieldIsMethodName = StringUtil.getFieldIsMethodName(fieldName);
			Method declaredMethod =getDeclaredMethod(obj.getClass(),fieldIsMethodName);
			if (declaredMethod != null) {
				return declaredMethod.invoke(obj);
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LoggerFactory.getLogger().info("获取字段之出错!{}",e.getMessage());
		}
		
		LoggerFactory.getLogger().info("{}没有{} getter方法,通过gett获取值失败",obj.getClass().getName(),fieldName);
		return null;
		
	}
	
	
	public static Class<?> getGenericType(Field f) {
		try {
	        Type mapMainType = f.getGenericType();
	        if (mapMainType instanceof ParameterizedType) {   
	            ParameterizedType parameterizedType = (ParameterizedType)mapMainType;   
	            // 获取泛型类型的泛型参数   
	            Type[] types = parameterizedType.getActualTypeArguments();   
	            return (Class<?>) types[0];
	        } else {   
	        	throw new RuntimeException(String.format("在%s字段找不到泛型信息！！", f.getName())); 
	        }   
		}  catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Field> getNotFinalFields(Object object){
		List<Field> fieldList = getAllFields(object);
		Iterator<Field> iterator = fieldList.iterator();
		while (iterator.hasNext()) {
			if (isFieldFinal(iterator.next())) {
				iterator.remove();
			}
		}
		return fieldList;
	}
	@SuppressWarnings("unchecked")
	public static List<Field> getAllFields(Object object){
		List<Field> fieldList = new ArrayList<>();
		Class<? extends Object> class1;
		if (object instanceof Class) {
			class1 = (Class<? extends Object>) object;
		}else {
			class1 = object.getClass();
		}
		while (class1 != Object.class) {
			Collections.addAll(fieldList, class1.getDeclaredFields());
			class1 = class1.getSuperclass();
		}
		return fieldList;
	}
	/**
	 * 获取clazz里面public的字段
	 * @param clazz
	 * @param fieldName 不能带点
	 * @return
	 * 
	 * @see #getDeclaredField(Class, String)
	 */
	public static Field getField(Class<?> clazz,String fieldName){
		return getField(clazz, fieldName, true);
	}
	
	/**
	 * 获取clazz里面的字段
	 * @param clazz
	 * @param fieldName 可以带点,比如"user.name"
	 * @return 若为null则说明没找到该字段,请自行检讨,为什么会搞错字段名
	 * 
	 * @see #getDeclaredField(Class, String)
	 */
	public static ReflectResult getChainField(Class<?> clazz,String fieldName){
		String[] fields = fieldName.split("\\.");
		Field field = null;
		Class<?> target = clazz;
		ReflectResult result = new ReflectResult();
		for(String temp:fields){
			field = getDeclaredField(target, temp);
			if (field == null) {
				return null;
			}
			result.setTarget(target);
			target = field.getType();
		}
		result.setField(field);
		return result;
	}
	
	
	/**
	 * 获取clazz里面的字段
	 * @param clazz
	 * @param fieldName 不能带点
	 * @return
	 * 
	 * @see #getChainField(Class, String)
	 */
	public static Field getDeclaredField(Class<?> clazz,String fieldName){
		return getField(clazz, fieldName, false);
	}
	
	/**
	 * 获取clazz里面的字段
	 * @param clazz
	 * @param fieldName 不能带点
	 * @param onlyPublic 是否只获取public的方法
	 * @return
	 * 
	 * @see #getChainField(Class, String)
	 */
	public static Field getField(Class<?> clazz,String fieldName, boolean onlyPublic){
		Class<?> rawClazz = clazz;
		Field field = null;
		String key = rawClazz.getName()+"."+fieldName;
		if (fieldCache.containsKey(key)) {
			field = fieldCache.get(key);
			if (field == null) {
				return null;
			}
		}
		while (clazz != Object.class && field == null) {
			try {
				field = clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException | SecurityException e) {
				clazz = clazz.getSuperclass();
			}
		}
		fieldCache.put(key, field);
		if (field != null && onlyPublic && !Modifier.isPublic(field.getModifiers())) {
			return null;
		}
		return field;
	}
	
	public static Method getDeclaredMethod(Class<?> clazz,String name, Class<?>... parameterTypes){
		while (clazz != Object.class) {
			try {
				return clazz.getDeclaredMethod(name, parameterTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}
	
	
	/**
	 * 判断该类型是否是数据库基本数据类型
	 * @param obj
	 * @return
	 */
	public static boolean isObjBaseData(Object obj){
		return isClassBaseData(obj.getClass());
	}
	/**
	 * 判断该类型是否是数据库基本数据类型
	 * @param clazz
	 * @return
	 */
	public static boolean isClassBaseData(Class<?> clazz){
		return isClassBaseType(clazz) || clazz == String.class || Date.class.isAssignableFrom(clazz);
	}
}
