package steed.hibernatemaster.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;

/**
 * 查询构建器,复杂的查询(&gt; ,&lt; , in等查询条件),请用该类构建
 * 
 * @author battlesteed
 *
 */
public class QueryBuilder {
	public static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
	private Map<String,Object> queryMap;
	
	private Class<? extends BaseDomain> target;
	
	private String[] groupBy;
	
	private String[] selectedField;
	
	public QueryBuilder(){
		queryMap = new HashMap<>();
	}
	
	public QueryBuilder(Map<String, Object> queryMap) {
		super();
		this.queryMap = queryMap;
	}

	public Class<? extends BaseDomain> getTarget() {
		return target;
	}

	public QueryBuilder(BaseDomain where){
		queryMap = DaoUtil.putField2Map(where);
		target = where.getClass();
	}
	
	public QueryBuilder( Class<? extends BaseDomain> target){
		this();
		this.target = target;
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
	public QueryBuilder setTarget(Class<? extends BaseDatabaseDomain> target){
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
	 * 
	 * @see #addNotEqual(String, Object, int)
	 */
	public QueryBuilder addNotEqual(String key,Object value){
		queryMap.put(key+"_not_equal_1", value);
		return this;
	}
	
	/**
	 * 添加不等于查询条件 生成的hql将包含 "model.key != :value "这个条件
	 * 直接调用两次{@link #addNotEqual(String, Object)},无法添加user != 2 and user != 3 的情况
	 * 最后生成的hql 只有一个 != ,而用该方法,有多少个index,就会生成多少个 != 
	 * 
	 * @param key 字段名
	 * @param value 值
	 * @param index 第几个 != 条件
	 * @return this
	 */
	public QueryBuilder addNotEqual(String key,Object value,int index){
		queryMap.put(key+"_i_"+index+"_not_equal_1", value);
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
	 *  一对多中,一关联的set或list等NotIn条件也可以使用该方法添加,但是调用该方法前得保证 {@link #target } 不为null,
	 *  否则该方法无法判断NotIn的是普通字段或是一对多中实体类
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public <T> QueryBuilder addNotIn(String key,T... value){
		if (BaseUtil.isObjEmpty(value)) {
			logger.info("addNotIn 数组为空,不添加addNotIn条件");
			return this;
		}
		queryMap.put(key+ getNotInSubfix(key), value);
		return this;
	}

	private String getNotInSubfix(String key) {
		String notin = DaoUtil.notIN;
		if (target != null) {
			Field field = ReflectUtil.getDeclaredField(target, key);
			if (Collection.class.isAssignableFrom(field.getType()) || field.getType().isArray()) {
				notin = DaoUtil.manyNotIN;
			}
		}
		return notin;
	}
	
	/**
	 * 添加not in查询条件 生成的hql将包含 "model.key not in( :list )"这个条件
	 * @param key 字段名
	 * @param list 值
	 * @return this
	 */
	public <T> QueryBuilder addNotIn(String key,List<T> list){
		if (list == null || list.isEmpty()) {
			logger.info("addNotIn 数组为空,不添加addNotIn条件");
			return this;
		}
		queryMap.put(key + getNotInSubfix(key), list);
		return this;
	}
	
	/**
	 * 添加not in查询条件 生成的hql将包含 "model.key not in( :set )"这个条件
	 * @param key 字段名
	 * @param set 值
	 * @return this
	 */
	public <T> QueryBuilder addNotIn(String key,Set<T> set){
		if (set == null || set.isEmpty()) {
			logger.info("addNotIn 数组为空,不添加addNotIn条件");
			return this;
		}
		queryMap.put(key + getNotInSubfix(key), set);
		return this;
	}
	
	/**
	 * 添加in查询条件 生成的hql将包含 "model.key in( :value )"这个条件
	 * @param key 字段名
	 * @param value 值
	 * @return this
	 */
	public <T> QueryBuilder addIn(String key,Object value){
		if (BaseUtil.isObjEmpty(value)) {
			if (Config.sqlWallOpen) {
				queryMap.put(DaoUtil.rawHqlPart, String.format(" %s = '1' and %s = '2' ", key,key));
			}else {
				queryMap.put(DaoUtil.rawHqlPart, " 1 = 2 ");
			}
		}else {
			queryMap.put(key+"_not_join", value);
		}
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
	 * 删除{@link #addNotNull(String, boolean)}方法添加的非空查询条件
	 * @param key 字段名
	 * 
	 * @return this
	 */
	public QueryBuilder removeNotNull(String key){
		queryMap.remove(key+"_not_null");
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
	 * 添加原生的hql where部分,不会被框架转义,处理等(除了'domain.'会被替换成'实体类简称.'外),生成的hql将包含 'where 其它where条件 and + rawHqlPart'这个条件
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
