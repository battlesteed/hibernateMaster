package steed.hibernatemaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import steed.hibernatemaster.util.DaoUtil;

/**
 * 默认排序(当实体类加了该注解后,当查询没有指定排序字段时,默认按照该字段排序.),若不要按DefaultOrderBy排序,查询时desc或asc不传null即可
 * @author battlesteed
 * @see DaoUtil#setDefaultOrderBy(Class, String, boolean)
 */
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultOrderBy {
	/**
	 * 要排序的列名(字段名)
	 *
	 */
	public String value();
	public boolean desc() default false;
	
//	/**
//	 * 模糊查询策略
//	 * @author 战马
//	 *
//	 */
//	public enum Order{
//		/**
//		 * 降序
//		 */
//		desc,
//		/**
//		 * 升序
//		 */
//		asc,
//	}
	
}
