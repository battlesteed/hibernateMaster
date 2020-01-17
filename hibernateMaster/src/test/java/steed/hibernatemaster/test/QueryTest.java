package steed.hibernatemaster.test;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class QueryTest extends SteedTest{
	
	@Test
	public void testResultNull(){
		School where = new School();
		assert(!DaoUtil.isResultNull(where));
		where.setId(UUID.randomUUID().toString());
		assert(DaoUtil.isResultNull(where));
	}
	@Test
	public void testSelectNumber(){
		School where = new School();
		where.setName("school%");
		Page<Object> listCustomField = DaoUtil.listCustomField(School.class, 1, 1, DaoUtil.putField2Map(where), null, null, true, "1");
		Collection<?> domainCollection = listCustomField.getDomainCollection();
		assert(domainCollection.size() == 1);
		assert(listCustomField.getRecordCount() == GenTestData.schoolCount);
		
		listCustomField = DaoUtil.listCustomField(School.class, 100, 1, DaoUtil.putField2Map(where), null, null, true, "1","name","300");
		domainCollection = listCustomField.getDomainCollection();
		
		assert(domainCollection.size() == (100>GenTestData.schoolCount?GenTestData.schoolCount:100));
		assert(listCustomField.getRecordCount() == GenTestData.schoolCount);
		for (Object temp:domainCollection) {
			Map<String, Object> map = (Map<String, Object>) temp;
			assert(1 == (Integer)map.get("0"));
			assert(300 == (Integer)map.get("2"));
		}
	}
}
