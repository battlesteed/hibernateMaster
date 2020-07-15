package steed.hibernatemaster.test;

import org.junit.Assert;
import org.junit.Test;

import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class BaseRelationalDatabaseDomainTest extends SteedTest{
	
	@Test
	public void testTrimEmptyDomain(){
		Clazz clazz = new Clazz();
		clazz.setId("testTrimEmptyDomain");
		DaoUtil.deleteByQuery(clazz);
		DaoUtil.managTransaction();
		
		clazz.setName("testTrimEmptyDomain");
		School school = new School();
		clazz.setSchool(school);
		clazz.setStudentCount(20);
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
