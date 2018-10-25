package steed.hibernatemaster.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import steed.util.base.CollectionsUtil;
import steed.util.base.StringUtil;


/**
 * SimpleHqlGenerator 顾名思义,简单的hql生成器
 * 可以通过config.properties的dao.HqlGenerator配置默认的hql生成器
 * 通过steed.domain.BaseRelationalDatabaseDomain.setHqlPersonalGenerator(HqlGenerator)
 * 可以设置自定义的hql生成器
 * 
 * @author 战马
 * Email battle_steed@163.com
 * @see steed.hibernatemaster.domain.BaseRelationalDatabaseDomain#setPersonalHqlGenerator(HqlGenerator)
 */
public class SimpleHqlGenerator implements HqlGenerator{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public StringBuffer appendHqlWhere(String domainSimpleName, StringBuffer hql, Map<String, Object> map) {
		if (CollectionsUtil.isCollectionsEmpty(map)) {
			return hql;
		}
		hql.append(" ");
		appendPersonalWhere(domainSimpleName, hql, map);
		if (hql.indexOf(" domain.") > -1) {
			hql = new StringBuffer(hql.toString().replace(" domain.", " "+domainSimpleName+"."));
		}
		hql.append(" ");
		List<String> removedEntry = new ArrayList<String>();
		Map<String, Object> put = new HashMap<>();
		for(Entry<String, Object> e:map.entrySet()){
			if (hql.indexOf(":"+dealDot(e.getKey())+" ") >= 0) {
				continue;
			}
			appendSingleWhereCondition(domainSimpleName, hql, removedEntry, map, e,put);
		}
		map.putAll((Map) put);
		for (String temp:removedEntry) {
			map.remove(temp);
		}
		return hql;
	}
	/**
	 * append 单个自定义 where 条件
	 * @param domainSimpleName 操作的实体类简称
	 * @param hql 当前已经生成的部分hql
	 * @param query 查询条件,比如query参数里面有一个&lt;name,false&gt;的查询条件,
	 * 	然后你生成了domainSimpleName.name is not null 的hql,这个hql是没有:name这个参数的,要把他移除掉,这时候就要调removedEntry.add("name");通知框架把name参数移除掉
	 * 
	 */
	protected void appendPersonalWhere(String domainSimpleName, StringBuffer hql,Map<String, Object> query) {
		
	}

	/**
	 * append 单个 where 条件
	 * @param domainSimpleName
	 * @param hql
	 * @param query
	 * @param removedEntry 你要移除的查询条件,比如query参数里面有一个&lt;name,false&gt;的查询条件,
	 * 	然后你生成了domainSimpleName.name is not null 的hql,这个hql是没有:name这个参数的,要把他移除掉,这时候就要调removedEntry.add("name");通知框架把name参数移除掉
	 * @param e
	 * @param put 你要往
	 */
	protected void appendSingleWhereCondition(String domainSimpleName, StringBuffer hql,
			List<String> removedEntry, Map<String, Object> query,Entry<String, Object> e,Map<String, Object> put) {
		String key = e.getKey();
		if (e.getValue() != null && (e.getValue() instanceof Collection || e.getValue().getClass().isArray()) 
				&& !key.endsWith("_not_join") && !key.endsWith("_not_in_1")) {
			//TODO 添加不联表的in和not in
			boolean isNotIn = key.endsWith("_not_in_1");
			String joinedName = domainSimpleName.replace("\\.", "_1_")
					+ key.replace("_not_in_1", "");
			
			StringBuffer innerJoinSB = new StringBuffer(" inner join ");
			innerJoinSB.append(domainSimpleName);
			innerJoinSB.append(".");
			innerJoinSB.append(key.replace("_not_in_1", ""));
			
			innerJoinSB.append(" ");
			innerJoinSB.append(joinedName);
			innerJoinSB.append(" ");
			//not in 和 in 都要 append inner join xxxx 如果之前append了就不再append
			if (hql.indexOf(innerJoinSB.toString()) == -1) {
				int index = hql.indexOf(" where ");
				hql.insert(index, innerJoinSB);
			}
			
			hql.append("and ");
			hql.append(joinedName);
			if (isNotIn) {
				hql.append(" not in (");
			}else {
				hql.append(" in (");
			}
			hql.append(":");
			hql.append(dealDot(key));
			hql.append(") ");
		}else if(key.endsWith("_not_join")){
			hql.append("and ");
			hql.append(domainSimpleName);
			hql.append(".");
			hql.append(key.replace("_not_join", ""));
			hql.append(" in (:");
			hql.append(dealDot(key));
			hql.append(") ");
		}else if(key.endsWith("_not_in_1")){
			hql.append("and ");
			hql.append(domainSimpleName);
			hql.append(".");
			hql.append(key.replace("_not_in_1", ""));
			hql.append(" not in (:");
			hql.append(dealDot(key));
			hql.append(") ");
		}else  if(key.endsWith(DaoUtil.rawHqlPart)){
			hql.append("and ");
			hql.append(e.getValue());
			removedEntry.add(key);
		}else {
			hql.append("and ");
			hql.append(domainSimpleName);
			hql.append(".");
			if (key.endsWith("_lessThan")) {
				hql.append(key.replace("_lessThan", ""));
				hql.append("< :");
				hql.append(dealDot(key));
				hql.append(" ");
			}else if (key.endsWith("_greaterThan")) {
				hql.append(key.replace("_greaterThan", ""));
				hql.append("> :");
				hql.append(dealDot(key));
				hql.append(" ");
			}else if (key.endsWith("_max_1")) {
				hql.append(key.replace("_max_1", ""));
				hql.append("<=:");
				hql.append(dealDot(key));
				hql.append(" ");
			}else if (key.endsWith("_min_1")) {
				hql.append(key.replace("_min_1", ""));
				hql.append(">=:");
				hql.append(dealDot(key));
				hql.append(" ");
			}else if (key.endsWith("_like_1")) {
				hql.append(key.replace("_like_1", ""));
				hql.append(" like :");
				hql.append(dealDot(key));
				hql.append(" ");
			}else if(key.endsWith("_not_equal_1")){
				String replace = key.replace("_not_equal_1", "");
				if (replace.lastIndexOf("_i_") != -1) {
					replace = replace.substring(0, replace.lastIndexOf("_i_"));
				}
				hql.append(replace);
				hql.append(" != :");
				hql.append(dealDot(key));
				hql.append(" ");
			}else if(key.endsWith("_not_null")){
				hql.append(key.replace("_not_null", ""));
				hql.append(" is ");
				if((Boolean) e.getValue()){
					hql.append("not ");
				}
				hql.append("null ");
				//is null 或 is not null 均不用
				//设置参数，所以remove掉
				removedEntry.add(key);
			}else if(key.endsWith("_not_compile_param")){
				hql.append(key.replace("_not_compile_param", ""));
				hql.append(" = ");
				hql.append(e.getValue());
				removedEntry.add(key);
			}else {
				hql.append(key);
				if (e.getValue() instanceof String 
						&& !StringUtil.isStringEmpty((String) e.getValue())
						&& ((String) e.getValue()).contains("%")) {
					hql.append(" like :");
				}else {
					hql.append(" = :");
				}
				hql.append(dealDot(key));
				hql.append(" ");
			}
			
		}
	}
	private String dealDot(String key) {
		return key.replace(".", "__");
	}

}
