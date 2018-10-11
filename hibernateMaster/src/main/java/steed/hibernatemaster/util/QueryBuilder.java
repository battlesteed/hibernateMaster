package steed.hibernatemaster.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;

/**
 * 查询构建器,复杂的查询(&gt; ,&lt; , in等查询条件),请用该类构建
 * 
 * @author battlesteed
 *
 */
public class QueryBuilder {
	private Map<String,Object> queryMap;
	
	private Class<? extends BaseDatabaseDomain> target;
	
	private String[] groupBy;
	
	private String[] selectedField;
	
	public QueryBuilder(){
		queryMap = new HashMap<>();
	}
	
	public QueryBuilder(BaseDomain domain){
		queryMap = DaoUtil.putField2Map(domain);
	}
	
	public QueryBuilder(BaseDatabaseDomain domain){
		queryMap = DaoUtil.putField2Map(domain);
		target = domain.getClass();
	}
	
	/**
	 * 设置hql的groupBy部分
	 * 
	 * @param groupBy
	 */
	private void setGroupBy(String... groupBy) {
		this.groupBy = groupBy;
	}
	
	/**
	 * 设置要select的字段
	 * 
	 * @param selectedField 
	 */
	private void setSelectedField(String... selectedField) {
		this.selectedField = selectedField;
	}

	/***
	 * 设置要查询的实体类
	 * 
	 * @return this
	 */
	private QueryBuilder setTarget(Class<? extends BaseDatabaseDomain> target){
		this.target = target;
		return this;
	}
	
	
	/**
	 * 添加普通查询条件, 生成的hql将包含 "model.key = :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public QueryBuilder add(String key,Object value){
		queryMap.put(key, value);
		return this;
	}
	
	/**
	 * 添加不等于查询条件 生成的hql将包含 "model.key != :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public QueryBuilder addNotEqual(String key,Object value){
		queryMap.put(key+"_not_equal_1", value);
		return this;
	}
	
	/**
	 * 添加自定义的hql生成器
	 * 
	 * @param generator 自定义的hql生成器
	 * 
	 * @return this
	 */
	public QueryBuilder addHqlGenerator(HqlGenerator generator){
		queryMap.put(DaoUtil.personalHqlGeneratorKey, generator);
		return this;
	}
	
	/*
	 * 添加in查询条件 生成的hql将包含 "model.key in( :value )"这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 *
	public QueryBuilder addIn(String key,Object value){
		queryMap.put(key+"_not_join", value);
		return this;
	}*/
	
	/**
	 * 添加not in查询条件 生成的hql将包含 "model.key not in( :value )"这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public <T> QueryBuilder addNotIn(String key,T... value){
		queryMap.put(key+"_not_in_1", value);
		return this;
	}
	/**
	 * 添加not in查询条件 生成的hql将包含 "model.key not in( :list )"这个条件
	 * @param key 字段名
	 * @param list 值
	 * @return this
	 */
	public <T> QueryBuilder addNotIn(String key,List<T> list){
		queryMap.put(key+"_not_in_1", list);
		return this;
	}
	/**
	 * 添加not in查询条件 生成的hql将包含 "model.key not in( :set )"这个条件
	 * @param key 字段名
	 * @param set 值
	 * @return this
	 */
	public <T> QueryBuilder addNotIn(String key,Set<T> set){
		queryMap.put(key+"_not_in_1", set);
		return this;
	}
	/**
	 * 添加in查询条件 生成的hql将包含 "model.key in( :value )"这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public <T> QueryBuilder addIn(String key,Object value){
		queryMap.put(key+"_not_join", value);
		return this;
	}
	
	/**
	 * 添加非空查询条件 生成的hql将包含 "model.key is null "(notNull为false)或者 "model.key is not null "(notNull为true)这个条件
	 * @param key 字段名
	 * @param notNull  "model.key is null "(notNull为false)或者 "model.key is not null "(notNull为true)这个条件
	 * 
	 * @return this
	 */
	public QueryBuilder addNotNull(String key,boolean notNull){
		queryMap.put(key+"_not_null", notNull);
		return this;
	}
	
	/**
	 * 添加小于查询条件 生成的hql将包含 "model.key &lt; :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 * 
	 * @see #addLessThan(String, Object)
	 */
	public QueryBuilder addLessThanNoEqual(String key,Object value){
		queryMap.put(key+"_lessThan", value);
		return this;
	}
	
	/**
	 * 添加大于查询条件 生成的hql将包含 "model.key &gt; :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 * 
	 * @see #addMoreThan(String, Object)
	 */
	public QueryBuilder addMoreThanNoEqual(String key,Object value){
		queryMap.put(key+"_greaterThan", value);
		return this;
	}
	/**
	 * 添加原生的hql where部分,不会被框架转义,处理等(除了外'domain.'会被替换成'实体类简称.'外),生成的hql将包含 'where 其它where条件 and + rawHqlPart'这个条件
	 * 
	 * @param rawHqlPart 如," (domain.name like '%admin' or domain.phone = '10086')",
	 * 			生成的hql将包含 'where 其它where条件 and (domain.name like '%admin' or domain.phone = '10086')' 这个条件
	 * 
	 * @return this
	 */
	public QueryBuilder addRawHqlPart(String rawHqlPart){
		queryMap.put(DaoUtil.rawHqlPart, rawHqlPart);
		return this;
	}
	
	/**
	 * 添加hql where 部分or分组查询条件,例如调了addOrGroup("group1","name","admin");
	 * 	addOrGroup("group1","name","battlesteed");addOrGroup("ab","phone","10086");
	 * 
	 * 生成的hql将包括where 其它查询条件 and (domain.phone = 10086) and (domain.name = battlesteed or domain.name = admin) 
	 * 
	 * @param groupId 同一个分组id的查询添加将会放到同一个()里面
	 * @param key 字段名
	 * @param value 值
	 * @return
	 */
	private QueryBuilder addGroup(String groupId,String key,Object value){
		queryMap.put(key+"_"+groupId+DaoUtil.orGroup, value);
		return this;
	}
	
	/**
	 * 添加小于或等于查询条件 生成的hql将包含 "model.key &lt;= :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 * 
	 * @see #addLessThanNoEqual(String, Object)
	 */
	public QueryBuilder addLessThan(String key,Object value){
		queryMap.put(key+"_max_1", value);
		return this;
	}
	/**
	 * 添加大于或等于查询条件 生成的hql将包含 "model.key &gt;= :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 * 
	 * @see #addMoreThanNoEqual(String, Object)
	 */
	public QueryBuilder addMoreThan(String key,Object value){
		queryMap.put(key+"_min_1", value);
		return this;
	}
	
	/**
	 * 获取查询条件map
	 * 
	 * @return DaoUtil中的查询(where)条件
	 * 
	 * @see #getWhere()
	 */
	@Deprecated
	public Map<String, Object> getQueryMap() {
		return queryMap;
	}
	/**
	 * 获取查询条件map
	 * 
	 * @return DaoUtil中的查询(where)条件
	 */
	public Map<String, Object> getWhere() {
		return queryMap;
	}
	
}
