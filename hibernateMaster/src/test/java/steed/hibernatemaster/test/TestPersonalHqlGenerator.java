package steed.hibernatemaster.test;

import java.util.Map;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.SimpleHqlGenerator;

public class TestPersonalHqlGenerator extends SteedTest{
	
	@Test
	public void test1() {
		Clazz clazz = new Clazz();
		clazz.setPersonalHqlGenerator(new SimpleHqlGenerator() {

			@Override
			protected void appendPersonalWhere(String domainSimpleName, StringBuffer hql,Map<String, Object> query) {
				hql.append(" and domain.id = '1' and domain.id = '2' ");
			}
			
		});
		assert(DaoUtil.getCount(clazz) == 0);
	}
}
