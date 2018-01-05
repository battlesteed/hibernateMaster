package steed.hibernatemaster.util;

import java.util.HashMap;
import java.util.Map;

import steed.hibernatemaster.domain.BaseDomain;

public class QueryBuilder {
	private Map<String,Object> queryMap = new HashMap<>();
	public QueryBuilder(){
		queryMap = new HashMap<>();
	}
	public QueryBuilder(BaseDomain domain){
		queryMap = DaoUtil.putField2Map(domain);
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
	
	/**
	 * 添加in查询条件 生成的hql将包含 "model.key in( :value )"这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public QueryBuilder addIn(String key,Object value){
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
	 * 添加小于查询条件 生成的hql将包含 "model.key &lt;= :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public QueryBuilder addLessThan(String key,Object value){
		queryMap.put(key+"_max_1", value);
		return this;
	}
	/**
	 * 添加大于于查询条件 生成的hql将包含 "model.key &gt;= :value "这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public QueryBuilder addMoreThan(String key,Object value){
		queryMap.put(key+"_min_1", value);
		return this;
	}
	
	/**
	 * 获取查询条件map
	 * 
	 * @return DaoUtil中的查询(where)条件
	 */
	public Map<String, Object> getQueryMap() {
		return queryMap;
	}
	
}
