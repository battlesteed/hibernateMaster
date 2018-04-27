package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.base.BaseUtil;

public class TestTransaction extends SteedTest{

	@Test
	public void queryTransactionID(){
		DaoUtil.listOne(new Student());//.delete();
		Object uniqueResult = DaoUtil.createSQLQuery(null, new StringBuffer("SELECT TRX_ID FROM INFORMATION_SCHEMA.INNODB_TRX  WHERE TRX_MYSQL_THREAD_ID = CONNECTION_ID()")).uniqueResult();
		BaseUtil.out(uniqueResult);
	}
}
