package steed.hibernatemaster.test;

import java.util.Arrays;

import org.junit.Test;

import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.sample.domain.user.User2;
import steed.hibernatemaster.util.DaoUtil;

public class TestGroupBy extends SteedTest{
	
	@Test
	public void testGroupBy(){
		Page<Object> listCustomField = DaoUtil.listCustomField(User2.class, 10, 1, null, null, Arrays.asList("name"), true, new String[]{"name","nickName"}, "sum(status)","name");
	}
	
}
