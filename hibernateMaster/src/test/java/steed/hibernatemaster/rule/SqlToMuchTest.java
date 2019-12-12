package steed.hibernatemaster.rule;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import steed.ext.util.logging.LoggerFactory;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.test.SteedTest;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;

public class SqlToMuchTest extends SteedTest{
	private String[] studentIds;
	private int dataSize = 1000;
	
//	@Test
	public void testManySqlTime(){
		steed.ext.util.test.TestEfficiency testEfficiency = new steed.ext.util.test.TestEfficiency();
		testEfficiency.begin();
		
		List<Student> studentList = new ArrayList<>(dataSize);
		
		for(String id:studentIds){
			studentList.add(DaoUtil.get(Student.class, id));
		}
		
		LoggerFactory.getLogger().debug("查询到了"+studentList.size()+"个student");
		
		for(Student temp:studentList){
			temp.delete();
		}
		cleanSession();
		
		testEfficiency.endAndOutUsedTime(String.format("%d个查询sql,%d个删除sql,用时", dataSize,dataSize));
	}
	
//	@Test
	public void testOneSqlTime(){
		steed.ext.util.test.TestEfficiency testEfficiency = new steed.ext.util.test.TestEfficiency();
		testEfficiency.begin();
		
		QueryBuilder builder = new QueryBuilder();
		builder.addIn("id", studentIds);
		
		List<Student> studentList = DaoUtil.listAllObj(Student.class, builder.getWhere(), null, null);
		LoggerFactory.getLogger().debug("查询到了"+studentList.size()+"个student");
		
		
		DaoUtil.deleteByQuery(Student.class,builder.getWhere());
		cleanSession();
		
		testEfficiency.endAndOutUsedTime(String.format("1个查询sql,1个删除sql,用时", dataSize,dataSize));
		
	}
	
//	@Before
//	@Override
	public void beforeTest() {
		super.beforeTest();
		insertTestData();
	}

	private void insertTestData() {
		studentIds = new String[dataSize];
		
		for (int i = 0; i < dataSize; i++) {
			Student student = new Student();
			student.setName("testNamet"+i);
			student.setStudentNumber("testNumber"+i);
			String id = "testStudent"+i;
			studentIds[i] = id;
			student.setId(id);
			student.save();
		}
		
		cleanSession();
	}

	//清理session,防止数据库查询操作直接从session缓存取数据,导致测试结果不准确
	private void cleanSession() {
		DaoUtil.managTransaction();
		DaoUtil.relese();
		DaoUtil.closeSessionNow();
	}
	
	
}
