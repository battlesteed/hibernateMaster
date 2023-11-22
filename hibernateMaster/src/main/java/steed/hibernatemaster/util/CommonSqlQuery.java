package steed.hibernatemaster.util;

public class CommonSqlQuery {
	public static String getDatabaseVersion() {
		return (String) DaoUtil.getSession().createSQLQuery("SELECT VERSION();").uniqueResult();
	}
}
