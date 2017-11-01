package steed.hibernatemaster.util;

import org.hibernate.SessionFactory;

import steed.hibernatemaster.domain.DomainScanner;

public interface FactoryEngine {
	SessionFactory buildFactory(String configFile) ;
	DomainScanner getScanner(String configFile) ;
}