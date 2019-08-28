package steed.hibernatemaster.listener;

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
import steed.hibernatemaster.domain.BaseDatabaseDomain;

/**
 * 单数据库实体类扫描器
 * @author 战马
 *
 */
public class SpringCRUDListenerScanner implements CRUDListenerManager{
	public static final steed.ext.util.logging.Logger logger = LoggerFactory.getLogger(SpringCRUDListenerScanner.class);
	private Map<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>> listeners;
	private Map<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>[]> listenerMap = new HashMap<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>[]>();
	private String[] packages;
	
	public SpringCRUDListenerScanner(String... packages) {
		super();
		this.packages = packages;
		listeners = scan();
	}

	public Map<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>> scan() {
		if (listeners != null) {
			return listeners;
		}
		listeners = new HashMap<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>>();
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
//		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		provider.addIncludeFilter(new AssignableTypeFilter(CRUDListener.class));
//		provider.setResourcePattern("**/*.class");
		Set<BeanDefinition> findCandidateComponents = new HashSet<>();
		for (String p:packages) {
			findCandidateComponents.addAll(provider.findCandidateComponents(p));
		}
		for (BeanDefinition temp:findCandidateComponents) {
			Class<? extends CRUDListener<? extends BaseDatabaseDomain>> listenerClass;
			try {
				listenerClass = (Class<? extends CRUDListener<? extends BaseDatabaseDomain>>) Class.forName(temp.getBeanClassName());
				CRUDListener<? extends BaseDatabaseDomain> newInstance = listenerClass.newInstance();
				Class<? extends BaseDatabaseDomain> listenClass = newInstance.getListenClass();
				if (listenClass != null) {
					logger.debug("添加crud监听器"+listenerClass.getName());
					listeners.put(listenClass, newInstance);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				logger.error("扫描crud监听器出错!",e);
			}
		}
		return listeners;
	}


	@Override
	public CRUDListener<? extends BaseDatabaseDomain>[] getListeners(Class<? extends BaseDatabaseDomain> clazz) {
		if (listenerMap.get(clazz) == null) {
			List<CRUDListener<? extends BaseDatabaseDomain>> list = new ArrayList<CRUDListener<? extends BaseDatabaseDomain>>();
			Set<Entry<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>>> entrySet = listeners.entrySet();
			for (Entry<Class<? extends BaseDatabaseDomain>, CRUDListener<? extends BaseDatabaseDomain>> e:entrySet) {
				if (e.getKey().isAssignableFrom(clazz)) {
					list.add(e.getValue());
				}
			}
			listenerMap.put(clazz, list.toArray(new CRUDListener[list.size()] ));
		}
		return listenerMap.get(clazz);
	}
	
}
