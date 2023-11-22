package steed.hibernatemaster.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import steed.ext.util.base.StringUtil;
import steed.hibernatemaster.util.CommonSqlQuery;

public class CommonSqlQueryTest extends SteedTest{
	@Test
	public void testGetDatabaseVersion() {
		String databaseVersion = CommonSqlQuery.getDatabaseVersion();
		assertTrue(!StringUtil.isStringEmpty(databaseVersion));
	}
}
