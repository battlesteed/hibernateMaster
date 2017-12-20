package steed.hibernatemaster.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import steed.hibernatemaster.util.DaoUtil;

public class SteedTest {
	
	public void genData(){
		new GenTestData().genData();
	}
	
	@BeforeClass
	public static void init(){
	}
	
	@Before
	public void beforeTest(){
	}
	
	@After
	public void afterTest(){
		assert(DaoUtil.managTransaction());
	}

}
