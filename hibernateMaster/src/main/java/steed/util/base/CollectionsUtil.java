package steed.util.base;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 数组,Collection,map工具类
 * @author 战马
 *
 */
public class CollectionsUtil {
	/**
	 * 数组类型
	 */
	public final static int array = 0;
	/**
	 * collection类型
	 */
	public final static int collection = 1;
	/**
	 * map类型
	 */
	public final static int map = 3;
	
	/**
	 * 是否是map或者collection
	 * @param object
	 * @return
	 */
	public static boolean isObjCollections(Object object){
		return object instanceof Collection || object instanceof Map;
	}
	public static boolean isCollectionsEmpty(Object obj){
		if (obj == null) {
			return true;
		}
		if (obj instanceof Collection) {
			return ((Collection<?>)obj).isEmpty();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>)obj).isEmpty();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>)obj).isEmpty();
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}
		throwNotCollectionsException(obj);
		return false;
	}
	private static void throwNotCollectionsException(Object obj) {
		String message;
		if (obj == null) {
			message = "所传入的参数不是集合类型！";
		}else {
			message = obj.getClass().getName()+"不是集合类型！";
		}
		throw new RuntimeException(message);
	}
	
	public List<Object> map2List(Map<?, ?> map){
		List<Object> returnList = new ArrayList<Object>();
		for (Object o:map.keySet()) {
			returnList.add(map.get(o));
		}
		return returnList;
	}
	
	public static boolean isObjArray(Object obj){
		return obj.getClass().isArray();
	}
	
	public static Object[] collections2Array(Object object){
		if (!isObjCollections(object)) {
			throwNotCollectionsException(object);
		}
		if (object == null) {
			return null;
		}
		if (isObjArray(object)) {
			return (Object[]) object;
		}
		if (object instanceof Collection) {
			Collection<?> tempCollection = (Collection<?>)object;
			return tempCollection.toArray();
		}else{
			Map<?, ?> tempMap = (Map<?, ?>)object;
			Object[] returned;
			returned = new Object[tempMap.size()];
			int index = 0;
			for(Object o:tempMap.keySet()){
				returned[index++] = tempMap.get(o);
			}
			return returned;
		}
	}
	
	public static int getCollectionLength(Object obj){
		if (obj instanceof Collection) {
			return ((Collection<?>)obj).size();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>)obj).size();
		}
		if (isObjArray(obj)) {
			return Array.getLength(obj);
		}
		throwNotCollectionsException(obj);
		return 0;
	}
	/*public static Object getItem(Object obj,int index){
		if (obj instanceof Collection) {
		}
		return null == list || list.size()==0;
	}*/
	
	public static int getCollectionType(Object obj){
		if (obj instanceof Collection) {
			return collection;
		}
		if (obj instanceof Map) {
			return map;
		}
		if (obj.getClass().isArray()) {
			return array;
		}
		throwNotCollectionsException(obj);
		return -1;
	}
	
	public static boolean isListEmpty(List<?> list){
		return null == list || list.size()==0;
	}
	public static boolean isMapEmpty(Map<?, ?> map){
		return null == map || map.isEmpty();
	}
	public static boolean isSetEmpty(Set<?> set){
		return null == set || set.isEmpty();
	}
}
