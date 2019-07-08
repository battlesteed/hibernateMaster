package steed.hibernatemaster.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.domain.DomainScanner;

/**
 * 单数据库实体类扫描器
 * @author 战马
 *
 */
public class SpringDomainScanner implements DomainScanner{
	private String[] packages;
	
	public SpringDomainScanner(String... packages) {
		super();
		this.packages = packages;
	}


	@Override
	public List<Class<? extends BaseDatabaseDomain>> scan(String configFile) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
//		provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		provider.addIncludeFilter(new AssignableTypeFilter(BaseDatabaseDomain.class));
//		provider.setResourcePattern("**/*.class");
		Set<BeanDefinition> findCandidateComponents = new HashSet<>();
		for (String p:packages) {
			findCandidateComponents.addAll(provider.findCandidateComponents(p));
		}
		List<Class<? extends BaseDatabaseDomain>> list = new ArrayList<>();
		for (BeanDefinition temp:findCandidateComponents) {
			Class<?> domainClass;
			try {
				domainClass = Class.forName(temp.getBeanClassName());
				if (BaseRelationalDatabaseDomain.class.isAssignableFrom(domainClass)) {
					if (domainClass.getAnnotation(Entity.class) != null) {
						list.add((Class<? extends BaseDatabaseDomain>) domainClass);
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
