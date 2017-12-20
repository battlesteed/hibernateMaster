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
	 * 添加普通查询条件, 生成的hql将包含 "model.kee = :value "这个条件
	 * @param key
	 * @param value
	 * @return
	 */
	public QueryBuilder add(String key,Object value){
		queryMap.put(key, value);
		return this;
	}
	
	/**
	 * 添加不等于查询条件 生成的hql将包含 "model.kee != :value "这个条件
	 * @param key
	 * @param value
	 * @return
	 */
	public QueryBuilder addNotEqual(String key,Object value){
		queryMap.put(key+"_not_equal_1", value);
		return this;
	}
	/**
	 * 添加in查询条件 生成的hql将包含 "model.kee in( :value )"这个条件
	 * @param key
	 * @param value
	 * @return
	 */
	public QueryBuilder addIn(String key,Object value){
		queryMap.put(key+"_not_join", value);
		return this;
	}
	
	/**
	 * 添加非空查询条件 生成的hql将包含 "model.kee is null "(notNull为false)或者 "model.kee is not null "(notNull为true)这个条件
	 * @param key
	 * @param value
	 * @return
	 */
	public QueryBuilder addNotNull(String key,boolean notNull){
		queryMap.put(key+"_not_null", notNull);
		return this;
	}
	
	/**
	 * 添加小于查询条件 生成的hql将包含 "model.kee <= :value "这个条件
	 * @param key
	 * @param value
	 * @return
	 */
	public QueryBuilder addLessThan(String key,Object value){
		queryMap.put(key+"_max_1", value);
		return this;
	}
	/**
	 * 添加大于于查询条件 生成的hql将包含 "model.kee >= :value "这个条件
	 * @param key
	 * @param value
	 * @return
	 */
	public QueryBuilder addMoreThan(String key,Object value){
		queryMap.put(key+"_min_1", value);
		return this;
	}

	public Map<String, Object> getQueryMap() {
		return queryMap;
	}
	
}
