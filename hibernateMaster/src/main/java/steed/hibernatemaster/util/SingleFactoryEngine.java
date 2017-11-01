package steed.hibernatemaster.util;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.DomainScanner;
import steed.util.base.BaseUtil;

public class SingleFactoryEngine implements FactoryEngine{
	
	public SessionFactory buildFactory(String configFile) {
		try {
			//configuration = new Configuration().configure(configFile);
//			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
//			.applySettings(configuration.getProperties());
//			SessionFactory factory2 = configuration.buildSessionFactory(builder.build());
			
			
			StandardServiceRegistry standardRegistry = new   StandardServiceRegistryBuilder()
				    .configure(configFile)
				    .build();

			MetadataSources metadataSources = new MetadataSources( standardRegistry );
			
			DomainScanner instanceFromProperties = getScanner(configFile);
			List<Class<? extends BaseDatabaseDomain>> scan = instanceFromProperties.scan(configFile);
			for(Class<? extends BaseDatabaseDomain> temp:scan){
				metadataSources.addAnnotatedClass(temp);
			}
			
		//	new HibernateUtil().scanDomain(metadataSources);
			
			Metadata metadata = metadataSources.getMetadataBuilder()
			    .applyImplicitNamingStrategy( ImplicitNamingStrategyJpaCompliantImpl.INSTANCE )
			    .build();

			SessionFactory factory2 = metadata.getSessionFactoryBuilder().build();
		
			
			return factory2;
			/*ProxoolConnectionProvider aConnectionProvider;*/
		} catch (Exception e) {
			BaseUtil.getLogger().error("创建sessionFactory失败",e);
		}
		return null;
	}

	@Override
	public DomainScanner getScanner(String configFile) {
		if (!Config.isSignalDatabase) {
			throw new RuntimeException("当前为多数据库模式,请设置steed.hibernatemaster.Config.factoryEngine为你自定义的多数据库sessionFactory生成器!");
		}
		return new SingleDomainScanner();
	}
	
	
	
}