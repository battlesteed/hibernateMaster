package steed.hibernatemaster.domain;

import java.util.List;

public interface DomainScanner {
	
	public List<Class<? extends BaseDatabaseDomain>> scan(String configFile);
}
