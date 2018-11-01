package steed.hibernatemaster.test;

import java.util.ArrayList;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;

public class TestQueryBuilder extends SteedTest{
	
	@Test
	public void testAddIn() {
		School listOne = DaoUtil.listOne(new School());
		QueryBuilder queryBuilder = new QueryBuilder();
//		queryBuilder.addIn("id", listOne.getId());
		queryBuilder.addIn("buildDate", null);
		assert(DaoUtil.getCount(School.class,queryBuilder.getWhere()) == 0);
		queryBuilder.getWhere().clear();
		queryBuilder.addIn("id", listOne.getId());
		assert(DaoUtil.getCount(School.class,queryBuilder.getWhere()) == 1);
		queryBuilder.getWhere().clear();
		queryBuilder.addIn("id", new ArrayList<>());
		assert(DaoUtil.getCount(School.class,queryBuilder.getWhere()) == 0);
		
	}
	
	@Test
	public void testNotEqual() {
		School listOne = DaoUtil.listOne(new School());
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.addNotEqual("chargeMan", listOne.getChargeMan(), 0);
		queryBuilder.addNotEqual("chargeMan", listOne.getChargeMan(), 0);
		queryBuilder.add("id", listOne.getId());
		
		assert(DaoUtil.isResultNull(School.class, queryBuilder.getWhere()));
		queryBuilder.addNotEqual("chargeMan", "ffdfd", 1);
		assert(DaoUtil.isResultNull(School.class, queryBuilder.getWhere()));
		
		queryBuilder.addNotEqual("chargeMan", "ffdfd");
		assert(DaoUtil.isResultNull(School.class, queryBuilder.getWhere()));
		
		queryBuilder.addNotEqual("chargeMan", listOne.getChargeMan()+"vvv", 0);
		assert(!DaoUtil.isResultNull(School.class, queryBuilder.getWhere()));
	}
}
