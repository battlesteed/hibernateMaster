package steed.hibernatemaster.util;

import java.util.Collection;

import org.hibernate.boot.MetadataSources;
import org.hibernate.service.ServiceRegistry;

public class MyMetadataSources extends MetadataSources{
	
	public MyMetadataSources() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MyMetadataSources(ServiceRegistry serviceRegistry) {
		super(serviceRegistry);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<Class<?>> getAnnotatedClasses() {
		new Exception().printStackTrace();
		return super.getAnnotatedClasses();
	}
}
