package steed.hibernatemaster.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class SteedTest extends HibernatemasterTester{
	
	public void genData(){
		new GenTestData().genData();
	}
	
	@BeforeClass
	public static void init(){
		Config.autoCommitTransaction = false;
		Config.devMode = true;
		Config.enableHibernateValidate = true;
		School school = new School();
		school.setName("school0");
		if (DaoUtil.isResultNull(school)) {
			new GenTestData().genData();
		}
	}
	
	@Before
	public void beforeTest(){
	}
	
	@After
	public void afterTest(){
		assert(DaoUtil.managTransaction());
		DaoUtil.closeSessionNow();
	}

}
