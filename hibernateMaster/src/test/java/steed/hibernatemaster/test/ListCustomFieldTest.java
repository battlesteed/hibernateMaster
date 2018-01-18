package steed.hibernatemaster.test;

import java.util.List;
import java.util.regex.Matcher;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.base.RegUtil;

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
	public void testReg(){
		Matcher matcher = RegUtil.getPattern(".+\\(\\s*(\\S+)\\s*(\\S*)\\s*\\)").matcher("count(distinct clazz)");
		assert(matcher.find());
		assert("distinct".equals(matcher.group(1)));
		assert("clazz".equals(matcher.group(2)));
	}
}
