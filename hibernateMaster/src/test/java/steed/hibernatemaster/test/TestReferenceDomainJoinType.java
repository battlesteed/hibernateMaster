package steed.hibernatemaster.test;

import java.util.List;

import org.junit.Test;

import com.sun.org.apache.bcel.internal.generic.NEW;

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
		assert(listAllCustomField.size() == GenTestData.classSize);
	}
	
	@Test
	public void testJoinType1(){
		Student constraint = new Student();
		Clazz clazz = new Clazz();
		clazz.setName("class1");
		constraint.setClazz(clazz);
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(constraint, "clazz.id","clazz.name");
		assert(listAllCustomField.size() == GenTestData.classCount*GenTestData.classSize);
	}
	
	@Test
	public void testJoinType2(){
		Student constraint = new Student();
		Clazz clazz = new Clazz();
		clazz.setName("class1");
		constraint.setClazz(clazz);
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(constraint, "clazz.id","clazz.name");
		assert(listAllCustomField.size() == GenTestData.classSize);
	}
	
	@Test
	public void testJoinType3(){
		QueryBuilder builder = new QueryBuilder();
		builder.addIn("clazz.id", new Long[]{0L,1L});
		List<Object> listAllCustomField = DaoUtil.listAllCustomField(Student.class, builder.getQueryMap(), null, null, "id");
		assert(listAllCustomField.size() == GenTestData.classSize*2);
	}
	
}
