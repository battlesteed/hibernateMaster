package steed.hibernatemaster.filter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.util.DaoUtil;

/**
 * 查询过滤器,<code>{@link DaoUtil#putField2Map(Object) }</code>的拦截器,
 * 通过设置<code>{@link Config#queryFilterManager }</code>,来全局实现数据权限控制等,例如:
 * 一个多公司系统,用户只能看自己公司的数据,可以在beforePutField2Map方法,
 * 把domain的公司字段值设置为当前登陆用户所属公司,这样就只能查出当前公司的数据了
 * 
 * @author 战马
 * @see DaoUtil#putField2Map(Object, Map, String, boolean)
 */
public interface QueryFilter<T>{
	/**
	 * 
	 * @param domain 要查询的domain
	 * @param map domain字段容器,用来生成hql或sqlwhere部分的查询条件
	 * @param prefixName 前缀,当put user里面的school时,prefixName="user.",原理比较复杂,具体可以看源码
	 * @param getFieldByGetter 是否用Getter方法来获取字段值,若传false,则用field.getValue直接获取字段值
	 * @return 是否继续执行DaoUtil#putField2Map(Object, Map, String, boolean)
	 * 
	 * @see DaoUtil#putField2Map(Object, Map, String, boolean)
	 */
	public boolean beforePutField2Map(T domain,Map<String, Object> map,String prefixName,boolean getFieldByGetter);
	public default void afterPutField2Map(T domain,Map<String, Object> map,String prefixName,boolean getFieldByGetter) {};
	
	/**
	 * 获取要过滤的实体类
	 * @return 要过滤的实体类,返回null表示禁用该过滤器
	 */
	public default Class<? extends BaseDatabaseDomain> getFiltedClass() {
		Class<? extends QueryFilter<? extends BaseDatabaseDomain>> clazz = (Class<? extends QueryFilter<? extends BaseDatabaseDomain>>) getClass();
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
		
}
