package steed.hibernatemaster.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;
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
	public void testCountSameField(){
		List<Map> listAllCustomField = DaoUtil.listAllCustomField(Clazz.class, new  HashMap<String, Object>(), null, null, "school", "name","count( name )");
		assert(listAllCustomField.get(0).size() == 3);
		listAllCustomField = DaoUtil.listAllCustomField(Clazz.class, new  HashMap<String, Object>(), null, null, new String[] {"school"}, "name","count( name )");
		assert(listAllCustomField.get(0).size() == 2);
	}
	
	@Test
	public void testNullMap(){
		DaoUtil.listAllCustomField(School.class, null, null, null);
	}
	
	@Test
	public void testListOneField(){
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(School.class, null, null, null,"id");
		for(Object temp:listAllCustomField){
			assert(temp instanceof String);
		}
	}
	
	@Test
	public void testSum(){
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(new Clazz(), "sum(studentCount)","sum(studentCount)");
		BaseUtil.out(listAllCustomField.get(0));
	}
	
	@Test
	public void testIn(){
		QueryBuilder builder = new QueryBuilder();
		builder.addNotIn("id", new String[]{"1111"});
		long count = DaoUtil.getCount(Clazz.class, builder.getWhere());
		
		assert(GenTestData.classCount*GenTestData.schoolCount == count);
		
//		List<Object> listAllCustomField = DaoUtil.listAllCustomField(builder.getWhere(), Clazz.class, "id");
	}
	
	@Test
	public void testOrderBY(){
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(Clazz.class, null, Arrays.asList("sum(studentCount)","studentCount"), Arrays.asList("sum(studentCount)"), "id");
		BaseUtil.out(listAllCustomField.get(0));
	}
	
}
