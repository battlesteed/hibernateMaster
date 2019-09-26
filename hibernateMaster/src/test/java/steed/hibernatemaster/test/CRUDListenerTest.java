package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.listener.CRUDListener;
import steed.hibernatemaster.listener.CRUDListenerManager;
import steed.hibernatemaster.listener.SpringCRUDListenerScanner;
import steed.hibernatemaster.sample.domain.School;

public class CRUDListenerTest extends SteedTest{
	public static String CRUDListenerTestID = "CRUDListenerTest.testSave()";
	
	@Test
	public void testSave() {
		setCRUDListeners();
		
		School school = new School();
		school.setId(CRUDListenerTestID);
		school.save();
		
		assert(school.smartGet() == null);
	}
	
	@Test
	public void testSpringCRUDListenerManager() {
		Config.CRUDListenerManager = new SpringCRUDListenerScanner("steed");
		
		School school = new School();
		school.setId(CRUDListenerTestID);
		school.save();
		
		assert(school.smartGet() == null);
	}

	private void setCRUDListeners() {
		Config.CRUDListenerManager = new CRUDListenerManager() {
			
			@SuppressWarnings("unchecked")
			@Override
			public CRUDListener<? extends BaseDatabaseDomain>[] getListeners(Class<? extends BaseDatabaseDomain> clazz) {
				if (School.class.isAssignableFrom(clazz)) {
					return new CRUDListener[] {new SchoolCRUDListener()};
				}
				return null;
			}
		};
	}
}
