package steed.hibernatemaster.domain;

import java.util.List;

public interface DomainScanner {
	
	/**
	 * 实体类扫描器
	 * @param configFile hibernate配置文件名
	 * @return 该配置文件对应的实体类
	 */
	public List<Class<? extends BaseDatabaseDomain>> scan(String configFile);
}
