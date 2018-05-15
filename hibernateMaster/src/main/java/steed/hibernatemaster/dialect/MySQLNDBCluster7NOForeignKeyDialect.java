package steed.hibernatemaster.dialect;

public class MySQLNDBCluster7NOForeignKeyDialect extends MySQLNDB7Dialect {

	@Override
	public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable,
			String[] primaryKey, boolean referencesPrimaryKey) {
		return "";
	}
	
	
}
