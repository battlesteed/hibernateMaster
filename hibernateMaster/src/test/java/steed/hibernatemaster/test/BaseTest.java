package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.util.DaoUtil;

public class BaseTest extends SteedTest{
	
	@Test
	public void testSave(){
		Clazz clazz = new Clazz();
		clazz.setId("testSaveClass");
		DaoUtil.deleteByQuery(clazz);

		clazz.setName("testSaveClass");
		clazz.save();
	}
}
