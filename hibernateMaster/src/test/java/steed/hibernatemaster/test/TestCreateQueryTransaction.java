package steed.hibernatemaster.test;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCreateQueryTransaction extends SteedTest{
	private String schoolId = "testSchool";
	
	@Test
	public void a1_insertTable() {
		Query createSQLQuery = DaoUtil.createSQLQuery(null, new StringBuffer(" INSERT INTO `School`(`id`, `buildDate`, `chargeMan`, `motto`, `name`) VALUES ('"+ schoolId +"', NULL, 'chargeMantest', NULL, 'schooltest');"));
		createSQLQuery.executeUpdate();
		DaoUtil.managTransaction();
		DaoUtil.relese();
		assert(isTestSchoolInDatabase());
	}
	
	@Test
	public void a2_testUpdateSqlQuery() {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("id", schoolId);
		String updateName = "schoolUpdated";
		param.put("updateName", updateName);
		Query createSQLQuery = DaoUtil.createSQLQuery(param, new StringBuffer(" update School set name=:updateName where id=:id "));
		createSQLQuery.executeUpdate();
		DaoUtil.managTransaction();
		DaoUtil.relese();
		assert(getSchool().getName().equals(updateName));
	}
	
	@Test
	public void a5_testUpdateHqlQuery() {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("id", schoolId);
		String updateName = "schoolUpdated2";
		param.put("updateName", updateName);
		Query createSQLQuery = DaoUtil.createQuery(param, new StringBuffer(" update School set name=:updateName where id=:id "));
		createSQLQuery.executeUpdate();
		DaoUtil.managTransaction();
		DaoUtil.relese();
		assert(getSchool().getName().equals(updateName));
	}
	@Test
	public void a6_testDeleteHqlQuery() {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", schoolId);
		Query createSQLQuery = DaoUtil.createQuery(where, new StringBuffer(" delete from School where id=:id "));
		createSQLQuery.executeUpdate();
		DaoUtil.managTransaction();
		DaoUtil.relese();
		assert(!isTestSchoolInDatabase());
	}
	
	@Test
	public void a7_testDeleteSqlQuery() {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", schoolId);
		Query createSQLQuery = DaoUtil.createSQLQuery(where, new StringBuffer(" delete from School where id=:id "));
		createSQLQuery.executeUpdate();
		DaoUtil.managTransaction();
		DaoUtil.relese();
		assert(!isTestSchoolInDatabase());
	}
	
	private boolean isTestSchoolInDatabase() {
		School school = getSchool();
		return school != null;
	}

	private School getSchool() {
		School school = DaoUtil.get(School.class, schoolId);
		return school;
	}
}
