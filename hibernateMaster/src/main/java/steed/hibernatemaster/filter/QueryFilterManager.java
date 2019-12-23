package steed.hibernatemaster.filter;

/**
 * 增删查过滤器管理器,管理,扫描增删查过滤器
 * @author battlesteed
 *
 */
public interface QueryFilterManager {
	/**
	 * 获取增删查改过滤器
	 * @return 所有要过滤clazz的增删查改过滤器,没有返回null或空数组
	 */
	public QueryFilter<?>[] getFilters(Class<?> clazz);
	
}
