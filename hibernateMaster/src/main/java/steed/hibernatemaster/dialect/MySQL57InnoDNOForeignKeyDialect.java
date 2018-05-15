package steed.hibernatemaster.dialect;

import org.hibernate.dialect.MySQL57InnoDBDialect;

public class MySQL57InnoDNOForeignKeyDialect extends MySQL57InnoDBDialect {

	@Override
	public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable,
			String[] primaryKey, boolean referencesPrimaryKey) {
		return "";
	}
	
	
}
