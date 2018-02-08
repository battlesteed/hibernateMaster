package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.util.DaoUtil;

public class TestSelectIndex {
	
	@Test
	public void testGetNoSelectIndexFieldName(){
		check("name", "ab");
		check("money", "a");
		check("phone", "123");
		check("name", "ab1");
	}
	private void check(String fieldName, String group) {
		String subfix = "_"+group+DaoUtil.orGroup;
		String fullName = fieldName+subfix;
		assert(DaoUtil.isSelectIndex(fullName) == subfix.length());
		assert(fieldName.equals(DaoUtil.getNoSelectIndexFieldName(fullName)));
	}
}
