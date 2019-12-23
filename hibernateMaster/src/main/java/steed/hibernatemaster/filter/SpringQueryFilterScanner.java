package steed.hibernatemaster.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import steed.ext.util.logging.LoggerFactory;

/**
 * spring查询过滤器扫描器
 * @author 战马
 *
 */
public class SpringQueryFilterScanner implements QueryFilterManager{
	public static final steed.ext.util.logging.Logger logger = LoggerFactory.getLogger(SpringQueryFilterScanner.class);
	private Map<QueryFilter<?>, Class<?>> listeners;
	private Map<Class<?>, QueryFilter<?>[]> listenerMap = new HashMap<Class<?>, QueryFilter<?>[]>();
	private String[] packages;
	
	public SpringQueryFilterScanner(String... packages) {
		super();
		this.packages = packages;
		listeners = scan();
	}

	public Map<QueryFilter<?>, Class<?>> scan() {
		if (listeners != null) {
			return listeners;
		}
		listeners = new HashMap<QueryFilter<?>, Class<?>>();
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
//		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		provider.addIncludeFilter(new AssignableTypeFilter(QueryFilter.class));
//		provider.setResourcePattern("**/*.class");
		Set<BeanDefinition> findCandidateComponents = new HashSet<>();
		for (String p:packages) {
			findCandidateComponents.addAll(provider.findCandidateComponents(p));
		}
		for (BeanDefinition temp:findCandidateComponents) {
			Class<? extends QueryFilter<?>> listenerClass;
			try {
				listenerClass = (Class<? extends QueryFilter<?>>) Class.forName(temp.getBeanClassName());
				QueryFilter<?> newInstance = listenerClass.newInstance();
				Class<?> listenClass = newInstance.getFiltedClass();
				if (listenClass != null) {
					logger.debug("添加查询过滤器"+listenerClass.getName());
					listeners.put(newInstance, listenClass);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				logger.error("扫描查询过滤器出错!",e);
			}
		}
		return listeners;
	}


	@Override
	public QueryFilter<?>[] getFilters(Class<?> clazz) {
		if (listenerMap.get(clazz) == null) {
			List<QueryFilter<?>> list = new ArrayList<QueryFilter<?>>();
			Set<Entry<QueryFilter<?>, Class<?>>> entrySet = listeners.entrySet();
			for (Entry<QueryFilter<?>, Class<?>> e:entrySet) {
				if (e.getValue().isAssignableFrom(clazz)) {
					list.add(e.getKey());
				}
			}
			listenerMap.put(clazz, list.toArray(new QueryFilter[list.size()] ));
		}
		return listenerMap.get(clazz);
	}
	
}
