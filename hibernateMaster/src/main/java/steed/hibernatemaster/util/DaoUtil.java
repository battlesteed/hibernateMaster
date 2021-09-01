package steed.hibernatemaster.util;


import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.NonUniqueObjectException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.base.CollectionsUtil;
import steed.ext.util.base.DomainUtil;
import steed.ext.util.base.RegUtil;
import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectResult;
import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.Config;
import steed.hibernatemaster.annotation.DefaultOrderBy;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;
import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.domain.BaseUnionKeyDomain;
import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.filter.QueryFilter;
/**
 * 实现0sql和0hql伟大构想的dao工具类，用该类即可满足绝大多数数据库操作<br>
 * <a href='https://battle_steed.gitee.io/hibernatemaster/hibernateMaster/doc/index.html'>java doc https://battle_steed.gitee.io/hibernatemaster/hibernateMaster/doc/</a><br>
_______________#########_______________________<br>
______________############_____________________<br>
______________#############____________________<br>
_____________##__###########___________________<br>
____________###__######_#####__________________<br>
____________###_#######___####_________________<br>
___________###__##########_####________________<br>
__________####__###########_####_______________<br>
________#####___###########__#####_____________<br>
_______######___###_########___#####___________<br>
______######___###___########___######_________<br>
_____#######___###__###########___######_______<br>
____#######___####_##############__######______<br>
___########__#####################_#######_____<br>
___########__##############################____<br>
___#######__######_#################_#######___<br>
___#######__######_######_#########___######___<br>
___#######____##__######___######_____######___<br>
___#######________######____#####_____#####____<br>
____######________#####_____#####_____####_____<br>
_____#####________####______#####_____###______<br>
______#####_______###________###______#________<br>
________##_______####________####______________<br>
                                     	
 * @author 战马 battle_steed@qq.com
 * 
 * @see QueryBuilder
 */
public class DaoUtil {
	private static final ThreadLocal<Boolean> transactionType = new ThreadLocal<>();
	// 具体的错误提示用
	private static final ThreadLocal<Exception> exception = new ThreadLocal<>();
	private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();
	public final static String personalHqlGeneratorKey = "personalHqlGenerator";
	
	private static final Logger logger = LoggerFactory.getLogger(DaoUtil.class);
	/**
	 * 是否自动提交或回滚事务
	 * 自助事务步骤：
	 * 1,调用setAutoManagTransaction(false)把自动事务设为false
	 * 2,调用managTransaction()管理事务;
	 */
	private static final ThreadLocal<Boolean> autoManagTransaction = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> throwException = new ThreadLocal<>();
	
	protected static final Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy> defaultOrderBy = new HashMap<Class<? extends BaseDatabaseDomain>, DefaultOrderBy>();
//	private static final Map<Class<?>, CRUDListener<?>> CRUDListenerMap = new HashMap<>();
	
	
//	public interface CRUDListener<T>{
//		public default void onDelete(T t){};
//		public default void onSave(T t){};
//		public default void onUpdate(){};
//	}
	
	
	private DaoUtil() {
	}

	public static final String orGroup = "_OrGroup";
	public static final String andGroup = "_AndGroup";
	public static final String rawHqlPart = "_raw_hql_part";
	/**
	 * xxx + manyNotIN 表示一对多中,一关联的set或list等不包含xxx
	 * @see #notIN
	 */
	public static final String manyNotIN = "_manyNotIn";
	/**
	 * xxx + notIN 表示实体类中的字段非集合类字段 xxx not in
	 */
	public static final String notIN = "_not_in_1";
	
	/**
	 * 查询条件后缀
	 */
	public final static String[] indexSuffix = {"_max_1","_min_1","_like_1",notIN,"_not_equal_1",
			manyNotIN,"_not_null","_not_compile_param",personalHqlGeneratorKey,
			"_greaterThan","_lessThan",rawHqlPart,orGroup};
	
	/***********\异常提示专用************/
	
	/*//TODO 完善异常类型
	private static final Integer[] exceptionTypes = {10,11};
	private static final String[] exceptionReasons = {"主键重复","主键未指定"};
	private static final Exception[] exceptions = {};*/
	
	/*-------#异常提示专用************/
	
	private final static Exception getExceptiontype() {
		return exception.get();
	}
	
	/**
	 * 设置实体类默认排序字段(当查询没有指定排序字段时,默认按照该字段排序),也可以直接在实体类加{@link DefaultOrderBy} 注解
	 * @param target 要排序的表(实体类)
	 * @param column 要排序的列名(字段名)
	 * @param desc 是否降序排列
	 * 
	 * @see #removeDefaultOrderBy(Class)
	 * @see steed.hibernatemaster.annotation.DefaultOrderBy
	 */
	public static final void setDefaultOrderBy(Class<? extends BaseDatabaseDomain> target,String column,boolean desc) {
		DefaultOrderBy orderBy = new DefaultOrderBy() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return DefaultOrderBy.class;
			}
			
			@Override
			public String value() {
				return column;
			}
			
