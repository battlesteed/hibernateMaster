package steed.hibernatemaster.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;

public class TestPutField2Map {
	
	@Test
	public void testTempData() {
		Student student = new Student();
		student.setName("student");
		student.setTempData("tempData");
		Map<String, Object> putField2Map = DaoUtil.putField2Map(student);
		assertNull(putField2Map.get("tempData"));
		assertNotNull(putField2Map.get("name"));
	}
}
