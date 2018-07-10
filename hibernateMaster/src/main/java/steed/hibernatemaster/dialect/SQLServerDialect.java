package steed.hibernatemaster.dialect;

import java.sql.Types;

import org.hibernate.type.StandardBasicTypes;

public class SQLServerDialect extends org.hibernate.dialect.SQLServerDialect{
	public SQLServerDialect(){
	     super();
	     registerHibernateType(Types.CHAR, StandardBasicTypes.STRING.getName());     
	     registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());     
	     registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.STRING.getName());     
	     registerHibernateType(Types.DECIMAL, StandardBasicTypes.DOUBLE.getName());
	 }
}
