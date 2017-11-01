package steed.hibernatemaster.util;

import java.util.Map;

/**
 * HqlGenerator 顾名思义,hql生成器
 * 可以通过config.properties的dao.HqlGenerator配置默认的hql生成器
 * 
 * @author 战马
 * Email battle_steed@163.com
 * @see steed.util.dao#HqlGenerator
 */
public interface HqlGenerator {
	StringBuffer appendHqlWhere(String domainSimpleName, StringBuffer hql,Map<String, Object> map);
}
