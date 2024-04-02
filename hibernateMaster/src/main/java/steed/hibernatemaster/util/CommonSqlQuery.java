package steed.hibernatemaster.util;

/**
 * 常用的sql查询功能
 * @author battlesteed
 *
 */
public class CommonSqlQuery {
	/**
	 * 查询数据库版本
	 * @return
	 */
	public static String getDatabaseVersion() {
		return (String) DaoUtil.getSession().createSQLQuery("SELECT VERSION();").uniqueResult();
	}
}
