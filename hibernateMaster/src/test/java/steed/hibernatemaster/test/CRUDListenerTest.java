package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.listener.CRUDListener;
import steed.hibernatemaster.listener.CRUDListenerManager;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class CRUDListenerTest extends SteedTest{
	
	@Test
	public void testSave() {
		Config.CRUDListenerManager = new CRUDListenerManager() {
			
			@SuppressWarnings("unchecked")
			@Override
			public CRUDListener<? extends BaseDatabaseDomain>[] getListeners(Class<? extends BaseDatabaseDomain> clazz) {
				if (School.class.isAssignableFrom(clazz)) {
					return new CRUDListener[] {new CRUDListener<School>() {

							@Override
							public void beforSave(School domain) {
								domain.setName("beforSave");
							}
	
							@Override
							public void afterSave(School domain) {
								DaoUtil.managTransaction();
								domain = domain.smartGet();
								assert("beforSave".equals(domain.getName()));
								domain.delete();
							}
							
						}
					};
				}
				return null;
			}
		};
		School school = new School();
		school.setId("CRUDListenerTest.testSave()");
		school.save();
		
		assert(school.smartGet() == null);
	}
}
