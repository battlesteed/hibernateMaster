package steed.hibernatemaster.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import steed.ext.util.base.DomainUtil;
/**
 * 标明该字段的模糊查询策略,
 * 一般用于系统自动模糊查询的时候在字段值前面加上
 * '%'导致该查询不走索引的问题
 * @author 战马
 * @see DomainUtil#fuzzyQueryInitialize(steed.hibernatemaster.domain.BaseDomain, String...)
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface FuzzyQuery {
	/**
	 * 模糊查询策略
	 * @author 战马
	 *
	 */
	public FuzzyQuerystrategy value() ;
	
	/**
	 * 模糊查询策略
	 * @author 战马
	 *
	 */
	public enum FuzzyQuerystrategy{
		/**
		 * 在左边加'%"
		 */
		left,
		/**
		 * 在右边加'%"
		 */
		right,
		/**
		 * 不使用模糊查询
		 */
		none,
		/**
		 * 在左右两边加'%"
		 */
		both
	}
}
