package steed.hibernatemaster.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import steed.util.reflect.ReflectUtil;

public class ReflectTest {
	
	@Test
	public void testConvertDate() {
		String[] dates = new String[] {"2019-09-03","2019/11/03","2019/07/14","2119-08-03"};
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < dates.length; i++) {
			java.sql.Date convertDate = ReflectUtil.convertDate(java.sql.Date.class, dates[i]);
			Assert.assertTrue(simpleDateFormat.format(convertDate).replace("/", "-").startsWith(dates[i].replace("/", "-")));
		}
		
		for (int i = 0; i < dates.length; i++) {
			Date convertDate = ReflectUtil.convertDate(Date.class, dates[i]);
			Assert.assertTrue(simpleDateFormat.format(convertDate).replace("/", "-").startsWith(dates[i].replace("/", "-")));
		}
	}
	
	
}
