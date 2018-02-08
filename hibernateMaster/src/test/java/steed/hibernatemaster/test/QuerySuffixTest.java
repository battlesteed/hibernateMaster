package steed.hibernatemaster.test;

import java.util.List;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;

public class QuerySuffixTest {
	
	@Test
	public void testMoreThanAdnLessThan(){
		QueryBuilder builder = new QueryBuilder();
		builder.addMoreThan("name", "school0");
		List<School> listAllObj = DaoUtil.listAllObj(School.class, builder.getQueryMap(), null, null);
		for (School school:listAllObj) {
			assert("school0".compareTo(school.getName()) <= 0);
		}
		
		builder.addLessThan("name", "school0");
		listAllObj = DaoUtil.listAllObj(School.class, builder.getQueryMap(), null, null);
		for (School school:listAllObj) {
			assert("school0".equals(school.getName()));
		}
		assert(listAllObj.size() == 1);
		
	}
}
