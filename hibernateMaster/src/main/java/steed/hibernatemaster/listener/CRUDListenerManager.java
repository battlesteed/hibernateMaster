package steed.hibernatemaster.listener;

import steed.hibernatemaster.domain.BaseDatabaseDomain;

/**
 * 增删查监听器管理器,管理,扫描增删查监听器
 * @author battlesteed
 *
 */
public interface CRUDListenerManager {
	/**
	 * 获取增删查改监听器
	 * @return 所有要监听clazz的增删查改监听器,没有返回null或空数组
	 */
	public CRUDListener<? extends BaseDatabaseDomain>[] getListeners(Class<? extends BaseDatabaseDomain> clazz);
	
}
