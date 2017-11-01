package steed.hibernatemaster.util;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.type.StandardBasicTypes;

public class MySQLServerDialect extends SQLServerDialect{
	public MySQLServerDialect()
	 {
	     super();
	     registerHibernateType(1, "string");     
	     registerHibernateType(-9, "string");     
	     registerHibernateType(-16, "string");     
	     registerHibernateType(3, "double");  
	       
	     registerHibernateType(Types.CHAR, StandardBasicTypes.STRING.getName());     
	     registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());     
	     registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.STRING.getName());     
	     registerHibernateType(Types.DECIMAL, StandardBasicTypes.DOUBLE.getName());
	 }
}
