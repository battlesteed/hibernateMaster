package steed.hibernatemaster;

import steed.hibernatemaster.util.FactoryEngine;
import steed.hibernatemaster.util.HqlGenerator;
import steed.hibernatemaster.util.SimpleHqlGenerator;
import steed.hibernatemaster.util.SingleFactoryEngine;

public class Config {
	/**
	 * 是否是单数据库模式,可以调用<code>{@link steed.hibernatemaster.util.HibernateUtil#switchDatabase}</code>
	 * 切换数据库<br>
	 * 
	 * 注:多数据库模式未经过生产环境严格测试(只有一个生产环境在用),不保证稳定性
	 * 
	 * @see steed.hibernatemaster.util.HibernateUtil#switchDatabase
	 */
	public static boolean isSignalDatabase = true;
	/**
	 * 是否是开发模式
	 */
	public static boolean devMode = false;
	
	/**
	 * 数据库操作失败是否抛出异常(该设置项为全局设置,单独对当前线程设置请参考<code>{@link steed.hibernatemaster.util.DaoUtil#setThrowException}</code>)
	 */
	public static boolean throwException = false;
	/**
	 * 默认hql生成器(目前只是生成where 部分的 hql或sql)
	 */
	public static HqlGenerator defaultHqlGenerator = new SimpleHqlGenerator();
	/**
	 * 配置获取SessionFactory的引擎
	 */
	public static FactoryEngine factoryEngine = new SingleFactoryEngine();
	/**
	 * 是否自动提交事务(DaoUtil做了一个数据库操作就马上提交事务还是做完多个数据库操作才一起提交)
	 * 建议设置为false,做完多个数据库操作才一起提交保证了要么一起成功要么一起失败,并且效率也比做了一个数据库操作就马上提交事务高得多.
	 * 如果设置为false,做完数据库操作后请手动调用<code>{@link steed.hibernatemaster.util.DaoUtil#managTransaction()}</code>提交或回滚事务.
	 * 建议写一个过滤器,过滤所有请求,在doFilter之后调用下面的方法统一管理事务
	 * 
	 * @see steed.hibernatemaster.util.DaoUtil#managTransaction()
	 */
	public static boolean autoCommitTransaction = false;
	
	/**
	 * 是否自动开启事务,防止用户直接获取session执行写操作,而忘记开启事务,
	 * 导致写操作不生效
	 */
	public static boolean autoBeginTransaction = true;
	
	public static boolean sqlWallOpen = true;
	
	
}
