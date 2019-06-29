package steed.hibernatemaster.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class BaseRelationalDatabaseDomainTest extends SteedTest{
	
	@Test
	public void testTrimEmptyDomain(){
		Clazz clazz = new Clazz();
		clazz.setId("testTrimEmptyDomain");
		DaoUtil.deleteByQuery(clazz);
		
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
	
	@Test
	public void testUpdateNotNullFieldByHql() {
		Clazz clazz = new Clazz();
		clazz.setId("testUpdateNotNullFieldByHqlClass");
		DaoUtil.deleteByQuery(clazz);
		clazz.setName("foo");
		clazz.setSchool(DaoUtil.listOne(new School()));
		clazz.setStudentCount(20);
		clazz.save();
		DaoUtil.managTransaction();
		
		DaoUtil.evict(clazz);
		clazz.setName("bar");
		clazz.setStudentCount(32);
		clazz.setSchool(null);
		clazz.updateNotNullFieldByHql(Arrays.asList("school"), true);
		
		clazz = clazz.smartGet();
		Assert.assertTrue("bar".equals(clazz.getName()));
		Assert.assertTrue(clazz.getStudentCount() == 32);
		Assert.assertTrue(clazz.getSchool() == null);
		
		clazz.delete();
	}
	
}
