package steed.hibernatemaster.util;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.DomainScanner;

public class SpringFactoryEngine extends SingleFactoryEngine{
	private String[] packages;
	
	public SpringFactoryEngine(String... packages) {
		super();
		this.packages = packages;
	}
	
	@Override
	public DomainScanner getScanner(String configFile) {
//		if (!Config.isSignalDatabase) {
//			throw new RuntimeException("当前为多数据库模式,请设置steed.hibernatemaster.Config.factoryEngine为你自定义的多数据库sessionFactory生成器!");
//		}
		return new SpringDomainScanner(packages);
	}
	
	
	
}