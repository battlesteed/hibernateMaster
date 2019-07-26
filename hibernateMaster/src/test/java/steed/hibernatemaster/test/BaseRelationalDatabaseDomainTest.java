package steed.hibernatemaster.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import steed.ext.util.base.DateUtil;
import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.sample.domain.Student;
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
	public void testUpdateNotNullFieldByHql2() {
		String id = "testUpdateNotNullFieldByHql2";
		Clazz clazz = new Clazz();
		clazz.setStudentCount(10);
		clazz.setName("aa");
		clazz.setId(id);
		DaoUtil.deleteByQuery(clazz);
		clazz.save();
		
		Student student = new Student();
		
		student.setId(id);
		DaoUtil.deleteByQuery(student);
		
		student.setClazz(clazz);
		student.save();
		DaoUtil.managTransaction();
		
		student = new Student();
		student.setClazz(clazz);
		Date inDate = new Date();
		student.setInDate(inDate);
		student.setStudentNumber("foo");
		student.updateNotNullFieldByHql(null, true, "clazz");//也可以
		DaoUtil.managTransaction();
		
		Student updated = DaoUtil.get(Student.class, id);
		assertEquals("foo", student.getStudentNumber());
		assertEquals(DateUtil.getStringFormatDate(inDate, "yyyy-MM-dd HH:mm:ss"), DateUtil.getStringFormatDate(updated.getInDate(), "yyyy-MM-dd HH:mm:ss"));
		updated.delete();
		clazz.delete();
//		student.updateNotNullFieldByHql(null, true, "clazz.id");
	}
	@Test
	public void testUpdateNotNullFieldByHql3() {
		String id = "testUpdateNotNullFieldByHql3";
		Clazz clazz = new Clazz();
		clazz.setStudentCount(10);
		clazz.setName("aa");
		clazz.setId(id);
		DaoUtil.deleteByQuery(clazz);
		clazz.save();
		
		Student student = new Student();
		
		student.setId(id);
		DaoUtil.deleteByQuery(student);
		
		student.setClazz(clazz);
		student.save();
		DaoUtil.managTransaction();
		
		student = new Student();
		student.setClazz(clazz);
		Date inDate = new Date();
		student.setInDate(inDate);
		student.setStudentNumber("foo");
		student.updateNotNullFieldByHql(null, true, "clazz.id");//也可以
		DaoUtil.managTransaction();
		
		Student updated = DaoUtil.get(Student.class, id);
		assertEquals("foo", student.getStudentNumber());
		assertEquals(DateUtil.getStringFormatDate(inDate, "yyyy-MM-dd HH:mm:ss"), DateUtil.getStringFormatDate(updated.getInDate(), "yyyy-MM-dd HH:mm:ss"));
		updated.delete();
		clazz.delete();
	}
	
	@Test
	public void testUpdateNotNullFieldByHql() {
		Clazz clazz = prepareClazz();
		
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

	private Clazz prepareClazz() {
		Clazz clazz = new Clazz();
		clazz.setId("testUpdateNotNullFieldByHqlClass");
		DaoUtil.deleteByQuery(clazz);
		clazz.setName("foo");
		clazz.setSchool(DaoUtil.listOne(new School()));
		clazz.setStudentCount(20);
		clazz.save();
		DaoUtil.managTransaction();
		return clazz;
	}
	
}
