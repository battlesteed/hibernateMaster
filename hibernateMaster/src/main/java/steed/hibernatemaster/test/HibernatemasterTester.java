package steed.hibernatemaster.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.util.DaoUtil;

public class HibernatemasterTester {
	
	
	@BeforeClass
	public static void init(){
		Config.autoCommitTransaction = false;
		Config.devMode = true;
		Config.enableHibernateValidate = true;
	}
	
	@Before
	public void beforeTest(){
	}
	
	@After
	public void afterTest(){
		assert(DaoUtil.managTransaction());
	}

}
