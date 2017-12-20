package steed.hibernatemaster.test;

import java.util.Arrays;

import org.junit.Test;

import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.sample.domain.user.User;
import steed.hibernatemaster.util.DaoUtil;

public class TestGroupBy extends SteedTest{
	
	@Test
	@SuppressWarnings("unused")
	public void testGroupBy(){
		Page<Object> listCustomField = DaoUtil.listCustomField(User.class, 10, 1, null, null, Arrays.asList("name"), true, new String[]{"name","nickName"}, "sum(status)","name");
		int a = 3;
	}
	
}
