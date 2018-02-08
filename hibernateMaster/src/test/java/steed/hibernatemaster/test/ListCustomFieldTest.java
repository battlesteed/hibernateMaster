package steed.hibernatemaster.test;

import java.util.List;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.base.BaseUtil;

public class ListCustomFieldTest {
	
	@Test
	public void testDistinct(){
		Clazz clazz = new Clazz();
		clazz.setName("class0");
		Student student = new Student();
		student.setClazz(clazz);
		
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(student, "count(distinct clazz.id)");
		assert(listAllCustomField.size() == 1);
		Object object = listAllCustomField.get(0);
		assert(((Long)object) == GenTestData.schoolCount);
		List<Object> clazzIDs = DaoUtil.listAllCustomField(student, "distinct clazz.id ");
		
		assert(clazzIDs.size() == GenTestData.schoolCount);
		
		
//		DaoUtil.getSession().createQuery("select count(distinct clazz ) as clazzss from steed.hibernatem aster.sample.domain.Student student_steed_00 where  1=1 ").list();
	}
	
	@Test
	public void testSum(){
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(new Clazz(), "sum(studentCount)","sum(studentCount)");
		BaseUtil.out(listAllCustomField.get(0));
	}
	
}
