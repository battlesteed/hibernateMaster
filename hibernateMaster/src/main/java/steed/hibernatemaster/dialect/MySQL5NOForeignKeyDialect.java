package steed.hibernatemaster.dialect;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5NOForeignKeyDialect extends MySQL5InnoDBDialect {

	@Override
	public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable,
			String[] primaryKey, boolean referencesPrimaryKey) {
		return "";
	}
	
	
}