			@Override
			public boolean desc() {
				return desc;
			}
		};
		defaultOrderBy.put(target, orderBy);
	}
	
	/**
	 * 移除{@link #setDefaultOrderBy(Class, String, boolean)} 设置的实体类默认排序规则
	 * 
	 * @see #setDefaultOrderBy(Class, String, boolean)
	 */
	public static final void removeDefaultOrderBy(Class<? extends BaseDatabaseDomain> target) {
		defaultOrderBy.remove(target);
	}
	
	public final static void setException(Exception exception,boolean rollbackTransaction) {
		DaoUtil.exception.set(exception);
		logger.error("数据库操作发生异常",exception);
		Boolean shouldThrowException = throwException.get();
		if ((shouldThrowException == null && Config.throwException) || 
				(shouldThrowException != null && shouldThrowException && exception instanceof RuntimeException)) {
			throw ((RuntimeException)exception);
		}
	}
	
	/**
	 * 数据库操作失败是否抛出异常
	 * @param isThrow
	 */
	public final static void setThrowException(Boolean isThrow) {
		throwException.set(isThrow);
	}
	public final static void setException(Exception exception) {
		setException(exception, false);
	}
	public final static Transaction getCurrentTransaction() {
		return currentTransaction.get();
	}
	public final static void setCurrentTransaction(Transaction currentTransaction) {
		DaoUtil.currentTransaction.set(currentTransaction);
	}
	
	/**
	 * 获取当前事务类型
	 * @return 调用{@link #managTransaction()}是否提交提交事务
	 * 
	 * @see #managTransaction()
	 */
	public final static Boolean getTransactionType() {
		return transactionType.get();
	}
	
	/**
	 * 设置默认事务类型
	 * @param isCommit
	 */
	public final static void setDefaultTransactionType(boolean isCommit) {
		if (isCommit && DaoUtil.transactionType.get() == null) {
			 DaoUtil.transactionType.set(isCommit);
		}
	}
	
	public final static void setTransactionType(Boolean transactionType) {
		DaoUtil.transactionType.set(transactionType);
	}
	
	public final static Boolean getAutoManagTransaction() {
		return autoManagTransaction.get();
	}
	
	/**
	 * 立即事务开始，框架可能配置了多个数据库操作使用同一事务然后统一提交
	 * 如某些操作可能要马上提交事务或者跟其他数据库操作使用不同的事务，可使用该方法
	 * 用法:<br>
	 *  <code> ImmediatelyTransactionData immediatelyTransactionData = DaoUtil.immediatelyTransactionBegin();<br>
	 *  //TODO 这里做其他数据库操作<br>
	 *	DaoUtil.immediatelyTransactionEnd(immediatelyTransactionData);<br>
	 *  </code>
	 *	
	 * @see #immediatelyTransactionEnd
	 * @return 调用该方法之前的事务数据,用于<code>{@link #immediatelyTransactionEnd(ImmediatelyTransactionData)}</code>恢复之前的事务.
	 */
	public final static ImmediatelyTransactionData immediatelyTransactionBegin(){
		logger.debug("立即事务开始");
		Session session = getSession();
		Transaction currentTransaction = getCurrentTransaction();
		Boolean autoManagTransaction = getAutoManagTransaction();
		Boolean transactionType = getTransactionType();
//		setAutoManagTransaction(true);
		setCurrentTransaction(null);
		setTransactionType(null);
		ImmediatelyTransactionData immediatelyTransactionData = new ImmediatelyTransactionData(currentTransaction, autoManagTransaction,session);
		immediatelyTransactionData.transactionType = transactionType;
		HibernateUtil.setSession(null);
		return immediatelyTransactionData;
	}
	
	/**
	 * 立即事务结束,调用该方法之前请先调用<code>{@link #managTransaction()}</code> 提交立即事务,否则
	 * immediatelyTransactionBegin和immediatelyTransactionEnd之间做的数据库操作不会生效
	 * 
	 * @param immediatelyTransactionData <code>{@link #immediatelyTransactionBegin()}</code>返回的值
	 * 
	 * @see #immediatelyTransactionBegin
	 * @see #managTransaction()
	 * 
	 */
	public final static void immediatelyTransactionEnd(ImmediatelyTransactionData immediatelyTransactionData){
		HibernateUtil.closeSession();
		DaoUtil.setTransactionType(immediatelyTransactionData.transactionType);
		DaoUtil.setCurrentTransaction(immediatelyTransactionData.currentTransaction);
		DaoUtil.setAutoManagTransaction(immediatelyTransactionData.autoManagTransaction);
		HibernateUtil.setSession(immediatelyTransactionData.session);
		logger.debug("立即事务结束");
	}
	
	public final static void setAutoManagTransaction(Boolean selfManagTransaction) {
		DaoUtil.autoManagTransaction.set(selfManagTransaction);;
	}
	
	/***************************增删查改开始******************************/
	
	/**
	 */
	public final static <T> Page<T> listObj(int pageSize,int currentPage, Class<? extends BaseRelationalDatabaseDomain> t){
		try {
			StringBuffer hql = getSelectHql(t,null,null,null);
			Long recordCount = getRecordCount(null, hql);
			Query query = getSession().createQuery(hql.toString());
			
			paging(pageSize,currentPage, query);
			@SuppressWarnings("unchecked")
			List<T> list = query.list();
			
			return setPage(currentPage, recordCount, pageSize, list);
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	/**
	 * 查询target对应的表中所有记录的主键
	 * 
	 * @param target 要查询的实体类
	 * 
	 * @return 查出来的主键
	 */
	@SuppressWarnings("unchecked")
	public final static <T> List<Serializable> listAllObjKey(Class<? extends BaseRelationalDatabaseDomain> target){
		try {
			String name = target.getName();
			String keyName = DomainUtil.getDomainIDName(target);
			String hql = "select "+keyName+" from " + name;
			Query query = getSession().createQuery(hql);
			return query.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	public final static boolean saveList(List<? extends BaseRelationalDatabaseDomain> list){
		if (list == null || list.isEmpty()) {
			return true;
		}
		try {
			beginTransaction();
			for (BaseRelationalDatabaseDomain obj:list) {
				obj.save();
			}
			return managTransaction(true);
		} catch (Exception e) {
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	
	/**
	 * 查询所有id在ids里面的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param ids 实体类id，英文逗号分割
	 * 
	 * @return 查询到的记录
	 */
	@SuppressWarnings("unchecked")
	public final static <T> List<T> listByKeys(Class<? extends BaseRelationalDatabaseDomain> target,String[] ids){
		try {
			if (ids == null || ids.length == 0) {
				return new ArrayList<>();
			}
			Map<String, Object> map = new HashMap<String, Object>();
			Class<? extends Serializable> idClass = DomainUtil.getDomainIDClass(target);
			Serializable[] serializables;
			//TODO 支持非String[] 类型的ids
			if (idClass == String.class) {
				serializables = ids;
			}else{
				serializables = new Serializable[ids.length];
				for (int i = 0; i < serializables.length; i++) {
					serializables[i] = (Serializable) ReflectUtil.convertFromString(idClass, ids[i]);
				}
			}
			map.put(DomainUtil.getDomainIDName(target)+"_not_join", serializables);
			return (List<T>) listAllObj(target, map, null, null);
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * list所有对象一起update成功或失败,update成功或失败的单位是整个list
	 * ,你可能需要另外一个方法updateListOneByOne(List list)
	 * @see #updateListOneByOne
	 * @param list
	 * @return 是否update成功(若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 */
	public final static boolean updateList(List<? extends BaseRelationalDatabaseDomain> list){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			for (BaseRelationalDatabaseDomain obj:list ) {
				session.update(obj);
			}
			return managTransaction(true);
		} catch (Exception e) {
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 不要求list里面所有对象一起update成功或失败,update单位是单个对象,
	 * 你可能需要另外一个方法updateList(List list),请参看本类中的updateEvenNull方法
	 * @param list
	 * @param updateEvenNull 即使为空也更新到数据库中的字段，如果为null，
	 * 			则根据domain字段中的UpdateEvenNull注解更新，
	 * 
	 * @return update失败的对象数
	 */
	public final static int updateListNotNullFieldOneByOne(List<? extends BaseRelationalDatabaseDomain> list,List<String> updateEvenNull){
		int failed = 0;
		for (Object o:list) {
			if (!updateNotNullField((BaseRelationalDatabaseDomain) o, updateEvenNull)) {
				failed++;
			}
		}
		return failed;
	}
	
	public final static void evict(Object obj){
		getSession().evict(obj);
		closeSession();
	}
	/**
	 * 如果数据库有obj对象就update否则save
	 * @param obj
	 * @return
	 */
	public final static boolean saveOrUpdate(BaseRelationalDatabaseDomain obj){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			if (BaseUtil.isObjEmpty(DomainUtil.getDomainId(obj))) {
				return save(obj);
			}else {
				Map<String, Object> map = new HashMap<>();
				Class<? extends BaseRelationalDatabaseDomain> clazz = obj.getClass();
				String domainIDName = DomainUtil.getDomainIDName(clazz);
				map.put(domainIDName, ReflectUtil.getValue(domainIDName, obj));
				
				if (!DaoUtil.isResultNull(clazz, map)) {
					session.update(obj);
				}else {
					session.save(obj);
				}
			}
			return managTransaction(true);
		} catch (Exception e) {
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	public final static int executeUpdateBySql(String sql,Map<String,? extends Object> map){
		return executeUpdate(sql, map, 1);
	}
	
	public final static int executeUpdate(String hql,Map<String,? extends Object> map){
		return executeUpdate(hql, map, 0);
	}
	
	public final static Object getUniqueResult(String hql,String domainSimpleName,
			Map<String,Object> map){
		try {
			StringBuffer sb = new StringBuffer(hql);
			if(domainSimpleName != null && map != null){
				appendHqlWhere(domainSimpleName, sb, map);
			}
			Query createQuery = getSession().createQuery(sb.toString());
			if (map != null) {
				setMapParam(map, createQuery);
			}
			return createQuery.uniqueResult();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	/**
	 * 
	 * @param hql
	 * @param domainSimpleName
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public final static List getQueryResult(String hql,String domainSimpleName,
			Map<String,Object> map){
		try {
			StringBuffer sb = new StringBuffer(hql);
			if (domainSimpleName != null&&map!=null) {
				appendHqlWhere(domainSimpleName, sb, map);
			}
			Query createQuery = getSession().createQuery(sb.toString());
			if (map != null) {
				setMapParam(map, createQuery);
			}
			
			return createQuery.list();
			//return createQuery.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static Page getQueryResult(String hql,String domainSimpleName,
			Map<String,Object> map,int pageSize,int currentPage){
		try {
			StringBuffer sb = new StringBuffer(hql);
			if (map != null && domainSimpleName != null) {
				appendHqlWhere(domainSimpleName, sb, map);
			}
			
			Long recordCount = getRecordCount(map, sb);
			
			Query createQuery = getSession().createQuery(sb.toString());
			if (map != null) {
				setMapParam(map, createQuery);
			}
			paging(pageSize, currentPage, createQuery);
			
			return setPage(currentPage, recordCount, pageSize, createQuery.list());
			//return createQuery.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public final static List getQueryResultBysql(String sql,Map<String,? extends Object> param){
		try {
			Query createQuery = getSession().createSQLQuery(sql);
			if (param != null) {
				setMapParam(param, createQuery);
			}
			return createQuery.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings("rawtypes")
	public final static List getQueryResult(String hql,Map<String,? extends Object> map,int pageSize,int currentPage){
		try {
			Query createQuery = getSession().createQuery(hql);
			if (map != null) {
				setMapParam(map, createQuery);
			}
			paging(pageSize, currentPage, createQuery);
			return createQuery.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings("rawtypes")
	public final static List getQueryResult(String hql,Map<String,? extends Object> map){
		try {
			Query createQuery = getSession().createQuery(hql);
			if (map != null) {
				setMapParam(map, createQuery);
			}
			return createQuery.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	private static int executeUpdate(String ql,Map<String,? extends Object> map,int type){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			Query createQuery;
			if (type == 0) {
				createQuery = session.createQuery(ql);
			}else {
				createQuery = session.createSQLQuery(ql);
			}
			if (map != null) {
				setMapParam(map, createQuery);
			}
			int executeUpdate = createQuery.executeUpdate();
			managTransaction(true);
			return executeUpdate;
		} catch (Exception e) {
			setException(e);
			managTransaction(false);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 通过hql更新数据库，用于批量更新
	 * @param where 查询条件，同listAllObj的查询条件
	 * @param updated 存放更新的 字段---值
	 * @return 更新的记录数，失败返回-1
	 */
	public final static int updateByQuery(BaseDomain where,Map<String, Object> updated){
		return updateByQuery(where.getClass(), putField2Map(where), updated);
	}
	
	/**
	 * 通过hql更新数据库，用于批量更新
	 * @param where 查询条件，同listAllObj的查询条件
	 * @param updated 要更新的字段值(不为null的字段将会被更新到数据库)
	 * @return 更新的记录数，失败返回-1
	 */
	public final static int updateByQuery(BaseDomain where,BaseDomain updated){
		return updateByQuery(where, ReflectUtil.field2Map(updated, true, true));
	}
	
	/**
	 * 通过hql更新数据库，用于批量更新
	 * @param where 查询条件，同listAllObj的查询条件
	 * @param updated 存放更新的字段-值
	 * @return 更新的记录数，失败返回-1
	 */
	public final static int updateByQuery(Class<?> clazz,Map<String, Object> where,Map<String, Object> updated){
		try {
			beginTransaction();
			
			if (Config.muffUpdateCheck && (where == null || where.isEmpty())) {
				throw new IllegalArgumentException("steed.hibernatemaster.Config已经开启了误更新检查,为防止误更新,queryCondition不能为空!");
			}
			
			if (where == null) {
				where = new HashMap<>();
			}
			StringBuffer updateHql = getUpdateHql(clazz, where,updated);
			for (Entry<String, Object> temp:updated.entrySet()) {
				where.put("steedUpdate_"+temp.getKey(), temp.getValue());
			}
			Query query = createQuery(where, updateHql);
			int count = query.executeUpdate();
			
			if(managTransaction(true)){
				return count;
			}else {
				return -1;
			}
		} catch (Exception e) {
			setException(e);
			managTransaction(false);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 以obj为查询条件删除数据库记录
	 * 
	 * @param obj 查询条件
	 * @return 删除的记录数（失败返回-1）
	 */
	public final static int deleteByQuery(BaseRelationalDatabaseDomain obj){
		Map<String, Object> queryCondition = new HashMap<String, Object>();
		putField2Map(obj, queryCondition, "");
		return deleteByQuery(obj.getClass(), queryCondition);
	}
	
	/**
	 * 以where为查询条件删除数据库记录
	 * 
	 * @param where 查询条件
	 * 
	 * @return 删除的记录数（失败返回-1）
	 */
	public final static int deleteByQuery(Class<? extends BaseRelationalDatabaseDomain> clazz,Map<String, Object> where){
		try {
			beginTransaction();
			Query query = createQuery(where, getDeleteHql(clazz, where));
			int count = query.executeUpdate();
			if(managTransaction(true)){
				return count;
			}else {
				return -1;
			}
		} catch (Exception e) {
			managTransaction(false);
			setException(e);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 级联删除,已不推荐使用,推荐通过重写实体类的delete方法来实现级联删除
	 * 
	 * @param domain 目标实体类
	 * 
	 * @return 是否删除成功(即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 */
	@Deprecated
	public final static boolean cascadeDelete(BaseRelationalDatabaseDomain domain,List<Class<?>> domainSkip){
		beginTransaction();
		if (domainSkip == null) {
			domainSkip = new ArrayList<Class<?>>();
		}
		boolean delete = deleteConneced(domain,Integer.MAX_VALUE,domainSkip);
		managTransaction(delete);
		return delete;
	}
	
	
	/**
	 * 删除数据库记录
	 * 
	 * @param clazz 要删除的实体类
	 * @param id 实体类id
	 * 
	 * @return 是否删除成功(即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 */
	public final static boolean delete(Class<? extends BaseRelationalDatabaseDomain> clazz,Serializable id){
		BaseRelationalDatabaseDomain newInstance;
		try {
			newInstance = clazz.newInstance();
			DomainUtil.setDomainId(newInstance, id);
			return delete(newInstance);
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error(clazz+"实例化失败！！",e);
			throw new RuntimeException(clazz+"实例化失败！！",e);
		}
	}
	
	/**
	 * 通过id删除实体类方法,不会会自动把ids转换成实体类id类型,比如实体类id为Long,ids不可以传string
	 * @param clazz 要删除的实体类
	 * @param ids 实体类id,注意,若实体类id为Long类型,这里就只能传Long类型的参数
	 * @return 删除的记录数（失败返回-1）
	 * 
	 * @see #smartDeleteByIds(Class, String...)
	 */
	public final static int deleteByIds(Class<? extends BaseRelationalDatabaseDomain> clazz,Serializable... ids){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(DomainUtil.getDomainIDName(clazz)+"_not_join", ids);
		return deleteByQuery(clazz, map);
	}
	/**
	 * 聪明的通过id删除实体类方法,会自动把ids转换成实体类id类型,比如实体类id为Long,ids一样可以传string
	 * @param clazz 要删除的实体类
	 * @param ids 实体类id,实体类id为Long类型,这里会自动把String转换为Long
	 * @return 删除的记录数（失败返回-1）
	 */
	public final static int smartDeleteByIds(Class<? extends BaseRelationalDatabaseDomain> clazz,String... ids){
		Class<? extends Serializable> idClass = DomainUtil.getDomainIDClass(clazz);
		Serializable[] serializables;
		if (idClass == String.class) {
			serializables = ids;
		}else{
			serializables = new Serializable[ids.length];
			for (int i = 0; i < serializables.length; i++) {
				serializables[i] = (Serializable) ReflectUtil.convertFromString(idClass, ids[i]);
			}
		}
		return deleteByIds(clazz, serializables);
	}
	
	/**
	 * 根据domain的id删除对应的数据库记录
	 * @param domain 要删除的实体类,调用该方法之前必须保证domain的id不为null
	 * @return 是否删除成功(即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 */
	public final static boolean delete(BaseRelationalDatabaseDomain domain){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			session.delete(domain);
			return managTransaction(true);
		} catch(NonUniqueObjectException e1){
			try {
				session.delete(smartGet(domain));
				return managTransaction(true);
			} catch (Exception e) {
				setException(e);
				return managTransaction(false);
			}
		}catch (Exception e) {
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	private static boolean deleteConneced(BaseRelationalDatabaseDomain obj,int level,List<Class<?>> domainSkip){
		if (level-- == 0) {
			return true;
		}
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			BaseRelationalDatabaseDomain smartGet = DaoUtil.smartGet(obj);
			if (smartGet == null) {
				return true;
			}
			for (Field temp : smartGet.getClass().getDeclaredFields()) {
				try {
					Class<?> type = temp.getType();
					if (!domainSkip.contains(temp)) {
						if (BaseRelationalDatabaseDomain.class.isAssignableFrom(type)) {
							//TODO 支持javax.persistence.OneToOne外的其他OneToOne注解
							if (ReflectUtil.getAnnotation(OneToOne.class, smartGet.getClass(), temp) != null) {
								temp.setAccessible(true);
								BaseRelationalDatabaseDomain object = (BaseRelationalDatabaseDomain) temp.get(smartGet);
								if (!BaseUtil.isObjEmpty(object)) {
									if (deleteConneced(object,level,domainSkip)) {
										session.delete(object);
									} else {
										return false;
									}
								}
							}
						} else if (Collection.class.isAssignableFrom(type)) {
							if (ReflectUtil.getAnnotation(OneToMany.class, smartGet.getClass(), temp) != null) {
								temp.setAccessible(true);
								Collection<?> collection = (Collection<?>) temp.get(smartGet);
								// 获取Collection泛型，看是不是BaseRelationalDatabaseDomain，然后循环删除
								try {
									Class<?> c = ReflectUtil.getGenericType(temp);
									if (BaseRelationalDatabaseDomain.class.isAssignableFrom(c)) {
										for(Object o:collection){
											if(!deleteConneced((BaseRelationalDatabaseDomain)o, level, domainSkip)){
												return false;
											}
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			session.delete(obj);
			return managTransaction(true);
		} catch (NonUniqueObjectException e1) {
			try {
				session.delete(smartGet(obj));
				return managTransaction(true);
			} catch (Exception e) {
				setException(e);
				return managTransaction(false);
			}
		} catch (Exception e) {
			setException(e);
			return managTransaction(false);
		} finally {
			closeSession();
		}
	}
	
	/**
	 * 分页查询实体类
	 * @param t 查询的实体类
	 * @param pageSize 分页大小
	 * @param currentPage 当前页码
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @param queryRecordCount 是否查询总记录数
	 * @return 查询结果(page)
	 * 
	 * @see #paging(int, int, Query)
	 */
	public final static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,List<String> desc,List<String> asc,boolean queryRecordCount){
		return listCustomField(t, pageSize, currentPage, null, desc, asc, queryRecordCount);
	}
	
	/**
	 * 分页查询实体类
	 * @param t 查询的实体类
	 * @param pageSize 分页大小
	 * @param currentPage 当前页码
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @return 查询结果(page)
	 * 
	 * @see Page
	 * @see #listObj(Class, int, int, List, List, boolean)
	 * @see #paging(int, int, Query)
	 */
	public final static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,List<String> desc,List<String> asc){
		return listObj(t, pageSize, currentPage, desc, asc, true);
	}
	
	/**
	 * 查询所有实体类
	 * @param t 查询的实体类
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @return 查询到的所有记录
	 * 
	 * @see Page
	 * @see #listObj(Class, int, int, List, List, boolean)
	 */
	public final static <T extends BaseRelationalDatabaseDomain> List<T> listAllObj(Class<T> t,List<String> desc,List<String> asc){
		return listAllObj(t, null, desc, asc);
	}
	
	/**
	 * 查询所有实体类
	 * @param t 查询的实体类
	 * @return 查询到的所有记录
	 * 
	 * @see #listObj(Class, int, int, List, List, boolean)
	 */
	public final static <T extends BaseRelationalDatabaseDomain> List<T> listAllObj(Class<T> t){
		return listAllObj(t, null, null, null);
	}
	
	/**
	 * 查询单个实体类,若要通过id查某条记录,请用{@link #get(Class, Serializable)} 或 {@link #load(Class, Serializable)}方法
	 * 
	 * @param where 查询条件
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null)
	 */
	public final static <T> T listOne(T where){
		return listOne(where,null,null);
	}
	
	/**
	 * 查询单个实体类
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null)
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T listOne(Class<T> target,Map<String, Object> where,List<String> desc,List<String> asc){
		try {
			StringBuffer hql = getSelectHql(target, where, desc, asc);
			
			Query query = createQuery(where,hql);
			paging(1, 1, query);
			return (T) query.uniqueResult();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 查询实体类的某几个字段(只查第一行记录)
	 * 
	 * @param where 查询条件
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null),当selectedFields.length &gt; 1时,返回map&lt;String,Object&gt;
	 * 当 selectedFields.length == 1时返回 直接返回单个查询字段
	 */
	public final static <T> T listOneFields(BaseDatabaseDomain where,String... selectedFields){
		return listOneFields(where.getClass(), DaoUtil.putField2Map(where), null, null,selectedFields);
	}
	
	/**
	 * 查询实体类的某几个字段
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null),当selectedFields.length &gt; 1时,返回map&lt;String,Object&gt;
	 * 当 selectedFields.length == 1时返回 直接返回单个查询字段
	 * 
	 * 
	 */
	public final static <T> T listOneFields(Class<?> target, Map<String, Object> where,String... selectedFields){
		return listOneFields(target, where, null, null,selectedFields);
	}
	
	/**
	 * 查询一条数据库记录,记录为指定的某几个字段
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null),当selectedFields.length &gt; 1时,返回map&lt;String,Object&gt;
	 * 当 selectedFields.length == 1时返回 直接返回查询到字段值
	 */
	public final static <T> T listOneFields(Class<?> target, Map<String, Object> where, List<String> desc, List<String> asc, String... selectedFields){
		return listOneFields(target, where, desc, asc, null,selectedFields);
	}
	
	/**
	 * 查询单个实体类的某几个字段
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null),当selectedFields.length &gt; 1时,返回map&lt;String,Object&gt;
	 * 当 selectedFields.length == 1时返回 直接返回查询到字段值
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T listOneFields(Class<?> target, Map<String, Object> where, List<String> desc, List<String> asc, String[] groupBy, String... selectedFields){
		try {
			StringBuffer hql = getSelectHql(target, where, desc, asc, groupBy, selectedFields);
			Query query = createQuery(where,hql);
			paging(1, 1, query);
			return (T) query.uniqueResult();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 查询单个实体类
	 * 
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @return 符合查询条件的第一个记录(没有符合查询条件的结果时返回null)
	 */
	@SuppressWarnings("unchecked")
	public final static <T> T listOne(T where,List<String> desc,List<String> asc){
		return (T) listOne(where.getClass(), putField2Map(where), desc, asc);
	}
	
	/**
	 * 例如:文章里有个用户实体类，
	 * 但是前台传过来的只有用户的id，我想获取用户的其他信息就要查数据库，
	 * 调用该方法会把target关联的所有BaseRelationalDatabaseDomain查出来,无需手工查询
	 * @param target 要填充关联实体类的实体类对象
	 */
	public final static void getRefrenceById(BaseDomain target){
		for (Field f:target.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Object temp;
			try {
				temp = f.get(target);
				if (temp != null & temp instanceof BaseRelationalDatabaseDomain) {
					f.set(target, smartGet((BaseRelationalDatabaseDomain)temp));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 获取所有符合查询条件的记录
	 * 
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * 
	 * @return 符合查询条件的所有记录
	 */
	public final static <T> List<T> listAllObj(T where,List<String> desc,List<String> asc){
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) where.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(where, map, "");
		return listAllObj(clazz, map,desc,asc);
	}
	
	/**
	 * 获取所有符合查询条件的记录
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param where 查询条件
	 * @return 符合查询条件的所有记录
	 */
	public final static <T> List<T> listAllObj(T where){
		return listAllObj(where,null,null);
	}
	
	
	/**
	 * 获取所有符合查询条件的记录
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * 
	 * @return 符合查询条件的所有记录
	 */
	@SuppressWarnings("unchecked")
	public final static <T> List<T> listAllObj(Class<T> target,Map<String, Object> where,List<String> desc,List<String> asc){
		return (List<T>) listAllCustomField(target, where, desc, asc);
	}
	
	/**
	 * 用where做查询条件查询的数据库是否存在对应的记录
	 * @param where 查询条件
	 * @return 结果是否存在
	 */
	public final static boolean isResultNull(BaseRelationalDatabaseDomain where){
		//TODO where包含id时,where其它条件被忽略bug
		return isResultNull(where.getClass(), DaoUtil.putField2Map(where));
	}
	
	/**
	 * 用where做查询条件查询的数据库是否存在对应的记录
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @return 结果是否不存在
	 */
	public final static boolean isResultNull(Class<?> target,Map<String, Object> where){
		try {
			Query query = getSession().createQuery(getSelectHql(target, where, null, null,"1").toString());
			setMapParam(where, query);
			paging(1, 1, query);
			
			return query.uniqueResult() == null;
		} catch (Exception e) {
			setException(e);
			return false;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 获取记录数
	 * @param where 查询条件
	 * @return 记录数
	 */
	public final static long getCount(BaseRelationalDatabaseDomain where){
		Class<? extends BaseRelationalDatabaseDomain> t = where.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(where, map, "");
		return getCount(t, map);
	}
	
	/**
	 * 查询实体类指定的字段
	 * @param where 查询条件
	 * @param selectedFields  要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格 可以带'.' 比如 user.role.name
	 * @param <T> 要查询的实体类
	 * 
	 * @return 符合查询条件的所有记录(List),当selectedFields长度为0时,返回List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,返回List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,返回List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	public final static <T> List<T> listAllCustomField(BaseDomain where,String... selectedFields){
		return listAllCustomField(DaoUtil.putField2Map(where), where.getClass(), selectedFields);
	}
	/**
	 * 查询实体类指定的字段
	 * @param where 查询条件
	 * @param selectedFields  要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格 可以带'.' 比如 user.role.name
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @param groupBy hql group by部分语句,不需要请传null
	 * 
	 * @return 符合查询条件的所有记录(List),当selectedFields长度为0时,返回List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,返回List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,返回List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	public final static <T> List<T> listAllCustomField(BaseDomain where,List<String> desc,List<String> asc,String[] groupBy,String... selectedFields){
		return listAllCustomField(where.getClass(), DaoUtil.putField2Map(where), desc, asc, groupBy, selectedFields);
	}
	
	/**
	 * 查询实体类指定的字段
	 * @param where 查询条件
	 * @param selectedFields  要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格 可以带'.' 比如 user.role.name
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * 
	 * @return 符合查询条件的所有记录(List),当selectedFields长度为0时,返回List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,返回List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,返回List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	public final static <T> List<T> listAllCustomField(BaseDomain where,List<String> desc,List<String> asc,String... selectedFields){
		return listAllCustomField(where.getClass(), DaoUtil.putField2Map(where), desc, asc, null, selectedFields);
	}
	
	/**
	 * 查询实体类指定的字段
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param where 查询条件
	 * @param target 要查询的实体类
	 * @param selectedFields  要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格 可以带'.' 比如 user.role.name
	 * 
	 * @return 符合查询条件的所有记录(List),当selectedFields长度为0时,返回List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,返回List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,返回List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	public final static <T> List<T> listAllCustomField(Map<String, Object> where,Class<?> target,String... selectedFields){
		return listAllCustomField(target, where, null, null, selectedFields);
	}
	
	/**
	 * 查询实体类指定的字段
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @param selectedFields  要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格 可以带'.' 比如 user.role.name
	 * 
	 * @return 符合查询条件的所有记录(List),当selectedFields长度为0时,返回List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,返回List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,返回List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	public final static <T> List<T> listAllCustomField(Class<?> target,Map<String, Object> where,List<String> desc,List<String> asc,String... selectedFields){
		return listAllCustomField(target, where, desc, asc, null, selectedFields);
	}
	
	/**
	 * 查询实体类指定的字段
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 不需要请传null
	 * @param asc 需要升序排列的字段 不需要请传null
	 * @param groupBy hql group by部分语句,不需要请传null
	 * @param selectedFields  要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格 可以带'.' 比如 user.role.name
	 * 
	 * @return 符合查询条件的所有记录(List),当selectedFields长度为0时,返回List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,返回List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,返回List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	@SuppressWarnings("unchecked")
	public final static <T> List<T> listAllCustomField(Class<?> target,Map<String, Object> where,List<String> desc,List<String> asc,String[] groupBy,String... selectedFields){
		try {
			Query query = getSession().createQuery(getSelectHql(target, where, desc, asc,groupBy,selectedFields).toString());
			setMapParam(where, query);
			return query.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 获取符合查询条件的记录数
	 * @param target 要查询的实体类
	 * @param where 查询条件
	 * @return 符合查询条件的记录数
	 */
	public final static long getCount(Class<? extends BaseRelationalDatabaseDomain> target,Map<String, Object> where){
		try {
			Query query = getSession().createQuery(getCountHql(getSelectHql(target, where, null, null)).toString());
			setMapParam(where, query);
			return (Long) query.uniqueResult();
		} catch (Exception e) {
			setException(e);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 分页查询实体类
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param pageSize 分页大小
	 * @param currentPage 当前页码
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @return 查询结果(page)
	 * 
	 * @see #paging(int, int, Query)
	 */
	@SuppressWarnings("unchecked")
	public final static <T> Page<T> listObj(int pageSize,int currentPage,T where,List<String> desc,List<String> asc){
		Class<T> t = (Class<T>) where.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(where, map, "");
		return listObj(t,pageSize,currentPage,map,desc,asc) ;
	}
	
	/**
	 * 随机取size条记录
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param size 记录数
	 * @param where 查询条件
	 * @return 符合查询条件的记录
	 */
	@SuppressWarnings("unchecked")
	public final static <T> List<T> randomlistObj(int size,T where){
		Class<T> t = (Class<T>) where.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(where, map, "");
		return randomlistObj(t, size, map);
	}
	
	/**
	 * 随机取size条记录
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param size 记录数
	 * @param where 查询条件
	 * 
	 * @return 查询到的记录
	 */
	@SuppressWarnings("unchecked")
	public final static <T> List<T> randomlistObj(Class<T> target,int size,Map<String, Object> where){
		try {
//			List<String> randList = new ArrayList<String>(1);
//			randList.add("RAND()");
			StringBuffer hql = getSelectHql(target, where, null, null);
			hql.append(" order by RAND()");
			Query query = createQuery(where,hql);
			paging(size,1, query);
			return query.list();
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 查询target里面指定的字段
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param pageSize 分页大小
	 * @param currentPage 当前页码
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @param queryRecordCount 是否查询总记录数(记录很多时查询较费时间),若传false,则返回的page实体类的记录数为Long.MAX_VALUE,<br>
	 * 			前端可做无限分页
	 * 
	 * @return 当前页的记录
	 */
	public final static <T> Page<T> listObj(Class<T> target,int pageSize,int currentPage,Map<String, Object> where,List<String> desc,List<String> asc,boolean queryRecordCount){
		return listCustomField(target, pageSize, currentPage, where, desc, asc, queryRecordCount);
	}
	
	public final static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,Map<String, Object> map,List<String> desc,List<String> asc){
		return listObj(t, pageSize, currentPage, map, desc, asc, true);
	}
	
	/**
	 * 查询target里面指定的字段
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的实体类
	 * @param pageSize 分页大小
	 * @param currentPage 当前页码
	 * @param where 查询条件
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @param queryRecordCount 是否查询总记录数(记录很多时查询较费时间),若传false,则返回的page实体类的记录数为Long.MAX_VALUE,<br>
	 * 			前端可做无限分页
	 * @param selectField 要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格若不传,则查询该类所有字段,page里面放的是实体类,否则放的是map,不过map里面的key的'.'会被替换成'__'<br>
	 * 
	 * @return  符合查询条件的当前页记录(page里面放的是List),当selectedFields长度为0时,page里面的数据为List&lt;target&gt;<br>
	 * 		当selectedFields长度只有1时,page里面的数据为List&lt;selectedField&gt;<br>
	 * 		当selectedFields长度&gt;1时,page里面的数据为List&lt;Map&lt;selectedField,value&gt;&gt;<br>
	 */
	public final static <T> Page<T> listCustomField(Class<?> target,int pageSize,int currentPage,Map<String, Object> where,
			List<String> desc,List<String> asc,boolean queryRecordCount,String... selectField){
		return listCustomField(target, pageSize, currentPage, where, desc, asc, queryRecordCount, null, selectField);
	}
	
	/**
	 * 查询target里面指定的字段
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param target 要查询的类
	 * @param pageSize 分页大小
	 * @param currentPage 当前页码
	 * @param where 查询条件
	 * @param desc
	 * @param asc
	 * @param queryRecordCount 是否查询总记录数(记录很多时查询较费时间),若传false,则返回的page实体类的记录数为Long.MAX_VALUE,<br>
	 * 			前端可做无限分页
	 * @param selectField 要查询的字段,可以传sum( name ) count( id ) 之类的函数,注意 括号之间要有空格若不传,则查询该类所有字段,page里面放的是实体类,否则放的是map,不过map里面的key的'.'会被替换成'__'<br>
	 * 			
	 * @return
	 */
	public final static <T> Page<T> listCustomField(Class<?> target,int pageSize,int currentPage,Map<String, Object> where,
			List<String> desc,List<String> asc,boolean queryRecordCount,String[] groupBy,String... selectField){
		try {
			StringBuffer hql = getSelectHql(target, where, desc, asc, groupBy, selectField);
			Long recordCount = (long) Integer.MAX_VALUE;
			if (queryRecordCount) {
				recordCount = getRecordCount(where, hql);
			}
			
			Query query = createQuery(where,hql);
			
			paging(pageSize,currentPage, query);
			@SuppressWarnings("unchecked")
			List<T> list = query.list();
			
			return setPage(currentPage, recordCount, pageSize, list);
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 根据实体类id加载实体类
	 * 
	 * @param <T> 要加载的实体类
	 * 
	 * @param domain 要查询的实体类,调用该方法之前必须保证domain的id不为null
	 * 
	 * @return 查询到的实体类的代理对象(记录不存则返回null)
	 * 
	 * @see #get(Class, Serializable)
	 */
	@SuppressWarnings("unchecked")
	public final static <T extends BaseRelationalDatabaseDomain> T smartLoad(T domain){
		return (T) load(domain.getClass(), DomainUtil.getDomainId(domain));
	}
	
	/**
	 * 根据实体类id查询实体类
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param domain 要查询的实体类,调用该方法之前必须保证domain的id不为null
	 * 
	 * @return 查询到的记录(记录不存在则返回null)
	 */
	@SuppressWarnings("unchecked")
	public final static <T extends BaseRelationalDatabaseDomain> T smartGet(T domain){
		return (T) get(domain.getClass(), DomainUtil.getDomainId(domain));
	}
	
	/**
	 * 根据实体类id查询实体类,关联的对象也会全部查询出来,性能较差,但序列化时不需要做特殊处理
	 * 
	 * @param <T> 要查询的实体类
	 * 
	 * @param clazz 要查询的实体类
	 * @param id 实体类id
	 * 
	 * @return 查询到的记录(记录不存在则返回null)
	 * 
	 * @see #load(Class, Serializable)
	 */
	public final static <T extends BaseRelationalDatabaseDomain> T get(Class<T> clazz,Serializable id){
		try {
			T t2 = (T) getSession().get(clazz, id);
			return t2;
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 根据实体类id加载实体类,关联的对象不会全部查询出来,用到的时候才查询,性能较好,
	 * 序列化时需要做特殊处理或调用 {@link BaseRelationalDatabaseDomain#initializeAll()}initialize关联的实体类  ,也可以使用{@link #get(Class, Serializable)}
	 * 
	 * @param <T> 要加载的实体类
	 * 
	 * @param clazz 要加载的实体类
	 * @param id 实体类id
	 * 
	 * @return 查询到的实体类的代理对象(记录不存则返回null)
	 */
	public final static <T extends BaseRelationalDatabaseDomain> T load(Class<T> clazz,Serializable id){
		try {
			Session session = getSession();
			return session.load(clazz, id);
		} catch (Exception e) {
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	private static boolean shouldCommitNow(){
		return (autoManagTransaction.get() == null && Config.autoCommitTransaction) || Boolean.TRUE == autoManagTransaction.get();
	}
	
	
	/**
	 *  管理事务
	 * @param isCommit 
	 * @return 事务是提交还是回滚
	 */
	public static boolean managTransaction(Boolean isCommit){
		if (shouldCommitNow()) {
			if (isCommit == null || isCommit) {
				commitTransaction();
				return true;
			}else {
				rollbackTransaction();
			}
			return isCommit;
		}else if (isCommit != null) {
			Boolean type = transactionType.get();
			if (type == null) {
				transactionType.set(isCommit);
			}else{
				transactionType.set(type&&isCommit);
			}
			return transactionType.get();
		}else {
			return true;
		}
	}
	
	/**
	 * 开启事务
	 */
	public final static void beginTransaction(){
		if (currentTransaction.get() == null) {
			logger.debug("开启事务.....");
//			transactionType.set(true);
			currentTransaction.set(HibernateUtil.getSession().beginTransaction());
		}
	}
	
	private static Transaction getTransaction(){
		return currentTransaction.get();
	}
	
	/***
	 * 管理事务(你不用管他是提交还是回滚,框架会自动判断.如果多个数据库操作中的一个出错了,这个方法会自动回滚事务,否则提交事务)
	 * 
	 * @see #rollbackTransaction
	 * @see #commitTransaction
	 * @return 事务是成功提交还是回滚
	 */
	public final static boolean managTransaction(){
		Boolean boolean1 = transactionType.get();
		try {
			if (boolean1 == null && exception.get() == null) {
				if (currentTransaction.get() != null) {
					logger.info("当前事务未进行写操作,回滚事务,防止对查询出来的实体类的更改保存到数据库..");
					rollbackTransaction();
				}
				return true;
			}
			if (exception.get() == null && boolean1) {
				commitTransaction();
			}else {
				rollbackTransaction();
			}
			return boolean1;
		} catch (Exception e) {
			try {
				rollbackTransaction();
			} catch (Exception e2) {
				setException(e2);
			}
			setException(e);
			return false;
		}finally {
			relese();
		}
	}
	
	/**
	 * 释放数据库连接等资源
	 */
	public final static void relese(){
		transactionType.remove();
		autoManagTransaction.remove();
		currentTransaction.remove();
		exception.remove();
		throwException.remove();
		HibernateUtil.release();
	}
	
	/**
	 * 提交事务,不推荐直接调用该方法提交事务,推荐用{@link #managTransaction()} 让系统判断是提交事务还是回滚事务.
	 * @see #managTransaction
	 */
	public final static void commitTransaction(){
		Transaction transaction = getTransaction();
		if (transaction != null) {
			transaction.commit();
			logger.debug("提交事务.....");
		}
		currentTransaction.remove();
	}
	
	/**
	 * 回滚事务
	 * @see #managTransaction()
	 */
	public final static void rollbackTransaction(){
		Transaction transaction = getTransaction();
		if (transaction != null) {
			transaction.rollback();
			getSession().clear();
			logger.debug("回滚事务并清空session.....");
		}
		currentTransaction.remove();
		relese();
	}
	
	/**
	 * 保存实体类
	 * 
	 * @param domain
	 * 
	 * @return 是否保存成功 (即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 */
	public final static boolean save(BaseRelationalDatabaseDomain domain){
		try {
			Session session = getSession();
			beginTransaction();
			session.save(domain);
			return managTransaction(true);
		} catch (Exception e) {
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	/**
	 * update实体类中不为空的字段
	 * 
	 * @param domain 要update的实体类
	 * @param updateEvenNull 即使为空也update到数据库中的字段,没有请传null
	 * 
	 * @return 是否更新成功(即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 */
	@Deprecated
	public final static boolean updateNotNullField(BaseRelationalDatabaseDomain domain,List<String> updateEvenNull){
		return updateNotNullField(domain, updateEvenNull, false);
	}
	
	/**
	 * update实体类中不为空的字段,该方法不会触发实体类的update方法,无法做update触发操作,
	 * 推荐用 {@link BaseRelationalDatabaseDomain#updateNotNullField(List) }
	 * 
	 * @param domain 要update的实体类
	 * @param updateEvenNull 即使为空也update到数据库中的字段,没有请传null
	 * @param strictlyMode 严格模式，如果为true则 字段==null才算空，
	 * 	否则调用BaseUtil.isObjEmpty判断字段是否为空
	 * 
	 * @return 是否更新成功(即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 * 
	 * @see BaseUtil#isObjEmpty
	 * @see DomainUtil#fillDomain
	 * @see BaseRelationalDatabaseDomain#updateNotNullField(List)
	 */
	@Deprecated
	public final static boolean updateNotNullField(BaseRelationalDatabaseDomain domain,List<String> updateEvenNull,boolean strictlyMode){
		BaseRelationalDatabaseDomain smartGet = smartGet(domain);
		DomainUtil.fillDomain(smartGet, domain,updateEvenNull,strictlyMode);
		return smartGet.update();
	}
	
	/*public final static boolean updateNotNullFieldByHql(BaseRelationalDatabaseDomain obj,List<String> updateEvenNull,boolean strictlyMode){
		BaseRelationalDatabaseDomain smartGet = smartGet(obj);
		DomainUtil.fillDomain(smartGet, obj,updateEvenNull,strictlyMode);
		return smartGet.update();
	}*/
	
	/**
	 * update整个实体类
	 * 
	 * @param domain 要update的实体类
	 * 
	 * @return 是否更新成功(即使返回true,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用)
	 * 
	 * @see #updateNotNullField(BaseRelationalDatabaseDomain, List, boolean)
	 */
	public final static boolean update(BaseRelationalDatabaseDomain domain){
		try {
			Session session = getSession();
			beginTransaction();
			session.update(domain);
			return managTransaction(true);
		} catch (Exception e) {
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 不要求list里面所有对象一起update成功或失败,update单位是单个对象,若事务失败了,数据库操作一样会失败,所以该返回值只做参考用
	 * 
	 * @see #updateList
	 * @param list 要update的对象
	 * 
	 * @return update失败的对象数
	 * 
	 */
	public final static int updateListOneByOne(List<? extends BaseRelationalDatabaseDomain> list){
		int failed = 0;
		for (BaseRelationalDatabaseDomain o:list) {
			if (!update(o)) {
				failed++;
			}
		}
		return failed;
	}
	/***************************增删查改结束******************************/
	
	
	
	
	/***************************数据库工具开始******************************/
	
	/**
	 * 获取更新用的hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param queryCondition where条件
	 * @param updated 要更新的'字段-&gt;值'
	 * @return 拼好的hql
	 */
	public final static <T> StringBuffer getUpdateHql(Class<T> t,
			Map<String, Object> queryCondition,Map<String, Object> updated) {
		String fullClassName = t.getName();
		StringBuffer hql = new StringBuffer();
		String domainSimpleName = getDomainSimpleName(fullClassName);
		
		hql.append(" update ")
			.append(fullClassName)
			.append(" ")
			.append(domainSimpleName);
		//TODO 级联update
//		
//		Set<String> innerJoin = new HashSet<String>();
//		if (queryCondition != null) {
//			for(String temp:queryCondition.keySet()){
//				if (temp.contains(".") && !(queryCondition.get(temp) instanceof HqlGenerator)) {
//					String chain = getMaxDepthDomainChain(temp, t);
//					if (chain != null) {
//						innerJoin.add(chain);
//					}
//				}
//			}
//		}
//		
//		for(String temp:innerJoin){
//			hql.append(" inner join ").append(domainSimpleName).append(".")
//			.append(temp).append(" ");
//		}
		
		hql.append(" set ");
		appendHqlUpdateSet(hql, domainSimpleName, updated);
		
		
		hql.append(" where 1=1 ");
//		getHql(t, where, desc, asc, prefix, groupBy, selectedFields)
		appendHqlWhere(domainSimpleName, hql, queryCondition);
		
		logger.debug("hql------>"+hql.toString());
		return hql;
	}
	
	/**
	 * 获取删除用的hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param where 查询条件
	 * @return 拼好的hql
	 */
	public final static StringBuffer getDeleteHql(Class<? extends BaseRelationalDatabaseDomain> t,Map<String, Object> where) {
		return getHql(t, where, null, null,"delete");
	}
	
	/**
	 * 获取查询用的hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param where 查询条件
	 * @param desc 降序排列字段
	 * @param asc 升序排列字段
	 * @return 拼好的hql
	 */
	public final static <T> StringBuffer getSelectHql(Class<T> t,Map<String, Object> where,List<String> desc,List<String> asc,String... selectedFields) {
		return getHql(t, where, desc, asc,"select",null,selectedFields);
	}
	
	/**
	 * 获取查询用的hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param where 查询条件
	 * @param desc 降序排列字段
	 * @param asc 升序排列字段
	 * @return 拼好的hql
	 */
	public final static <T> StringBuffer getSelectHql(Class<T> t,Map<String, Object> where,List<String> desc,List<String> asc,String[] groupBy,String... selectedFields) {
		return getHql(t, where, desc, asc,"select",groupBy, selectedFields);
	}
	/**
	 * 
	 * 获取hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param queryMap 查询数据
	 * @param desc 降序排列字段
	 * @param asc 升序排列字段
	 * @param prefix hql前面部分目前只支持"select"或"delete"
	 * @return 拼好的hql
	 */
	public final static <T> StringBuffer getHql(Class<T> t, Map<String, Object> queryMap,List<String> desc,
			List<String> asc,String prefix,String... selectedFields) {
		return getHql(t, queryMap, desc, asc, prefix, null,selectedFields);
	}
	/**
	 * 
	 * 获取hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param where 查询条件
	 * @param desc 降序排列字段
	 * @param asc 升序排列字段
	 * @param prefix hql前面部分目前只支持"select"或"delete"
	 * @param groupBy hql group by 的字段
	 * @return 拼好的hql
	 */
	public final static <T> StringBuffer getHql(Class<T> t, Map<String, Object> where,List<String> desc,
			List<String> asc,String prefix,String[] groupBy,String... selectedFields) {
		String fullClassName = t.getName();
		StringBuffer hql = new StringBuffer();
		String domainSimpleName = getDomainSimpleName(fullClassName);
		
		hql.append(prefix).append(" ");
		
		Set<String> domainSelected = new HashSet<>();
		Set<String> innerJoin = new HashSet<>();
		if ("select".equals(prefix)) {
			if ((selectedFields == null || selectedFields.length == 0)) {
				hql.append(domainSimpleName);
			}else{
				if (selectedFields.length > 1) {
					hql.append("new map( ");
				}
				//
				Map<String, Integer> dealSpecialCharMap = new HashMap<>();
				for(String temp:selectedFields){
					Pattern pattern = RegUtil.getPattern("^\\s*\\d+\\s*$");
					if (pattern.matcher(temp).find()) {
						hql.append(temp).append(",");
						continue;
					}
					
					String selectedField = praseRealSelectedField(temp);
						//sum(weight),count(id)之类的
						// 0    1
						//temp = temp.replace(matcher.group(1), domainSimpleName+"."+matcher.group(1));
							//TODO 支持二级及以上实体类加减乘除操作
							//TODO 类似a/1 1前面不加实体类前缀,a/b和a-b生成的别名是一样的,a-b可以改成a-b-0
					String replace = dealMathChar(domainSimpleName, selectedField);
					String dealedSelectedField;
					String dealSpecialChar;
					if (!"*".equals(selectedField)) {
						dealedSelectedField = temp.replace(selectedField, domainSimpleName+"."+replace);
						
						addSelectedDomain(t, domainSelected, selectedField);
						dealSpecialChar = dealSpecialChar(selectedField);
					}else {
						//count(*)的情况
						dealedSelectedField = temp;
						dealSpecialChar = dealSpecialChar(temp.replace(")", "").replace("(", ""));
					}
					
					//修复 同时select Count(name),name 时生成的别名都为name,导致返回的map只有一个key的bug
					if (dealSpecialCharMap.containsKey(dealSpecialChar)) {
						int count = dealSpecialCharMap.get(dealSpecialChar);
						dealSpecialChar += "_steed"+count;
						dealSpecialCharMap.put(dealSpecialChar, ++count);
					}else {
						dealSpecialCharMap.put(dealSpecialChar, 0);
					}
					
					hql.append(dealedSelectedField)
						.append(" as ").append(dealSpecialChar)
						.append(",");
				}
				
				hql.deleteCharAt(hql.length()-1);
				if (selectedFields.length > 1) {
					hql.append(" )");
				}
				
			}
		}/*else {
			hql.append(" from ")
			.append(fullClassName)
			.append(" ")
			.append(domainSimpleName);
		}*/
		
		dealSortFieldsJoin(t, desc, domainSelected);
		
		dealSortFieldsJoin(t, asc, domainSelected);
		
		if (where != null && !"delete".equals(prefix)) {
			for(String temp:where.keySet()){
				if (temp.contains(".") && !(where.get(temp) instanceof HqlGenerator)) {
					String chain = getMaxDepthDomainChain(temp, t);
					if (chain != null) {
						if (chain.contains(".")) {
							innerJoin.add(chain);
						}else {
							//company.id这类,通过关联的实体类id查询的,不需要inner join,company.name之类才需要
							Field field = ReflectUtil.getDeclaredField(t, chain);
							if (field == null) {
								throw new IllegalArgumentException(t.getName() + "中没有" + chain + "字段!");
							}
							@SuppressWarnings("unchecked")
							String domainIDName = DomainUtil.getDomainIDName((Class<? extends BaseDomain>) field.getType());
							if (!temp.substring(chain.length()+1).equals(domainIDName)) {
								innerJoin.add(chain);
							}
						}
					}
				}
			}
		}
		
		hql.append(" from ")
		.append(fullClassName)
		.append(" ")
		.append(domainSimpleName);
		
		for(String temp:domainSelected){
			if (innerJoin.contains(temp)) {
				continue;
			}
			hql.append(" left join ").append(domainSimpleName).append(".")
				.append(temp).append(" ");
		}
		for(String temp:innerJoin){
			hql.append(" left join ").append(domainSimpleName).append(".")
			.append(temp).append(" ");
		}
		
		hql.append(" where  1=1 ");
		
		
		hql = appendHqlWhere(domainSimpleName, hql, where);
		
		if (groupBy != null && groupBy.length > 0) {
			int i = 0;
			hql.append(" group by ");
			for (String temp:groupBy) {
				hql.append(" ").append(domainSimpleName).append(".").append(temp).append(" ");
				if (++i < groupBy.length) {
					hql.append(",");
				}
			}
		}
		//含有distinct语句,不能order by
		boolean containsDistinct = false;
		if (selectedFields != null) {
			for (String field:selectedFields) {
				if (field.toLowerCase().contains("distinct ")) {
					containsDistinct = true;
					break;
				}
			}
		}
		
		if (!containsDistinct && desc == null && asc == null) {
			DefaultOrderBy defaultOrderBy2 = defaultOrderBy.get(t);
			if (defaultOrderBy2 != null) {
				if (defaultOrderBy2.desc()) {
					desc = Arrays.asList(defaultOrderBy2.value());
				}else {
					asc = Arrays.asList(defaultOrderBy2.value());
				}
			}
		}
		//select 1 from (isResultNull方法) 不需要排序,提升性能.
		if (!hql.toString().trim().toLowerCase().startsWith("delete ") && ( selectedFields == null || selectedFields.length != 1 || !selectedFields[0].equals("1"))) {
			appendHqlOrder(hql, desc, asc, domainSimpleName);
		}
		
		if (where != null && where.get(personalHqlGeneratorKey) != null) {
			((HqlGenerator)where.get(personalHqlGeneratorKey)).afterHqlGenered(domainSimpleName, hql, where);
		}
		
		logger.debug("hql------>%s",hql.toString());
		logger.debug("参数----->%s",where==null?null:where.toString());
		
		return hql;
	}
	
	private static String dealMathChar(String domainSimpleName, String selectedField) {
		String replace = selectedField.replace("/", "/"+domainSimpleName+".")
				.replace("+", "+"+domainSimpleName+".")
				.replace("-", "-"+domainSimpleName+".")
				.replace("*", "*"+domainSimpleName+".").replace(" ", "");
		return replace;
	}
	
	/**
	 * 解析真正要select的字段(去除count(),sum()等)
	 * 
	 * @param originalField 要查询的字段
	 * @return 真正要select的字段
	 */
	private static String praseRealSelectedField(String originalField) {
		Matcher matcher = RegUtil.getPattern(".+\\((.+)\\)").matcher(originalField);
//					Matcher matcher = RegUtil.getPattern(".+\\([(.+)|(\\S+\\s+(\\S+)\\s.)]\\)").matcher(temp);
		String selectedField = originalField;
		if (matcher.find()) {
			selectedField = matcher.group(1);
		}
		Matcher fieldMatcher = RegUtil.getPattern("\\s*(\\S+)\\s*(\\S*)\\s*").matcher(selectedField);
		if (fieldMatcher.find()) {
			selectedField = fieldMatcher.group(2);
		}
		if (StringUtil.isStringEmpty(selectedField)) {
			selectedField = fieldMatcher.group(1);
		}
		
		return selectedField;
	}
	private static <T> void addSelectedDomain(Class<T> t, Set<String> domainSelected, String selectedField) {
		if (selectedField.contains(".")) {
			String chain = getMaxDepthDomainChain(selectedField, t);
			if (chain != null) {
				domainSelected.add(chain);
			}
		}
	}
	
	/**
	 * 排序字段含有关联实体类时hibernate生成的sql 默认inner join 关联表,得在select后指明left join...
	 * @param t
	 * @param desc
	 * @param domainSelected
	 */
	private static <T> void dealSortFieldsJoin(Class<T> t, List<String> desc, Set<String> domainSelected) {
		if (desc != null) {
			for(String temp:desc){
				if (temp.contains(".")) {
					String chain = getMaxDepthDomainChain(temp, t);
					if (chain != null) {
						domainSelected.add(chain);
					}
				}
			}
		}
	}
	
	private static String dealSpecialChar(String group) {
		return group.replace(".", "__").replace("\r", "").replace("*", "_").replace("/", "_").replace("-", "_").replace("+", "_");
	}
	
	/**
	 * 截取带点的字段仅包含实体类最长的一段如 company.user.id,返回 company.user , company.user.school.name 返回company.user.school
	 * @param chain
	 * @param clazz
	 * @return
	 */
	private static String getMaxDepthDomainChain(String chain,Class<?> clazz){
		String maxDepthDomainChain = getMaxDepthDomainChain(clazz, getNoSelectIndexFieldName(chain));
		if (!StringUtil.isStringEmpty(chain) && maxDepthDomainChain.endsWith(".")) {
			return maxDepthDomainChain.substring(0,maxDepthDomainChain.length()-1);
		}
		return maxDepthDomainChain;
	}
	
	@SuppressWarnings("unchecked")
	private static String getMaxDepthDomainChain(Class<?> clazz,String chain){
		if (StringUtil.isStringEmpty(chain)) {
			return null;
		}
		ReflectResult chainField = ReflectUtil.getChainField(clazz, chain);
		boolean isId = false;
		if (chainField != null && BaseDatabaseDomain.class.isAssignableFrom(chainField.getTarget())) {
			if (!(isId = chainField.getField().getAnnotation(Id.class) != null)) {
				Method iDmethod = DomainUtil.getIDmethod((Class<? extends BaseDomain>) chainField.getTarget());
				if (iDmethod != null) {
					String fieldName = chainField.getField().getName();
					isId = StringUtil.getFieldGetterName(fieldName).equals(iDmethod.getName()) 
							|| StringUtil.getFieldIsMethodName(fieldName).equals(iDmethod.getName());
				}
			}
		}
		if (isId && chain.contains(".")) {
			chain = chain.substring(0, chain.lastIndexOf("."));
		}
		
		if (!isId && chainField != null && BaseDatabaseDomain.class.isAssignableFrom(chainField.getField().getType())) {
			return chain;
		}else if (chain.contains(".")) {
			return getMaxDepthDomainChain(clazz, chain.substring(0, chain.lastIndexOf(".")));
		}else if (chainField != null && BaseDatabaseDomain.class.isAssignableFrom(chainField.getTarget())) {
			return chain;
		}
		throw new IllegalArgumentException(clazz.getName()+"中找不到字段"+chain);
	}
	
	/**
	 * 根据查询对象生成hql
	 * @param domain
	 * @param desc 需要降序排列的字段
	 * @param asc 需要升序排列的字段
	 * @return 生成的hql
	 */
	public final static StringBuffer getSelectHql(BaseRelationalDatabaseDomain domain,List<String> desc,List<String> asc) {
		Class<?> clazz = domain.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(domain, map, "");
		return getSelectHql(clazz, map, desc, asc);
	}
	
	/**
	 * 获取该条查询的记录总数
	 * @param hql
	 * @return
	 */
	public final static StringBuffer getCountHql(StringBuffer hql) {
		return changeSelectHql(hql, "count(*)");
	}
	
	/**
	 * 获取该条查询的记录总数
	 * @param hql
	 * @return
	 */
	public final static StringBuffer changeSelectHql(StringBuffer hql,String selectedField) {
		//select people_steed_00 inner join people_steed_00.roleSet people_steed_00roleSet from steed.domain.people.People people_steed_00 where 1=1 
		//and people_steed_00roleSet in (:roleSet) and people_steed_00roleSet not in (:roleSet_not_in_1)
		StringBuffer countHql = new StringBuffer(hql);
		Pattern p = RegUtil.getPattern("select .+? from");
		Matcher m = p.matcher(countHql);
		if (m.find()) {
			countHql.replace(m.start(0), m.end(0), "select " + selectedField + " from");
		}else {
			countHql.insert(0, "select " + selectedField + " ");
		}
		logger.debug("countHql--->"+countHql);
		return countHql;
	}
	
	/**
	 * 
	 * 生成query并设置查询参数,使用例子:<br><code>
	 * Map&lt;String, Object&gt; param = new Map&lt;String, Object&gt;();<br>
		param.put("id", schoolId);<br>
		String updateName = "schoolUpdated";<br>
		param.put("updateName", updateName);<br>
		Query createSQLQuery = DaoUtil.createQuery(param, " update School set name=:updateName where id=:id ");<br>
		createSQLQuery.executeUpdate();<br>
	 * </code>
	 * @param param 参数,存放sql中类似 update School set name=:updateName where id=:id 中的 id,updateName对应的值
	 * @param hql
	 * @return
	 */
	public final static Query createQuery(Map<String, Object> param,StringBuffer hql) {
		return createQuery(param, hql.toString());
	}
	/**
	 * 
	 * 生成query并设置查询参数,使用例子:<br><code>
	 * Map&lt;String, Object&gt; param = new Map&lt;String, Object&gt;();<br>
		param.put("id", schoolId);<br>
		String updateName = "schoolUpdated";<br>
		param.put("updateName", updateName);<br>
		Query createSQLQuery = DaoUtil.createQuery(param, " update School set name=:updateName where id=:id ");<br>
		createSQLQuery.executeUpdate();<br>
	 * </code>
	 * @param param 参数,存放sql中类似 update School set name=:updateName where id=:id 中的 id,updateName对应的值
	 * @param hql
	 * @return
	 */
	public final static Query createQuery(Map<String, Object> param,String hql) {
		logger.debug("hql---->"+hql);
		logger.debug("参数---->"+param);
		Query query = getSession().createQuery(hql);
		setMapParam(param, query);
		openTransactionIfNeed(hql);
		return query;
	}
	/**
	 * 生成SQLquery并设置查询参数,使用例子:<br><code>
	 * Map&lt;String, Object&gt; param = new Map&lt;String, Object&gt;();<br>
		param.put("id", schoolId);<br>
		String updateName = "schoolUpdated";<br>
		param.put("updateName", updateName);<br>
		Query createSQLQuery = DaoUtil.createSQLQuery(param, " update School set name=:updateName where id=:id ");<br>
		createSQLQuery.executeUpdate();<br>
	 * </code>
	 * @param param 参数,存放sql中类似 update School set name=:updateName where id=:id 中的 id,updateName对应的值
	 * @param sql
	 * @return
	 */
	public final static Query createSQLQuery(Map<String, Object> param,String sql) {
		logger.debug("sql---->" + sql);
		logger.debug("参数---->" + param);
		Query query = getSession().createSQLQuery(sql.toString());
		setMapParam(param, query);
		openTransactionIfNeed(sql);
		return query;
	}

	private static void openTransactionIfNeed(String sql) {
		String lowerCase = sql.trim().toLowerCase();
		if (lowerCase.startsWith("update ") || lowerCase.startsWith("delete ") || lowerCase.startsWith("insert ")) {
			managTransaction(true);
		}
	}
	/**
	 * 生成SQLquery并设置查询参数,使用例子:<br><code>
	 * Map&lt;String, Object&gt; param = new Map&lt;String, Object&gt;();<br>
		param.put("id", schoolId);<br>
		String updateName = "schoolUpdated";<br>
		param.put("updateName", updateName);<br>
		Query createSQLQuery = DaoUtil.createSQLQuery(param, new StringBuffer(" update School set name=:updateName where id=:id "));<br>
		createSQLQuery.executeUpdate();<br>
	 * </code>
	 * @param param 参数,存放sql中类似 update School set name=:updateName where id=:id 中的 id,updateName对应的值
	 * @param sql
	 * @return
	 */
	public final static Query createSQLQuery(Map<String, Object> param,StringBuffer sql) {
		return createSQLQuery(param, sql.toString());
	}
	
	public final static Session getSession(){
		Session session = HibernateUtil.getSession();
		if (Config.autoBeginTransaction) {
			beginTransaction();
		}
		return session;
	}
	
	/**
	 * 组装hql的order和order by 部分
	 * @param hql 要组装的hql
	 * @param desc 
	 * @param asc
	 * @param domainSimpleName
	 * @return 组装后的hql
	 */
	public final static StringBuffer appendHqlOrder(StringBuffer hql,List<String> desc,List<String> asc,String domainSimpleName){
		boolean hasOrderByAppened = appendHqlOrder(hql, desc, domainSimpleName, false,true);
		hasOrderByAppened = appendHqlOrder(hql, asc, domainSimpleName, hasOrderByAppened,false);
		return hql;
	}
	
	private static boolean appendHqlOrder(StringBuffer hql, List<String> orderField, String domainSimpleName,
			boolean hasOrderByAppened,boolean desc) {
		String order = desc ? "desc":"asc";
		if (orderField != null && !orderField.isEmpty()) {
			for (String name:orderField) {
				
				String selectedField = praseRealSelectedField(name);
				String replace = dealMathChar(domainSimpleName, selectedField);
						
				String dealedSelectedField = name.replace(selectedField, domainSimpleName+"."+replace);
				if (!hasOrderByAppened) {
					hql.append("order by ");
					hasOrderByAppened = true;
				}else {
					hql.append(", ");
				}
				hql.append(" ").append(dealedSelectedField).append(" ").append(order);
			}
		}
		return hasOrderByAppened;
	}
	
	
	/**
	 * 根据配置关闭session
	 */
	private static void closeSession(){
		managTransaction(null);
		if (HibernateUtil.getColseSession() && shouldCommitNow()) {
			managTransaction();
			HibernateUtil.closeSession();
		}
	}
	/**
	 * 无论是否配置了框架管理session,马上关闭session
	 */
	public final static void closeSessionNow(){
		managTransaction();
		HibernateUtil.closeSession();
	}
	/***************************数据库工具结束********************************/
	
	
	
	/***************************内部方法开始********************************/
	
	/**
	 * 获取domain的简称用作查询时的别名
	 * @param fullClassName 全类名
	 * @return domain的简称
	 */
	private static String getDomainSimpleName(String fullClassName) {
		String domainSimpleName = StringUtil.firstChar2LowerCase(StringUtil.getClassSimpleName(fullClassName)+"_steed_00");
		return domainSimpleName;
	}

	/**
	 *  把domain中非空字段放到map
	 * @param domain
	 * @return map
	 */
	public final static Map<String, Object> putField2Map(Object domain) {
		Map<String, Object> map = new HashMap<>();
		putField2Map(domain, map, "");
		return map;
	}
	/**
	 * 把domain中非空字段放到map
	 */
	public final static void putField2Map(Object domain,Map<String, Object> map,String prefixName) {
		putField2Map(domain, map, prefixName, true);
	}
	
	/**
	 * <code>{@link DaoUtil#putField2Map(Object) }</code>的拦截器,
	 * 实体类可实现该接口,来实现数据权限控制等,例如:
	 * 一个多公司系统,用户只能看自己公司的数据,那么,设计实体类的时候,就可以
	 * 让所有要实现"用户只能看自己公司的数据"功能的实体类都继承BaseCompanyDomain,然后
	 * BaseCompanyDomain实现PutField2MapIntercepter接口,
	 * 在beforePutField2Map方法,把自己的公司字段设置为当前登陆用户所属公司,这样就只能查出当前公司的数据了
	 * 
	 * 
	 * @author 战马
	 * @see DaoUtil#putField2Map(Object, Map, String, boolean)
	 */
	public interface PutField2MapIntercepter{
		
		/**
		 * 
		 * @param map obj字段容器,用来生成hql或sqlwhere部分的查询条件
		 * @param prefixName 前缀,当put user里面的school时,prefixName="user.",原理比较复杂,具体可以看源码
		 * @param getFieldByGetter 是否用Getter方法来获取字段值,若传false,则用field.getValue直接获取字段值
		 * @return 是否继续执行DaoUtil#putField2Map(Object, Map, String, boolean)
		 * 
		 * @see DaoUtil#putField2Map(Object, Map, String, boolean)
		 */
		public boolean beforePutField2Map(Map<String, Object> map,String prefixName,boolean getFieldByGetter);
		public default void afterPutField2Map(Map<String, Object> map,String prefixName,boolean getFieldByGetter) {};
		
	}
	
	/**
	 * 
	 * @param domain 要put到map的对象
	 * @param map obj字段容器,用来生成hql或sqlwhere部分的查询条件
	 * @param prefixName 前缀,当put user里面的school时,prefixName="user.",原理比较复杂,具体可以看源码
	 * @param getFieldByGetter 是否用Getter方法来获取字段值,若传false,则用field.getValue直接获取字段值
	 * 
	 * @see DaoUtil.PutField2MapIntercepter
	 */
	public final static void putField2Map(Object domain,Map<String, Object> map,String prefixName,boolean getFieldByGetter) {
		if (domain == null) {
			return;
		}
		QueryFilter<?>[] filters = Config.queryFilterManager.getFilters(domain.getClass());
		if (filters != null) {
			for(QueryFilter f:filters) {
				if(!f.beforePutField2Map(domain, map, prefixName, getFieldByGetter)) {
					return;
				}
			}
		}
		if (domain instanceof PutField2MapIntercepter) {
			if(!((PutField2MapIntercepter)domain).beforePutField2Map(map, prefixName, getFieldByGetter)){
				return;
			}
		}
		try {
			boolean containID = false;
			
			try {
				if ((domain instanceof BaseDomain)) {
					Serializable domainId = DomainUtil.getDomainId((BaseDomain) domain);
					if (!BaseUtil.isObjEmpty(domainId)) {
						map.put(prefixName + DomainUtil.getIDfield((Class<? extends BaseDomain>) domain.getClass()).getName(), domainId);
						containID = true;
					}
				}
			} catch (Exception e) {
				logger.info("尝试获取实体类id失败,"+e.getMessage());
			}
			
			if (!containID) {
				Class<? extends Object> objClass = domain.getClass();
				List<Field> Fields = ReflectUtil.getNotFinalFields(domain);
				for (Field f:Fields) {
					String fieldName = f.getName();
					if (map.containsKey(prefixName+fieldName)) {
						continue;
					}
					//不是索引字段且标有Transient即跳过
					if (isSelectIndex(fieldName) == 0 && !DomainUtil.isDatabaseField(objClass, f)) {
						continue;
					}
					
					Object value = null;
					if (getFieldByGetter) {
						value = ReflectUtil.getFieldValueByGetter(domain, fieldName);
					}
					if (value == null) {
						f.setAccessible(true);
						value = f.get(domain);
					}
					if (!BaseUtil.isObjEmpty(value)) {
						if (value instanceof BaseRelationalDatabaseDomain ) {
							JoinColumn annotation = ReflectUtil.getAnnotation(JoinColumn.class, objClass, f);
							if (annotation == null 
									|| !(!annotation.insertable() 
											&& !annotation.updatable())) {
								map.put(prefixName + fieldName, value);
							}
						}else {
							map.put(prefixName + fieldName, value);
						}
					}else if (value instanceof BaseRelationalDatabaseDomain 
							&& !(value instanceof BaseUnionKeyDomain)
							&& BaseUtil.isObjEmpty(DomainUtil.getDomainId((BaseDomain) value))) {
						//实体类级联查询支持,离0hql的伟大构想已经非常接近了!
						putField2Map(value, map,prefixName + fieldName +".");
					}
				}
			}
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.debug("putField2Map出错",e);
		}
		if (filters != null) {
			for(QueryFilter f:filters) {
				f.afterPutField2Map(domain, map, prefixName, getFieldByGetter);
			}
		}
		if (domain instanceof PutField2MapIntercepter) {
			((PutField2MapIntercepter)domain).afterPutField2Map(map, prefixName, getFieldByGetter);
		}
	}
	
	/**
	 * 是否属于查询后缀
	 * @param fieldName 字段名..
	 * @return 如果不是查询后缀返回0,是则返回查询后缀长度...方便subString拿到真实字段名
	 */
	public final static int isSelectIndex(String fieldName){
		for (String suffix:indexSuffix) {
			if (fieldName.endsWith(suffix)) {
				if (suffix.equals(orGroup)) {
					int lastIndexOf = fieldName.replace(orGroup, "").lastIndexOf("_");
					return fieldName.length()-lastIndexOf;
				}
				return suffix.length();
			}
		}
		return 0;
	}
	
	/**
	 * 获取没有查询后缀的fieldName
	 * @param fieldName 查询字段(没有查询后缀也一样可以传过来,不会抛异常,这设计的比较巧妙,可以自己看源码研究
	 * 		<code>{@link steed.hibernatemaster.util.DaoUtil#isSelectIndex}</code>
	 * @return fieldName去掉查询后缀后的名字,也就是真正的字段名(若fieldName没含有查询后缀,则会原样返回)
	 */
	public final static String getNoSelectIndexFieldName(String fieldName){
		int selectIndex = isSelectIndex(fieldName);
		if (selectIndex == 0) {
			return fieldName;
		}
		return fieldName.substring(0,fieldName.length() - selectIndex);
	}
	
	/**
	 * 往where里面put("personalHqlGenerator",{@link HqlGenerator} );
	 * 即可跳过默认的HqlGenerator,用个性化HqlGenerator生成hql
	 * 
	 * 组装参数到hql的where部分
	 * 
	 * @param domainSimpleName 类简称
	 * @param hql 要append的hql
	 * @param where where查询条件
	 * 
	 * @see HqlGenerator
	 */
	public final static StringBuffer appendHqlWhere(String domainSimpleName, StringBuffer hql,
			Map<String, Object> where) {
		if (where == null || where.isEmpty()) {
			return hql;
		}
		Object object = where.get(personalHqlGeneratorKey);
		if (object != null && object instanceof HqlGenerator) {
			where.remove(personalHqlGeneratorKey);
			StringBuffer appendHqlWhere = ((HqlGenerator)object).appendHqlWhere(domainSimpleName, hql, where);
			where.put(personalHqlGeneratorKey, object);
			return appendHqlWhere;
		}
		return Config.defaultHqlGenerator.appendHqlWhere(domainSimpleName, hql, where);
	}
	
	/**
	 * 组装update类型的hql中set部分语句
	 * 
	 * @param hql 要组装的hql
	 * @param domainSimpleName 实体类简称
	 * @param updated 要更新的字段
	 */
	private static void appendHqlUpdateSet(StringBuffer hql,String domainSimpleName,Map<String, Object> updated){
		for (String temp:updated.keySet()) {
			hql.append(domainSimpleName)
				.append(".")
				.append(temp)
				.append("=:steedUpdate_")
				.append(temp)
				.append(", ");
		}
		hql.deleteCharAt(hql.length()-2);
	}
	
	/**
	 * 获取记录数
	 * @param where 查询条件
	 * @param selectHql 普通的select hql,会自动转换成查询记录数的hql
	 * @return 记录数
	 */
	private static long getRecordCount(Map<String, Object> where,StringBuffer selectHql) {
		Query query = createQuery(where, getCountHql(selectHql));
		try {
			return (Long) query.uniqueResult();
		} catch (NonUniqueResultException e) {
			return (long) query.list().size();
		}catch (NullPointerException e) {
			return 0;
		}
	}

	/**
	 * 设置page的大小，当前页等
	 * @param currentPage 当前页码,从1开始
	 * @param recordCount 总记录数
	 * @param pageSize 分页大小
	 * @param list 分页数据
	 * @return 设置好分页数据的Page对象
	 */
	private static <T> Page<T> setPage(int currentPage, Long recordCount,
			int pageSize, List<T> list) {
		Page<T> page = new Page<T>();
		page.setCurrentPage(currentPage);
		page.setPageSize(pageSize);
		page.setRecordCount(recordCount);
		page.setDomainCollection(list);
		return page;
	}
	/**
	 * 把map中的查询数据设置到query
	 * @param map map
	 * @param query query
	 */
	private static void setMapParam(Map<String, ? extends Object> map, Query query) {
		if (CollectionsUtil.isCollectionsEmpty(map)) {
			return;
		}
		String key;
		for (Entry<String, ? extends Object> e:map.entrySet()) {
			key = e.getKey();
			if (personalHqlGeneratorKey.equals(key)) {
				continue;
			}
			Object value = e.getValue();
			if (value == null) {
				query.setParameter(key.replace(".", "__"), value);
			}else {
				if (value instanceof Collection) {
					query.setParameterList(key.replace(".", "__"), (Collection<?>) value);
				}else if(value.getClass().isArray()){
					query.setParameterList(key.replace(".", "__"), (Object[]) value);
				}else {
					query.setParameter(key.replace(".", "__"), value);
				}
			}
		}
	}
	/***************************内部方法结束********************************/

	/**
	 * 设置query分页
	 * @param pageSize 分页大小
	 * @param currentPage 当前页面,从1开始
	 * @param query
	 */
	public final static void paging(int pageSize,int currentPage, Query query) {
		query.setFirstResult((currentPage-1)*pageSize);
		query.setMaxResults(pageSize);
	}
	
	/**
	 * 事务数据
	 * @author 战马
	 *
	 */
	public final static class ImmediatelyTransactionData{
		Transaction currentTransaction;
		Boolean autoManagTransaction;
		Session session;
		Boolean transactionType;
		public ImmediatelyTransactionData(Transaction currentTransaction,Boolean autoManagTransaction,Session session) {
			this.currentTransaction = currentTransaction;
			this.autoManagTransaction = autoManagTransaction;
			this.session = session;
		}
	}
}
