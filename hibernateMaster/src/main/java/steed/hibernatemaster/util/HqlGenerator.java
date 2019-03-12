package steed.hibernatemaster.util;

import java.util.Map;

import steed.hibernatemaster.Config;

/**
 * HqlGenerator 顾名思义,hql生成器 可以通过{@link Config#defaultHqlGenerator} 配置默认的hql生成器
 * 通过BaseRelationalDatabaseDomain#setHqlPersonalGenerator(HqlGenerator)
 * 可以设置自定义的hql生成器
 * 
 * @author 战马 battle_steed@qq.com
 * @see SimpleHqlGenerator
 */
public interface HqlGenerator {
	StringBuffer appendHqlWhere(String domainSimpleName, StringBuffer hql,Map<String, Object> map);
	
	default StringBuffer afterHqlGenered(String domainSimpleName, StringBuffer hql,Map<String, Object> map){
		return hql;
	};
}
