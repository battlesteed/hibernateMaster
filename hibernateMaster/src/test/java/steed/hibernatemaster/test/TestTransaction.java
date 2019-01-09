package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.base.BaseUtil;

public class TestTransaction extends SteedTest{
/*
	@Test
	public void queryTransactionID(){
		DaoUtil.listOne(new Student());//.delete();
		Object uniqueResult = DaoUtil.createSQLQuery(null, new StringBuffer("SELECT TRX_ID FROM INFORMATION_SCHEMA.INNODB_TRX  WHERE TRX_MYSQL_THREAD_ID = CONNECTION_ID()")).uniqueResult();
		BaseUtil.out(uniqueResult);
	}*/
	
	@Test
	public void testFlushRollback(){
		Student student = new Student();
		student.setId("TestTransaction.testFlushRollback");
		
		student.save();
		DaoUtil.getCount(student);
		DaoUtil.rollbackTransaction();
		DaoUtil.closeSessionNow();
		assert(DaoUtil.deleteByQuery(student) == 0);
	}
	
	@Test
	public void testRollback(){
		Student student = new Student();
		student.setId("TestTransaction.testRollback");
		
		student.save();
		DaoUtil.rollbackTransaction();
		DaoUtil.closeSessionNow();
		
		assert(DaoUtil.deleteByQuery(student) == 0);
	}
	
	@Test
	public void testCommitFail(){
		DaoUtil.managTransaction();
		DaoUtil.closeSessionNow();
		Student student = new Student();
		student.setId("TestTransaction.testCommitFail");
		student.delete();
		student.save();
		Student student2 = new Student();
		student2.setId("TestTransaction.testCommitFail2");
		student2.delete();
		student2.save();
		DaoUtil.managTransaction();
		DaoUtil.closeSessionNow();
		
		DaoUtil.getSession().createSQLQuery("update student set name='22222222222' where id = 'TestTransaction.testCommitFail2'").executeUpdate();
		student = new Student();
		student.setId("TestTransaction.testCommitFail");
		student.save();
		
		DaoUtil.managTransaction();
		DaoUtil.closeSessionNow();
		student2 = new Student();
		student2.setName("22222222222");
		
		assert(DaoUtil.getCount(student2) == 0);
		student.delete();
//		student2.setId("TestTransaction.testCommitFail2");
		student2.delete();
	}
	
	
}
