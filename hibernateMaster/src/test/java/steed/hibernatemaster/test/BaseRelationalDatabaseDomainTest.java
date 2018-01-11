package steed.hibernatemaster.test;

import org.junit.Assert;
import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.reflect.ReflectUtil;

public class BaseRelationalDatabaseDomainTest extends SteedTest{
	
	@Test
	public void testTrimEmptyDomain(){
		Clazz clazz = new Clazz();
		clazz.setId("testTrimEmptyDomain");
		DaoUtil.deleteByQuery(clazz);
		
		clazz.setName("testTrimEmptyDomain");
		School school = new School();
		clazz.setSchool(school);
		clazz.save();
		Assert.assertTrue(!DaoUtil.managTransaction());
		DaoUtil.closeSessionNow();
		
		clazz = ReflectUtil.copyObj(clazz);
		clazz.setTrimEmptyDomain(true);
		Assert.assertTrue(clazz.save());
		Assert.assertTrue(DaoUtil.managTransaction());
		Assert.assertTrue(DaoUtil.deleteByQuery(clazz) == 1);
	}
	
}
