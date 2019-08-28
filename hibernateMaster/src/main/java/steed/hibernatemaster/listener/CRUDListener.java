package steed.hibernatemaster.listener;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import steed.hibernatemaster.domain.BaseDatabaseDomain;
/**
 * 增删查改监听器,若要阻止对应的数据库操作,在对应方法抛异常即可
 * @author battlesteed
 *
 * @param <T>
 */
public interface CRUDListener<T extends BaseDatabaseDomain> {
	/**
	 * 实体类实例保存前回调方法,只有调用 {@link BaseDatabaseDomain#save()}方法才会触发该方法,请注意
	 * @param domain 要保存的实体类实例
	 */
	public default void beforSave(T domain) {};
	/**
	 * 实体类实例保存后回调方法,只有调用 {@link BaseDatabaseDomain#save()}方法才会触发该方法,请注意
	 * @param domain 保存的实体类实例
	 */
	public default void afterSave(T domain) {};
	
	/**
	 * 实体类删除前回调方法,只有调用 {@link BaseDatabaseDomain#delete()}方法才会触发该方法,请注意
	 * @param domain 要删除的实体类实例
	 */
	public default void beforeDelete(T domain) {}
	/**
	 * 实体类删除后回调方法,只有调用 {@link BaseDatabaseDomain#delete()}方法才会触发该方法,请注意
	 * @param domain 删除的实体类实例
	 */
	public default void afterDelete(T domain) {}
//	/**
//	 * 实体类删除前回调方法,只有调用 {@link BaseDatabaseDomain#delete()}方法才会触发该方法,请注意
//	 * @param clazz 要删除的实体类
//	 * @param id id
//	 */
//	public default void beforeDelete(Class<T> clazz,Serializable id) {}
//	/**
//	 * 实体类删除后回调方法,只有调用 {@link BaseDatabaseDomain#delete()}方法才会触发该方法,请注意
//	 * @param clazz 删除的实体类
//	 * @param id id
//	 */
//	public default void afterDelete(Class<T> clazz,Serializable id) {}
	
	/**
	 * 获取要监听的实体类
	 * @return 要监听的实体类,返回null表示禁用该监听器
	 */
	public default Class<? extends BaseDatabaseDomain> getListenClass() {
		Class<? extends CRUDListener<? extends BaseDatabaseDomain>> clazz = (Class<? extends CRUDListener<? extends BaseDatabaseDomain>>) getClass();
		Type[] genericSuperclass = clazz.getGenericInterfaces();
		for (Type temp:genericSuperclass) {
			if (temp instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) temp;
				if (BaseDatabaseDomain.class.isAssignableFrom((Class<?>) (parameterizedType.getActualTypeArguments()[0]))) {
					return (Class<? extends BaseDatabaseDomain>) parameterizedType.getActualTypeArguments()[0];
				}
			}
		}
		return null;
	}
//	
//	/**
//	 * 
//	 * @return 要监听的实体类,返回null表示禁用该监听器
//	 */
//	public Class<? extends BaseDatabaseDomain> listenTarget();
}
