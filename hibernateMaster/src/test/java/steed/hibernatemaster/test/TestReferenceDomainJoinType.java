package steed.hibernatemaster.test;

import java.util.List;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;

public class TestReferenceDomainJoinType extends SteedTest{
	
	@Test
	public void testJoinType0(){
		Student constraint = new Student();
		Clazz clazz = new Clazz();
		clazz.setName("class0");
		constraint.setClazz(clazz);
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(constraint, "clazz.id");
		assert(listAllCustomField.size() == GenTestData.classSize*GenTestData.schoolCount);
	}
	
	@Test
	public void testJoinType1(){
		Student constraint = new Student();
		Clazz clazz = new Clazz();
		clazz.setName("class1");
		constraint.setClazz(clazz);
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(constraint, "clazz.id","clazz.name");
		assert(listAllCustomField.size() == GenTestData.schoolCount*GenTestData.classSize);
	}
	
	@Test
	public void testJoinType2(){
		Student constraint = new Student();
		Clazz clazz = new Clazz();
		clazz.setName("class1");
		constraint.setClazz(clazz);
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(constraint, "clazz.id","clazz.name");
		assert(listAllCustomField.size() == GenTestData.classSize*GenTestData.schoolCount);
	}
	
	@Test
	public void testJoinType3(){
		QueryBuilder builder = new QueryBuilder();
		builder.addIn("clazz.name", new String[]{"class0","class1"});
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(Student.class, builder.getWhere(), null, null, "id");
		assert(listAllCustomField.size() == GenTestData.classSize*2*GenTestData.schoolCount);
	}
	
}
