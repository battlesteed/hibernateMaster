package steed.hibernatemaster.util;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDomain;
import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.domain.BaseUnionKeyDomain;
import steed.hibernatemaster.domain.Page;
import steed.util.base.BaseUtil;
import steed.util.base.CollectionsUtil;
import steed.util.base.DomainUtil;
import steed.util.base.RegUtil;
import steed.util.base.StringUtil;
import steed.util.logging.Logger;
import steed.util.reflect.ReflectUtil;
/**
 * 实现0sql和0hql伟大构想的dao工具类，用该类即可满足绝大多数数据库操作
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
                                     	葱官赐福   百无禁忌
 * @author 战马
 */
public class DaoUtil {
	private static final ThreadLocal<Boolean> transactionType = new ThreadLocal<>();
	// 具体的错误提示用
	private static final ThreadLocal<Exception> exception = new ThreadLocal<>();
	private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();
	public static final String personalHqlGeneratorKey = "personalHqlGenerator";
	/**
	 * 是否自动提交或回滚事务
	 * 自助事务步骤：
	 * 1,调用setAutoManagTransaction(false)把自动事务设为false
	 * 2,调用managTransaction()管理事务;
	 */
	private static final ThreadLocal<Boolean> autoManagTransaction = new ThreadLocal<>();
	
	private DaoUtil() {
	}

	/**
	 * 查询条件后缀
	 */
	public static final String[] indexSuffix = {"_max_1","_min_1","_like_1","_not_in_1","_not_equal_1","_not_join","_not_null","_not_compile_param",personalHqlGeneratorKey};
	/***********\异常提示专用************/
	
	/*//TODO 完善异常类型
	private static final Integer[] exceptionTypes = {10,11};
	private static final String[] exceptionReasons = {"主键重复","主键未指定"};
	private static final Exception[] exceptions = {};*/
	
	/***********#异常提示专用************/
	public static Exception getExceptiontype() {
		return exception.get();
	}
	public static void setException(Exception exception) {
		DaoUtil.exception.set(exception);
		steed.util.logging.LoggerFactory.getLogger().error("数据库操作发生异常",exception);
	}
	public static Transaction getCurrentTransaction() {
		return currentTransaction.get();
	}
	public static void setCurrentTransaction(Transaction currentTransaction) {
		DaoUtil.currentTransaction.set(currentTransaction);
	}
	public static Boolean getTransactionType() {
		return transactionType.get();
	}
	public static void setTransactionType(Boolean transactionType) {
		DaoUtil.transactionType.set(transactionType);
	}
	public static Boolean getAutoManagTransaction() {
		return autoManagTransaction.get();
	}
	/**
	 * 立即事务开始，框架可能配置了多个数据库操作使用同一事务然后统一提交
	 * 如某些操作可能要马上提交事务，可使用该方法
	 * 用法<br />
	 *  ImmediatelyTransactionData immediatelyTransactionData = DaoUtil.immediatelyTransactionBegin();<br />
	 *  //TODO 这里做其他数据库操作<br />
	 *	DaoUtil.immediatelyTransactionEnd(immediatelyTransactionData);<br />
	 *	
	 * @see #immediatelyTransactionEnd
	 * @return 
	 */
	public static ImmediatelyTransactionData immediatelyTransactionBegin(){
		steed.util.logging.LoggerFactory.getLogger().debug("立即事务开始");
		Session session = getSession();
		Transaction currentTransaction = getCurrentTransaction();
		Boolean autoManagTransaction = getAutoManagTransaction();
		Boolean transactionType = getTransactionType();
		setAutoManagTransaction(true);
		setCurrentTransaction(null);
		setTransactionType(null);
		ImmediatelyTransactionData immediatelyTransactionData = new ImmediatelyTransactionData(currentTransaction, autoManagTransaction,session);
		immediatelyTransactionData.transactionType = transactionType;
		HibernateUtil.setSession(null);
		return immediatelyTransactionData;
	}
	/**
	 * 立即事务结束
	 * @see #immediatelyTransactionBegin
	 * @param immediatelyTransactionData
	 */
	public static void immediatelyTransactionEnd(ImmediatelyTransactionData immediatelyTransactionData){
		HibernateUtil.closeSession();
		DaoUtil.setTransactionType(immediatelyTransactionData.transactionType);
		DaoUtil.setCurrentTransaction(immediatelyTransactionData.currentTransaction);
		DaoUtil.setAutoManagTransaction(immediatelyTransactionData.autoManagTransaction);
		HibernateUtil.setSession(immediatelyTransactionData.session);
		steed.util.logging.LoggerFactory.getLogger().debug("立即事务结束");
	}
	
	
	
	
	public static void setAutoManagTransaction(Boolean selfManagTransaction) {
		DaoUtil.autoManagTransaction.set(selfManagTransaction);;
	}
	/***************************增删查改开始******************************/
	/**
	 */
	public static <T> Page<T> listObj(int pageSize,int currentPage, Class<? extends BaseRelationalDatabaseDomain> t){
		try {
			StringBuffer hql = getSelectHql(t,null,null,null);
			Long recordCount = getRecordCount(null, hql);
			Query query = getSession().createQuery(hql.toString());
			
			faging(pageSize,currentPage, query);
			@SuppressWarnings("unchecked")
			List<T> list = query.list();
			
			return setPage(currentPage, recordCount, pageSize, list);
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	/**
	 * 查询t对应的表中所有记录的主键
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Serializable> listAllObjKey(Class<? extends BaseRelationalDatabaseDomain> t){
		try {
			String name = t.getName();
			String keyName = DomainUtil.getDomainIDName(t);
			String hql = "select "+keyName+" from " + name;
			Query query = getSession().createQuery(hql);
			return query.list();
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	
	public static boolean saveList(List<? extends BaseRelationalDatabaseDomain> list){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			for (Object obj:list) {
				session.save(obj);
			}
			return managTransaction(true);
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	
	/**
	 * 查询所有id在ids里面的实体类
	 * @param t
	 * @param ids 实体类id，英文逗号分割
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> listByKeys(Class<? extends BaseRelationalDatabaseDomain> t,String[] ids){
		try {
			if (ids == null || ids.length == 0) {
				return new ArrayList<>();
			}
			Map<String, Object> map = new HashMap<String, Object>();
			Class<? extends Serializable> idClass = DomainUtil.getDomainIDClass(t);
			Serializable[] serializables;
			if (idClass == String.class) {
				serializables = ids;
			}else{
				serializables = new Serializable[ids.length];
				for (int i = 0; i < serializables.length; i++) {
					serializables[i] = (Serializable) ReflectUtil.convertFromString(idClass, ids[i]);
				}
			}
			map.put(DomainUtil.getDomainIDName(t)+"_not_join", serializables);
			return (List<T>) listAllObj(t, map, null, null);
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @return
	 */
	public static boolean updateList(List<? extends BaseRelationalDatabaseDomain> list){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			for (BaseRelationalDatabaseDomain obj:list ) {
				session.update(obj);
			}
			return managTransaction(true);
		} catch (Exception e) {
			e.printStackTrace();
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
	public static int updateListNotNullFieldOneByOne(List<? extends BaseRelationalDatabaseDomain> list,List<String> updateEvenNull){
		int failed = 0;
		for (Object o:list) {
			if (!updateNotNullField((BaseRelationalDatabaseDomain) o, updateEvenNull)) {
				failed++;
			}
		}
		return failed;
	}
	
	public static void evict(Object obj){
		getSession().evict(obj);
		closeSession();
	}
	/**
	 * 如果数据库有obj对象就update否则save
	 * @param obj
	 * @return
	 */
	public static boolean saveOrUpdate(BaseRelationalDatabaseDomain obj){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			if (BaseUtil.isObjEmpty(DomainUtil.getDomainId(obj))) {
				return save(obj);
			}else {
				BaseRelationalDatabaseDomain smartGet = smartGet(obj);
				if (smartGet != null) {
					session.evict(smartGet);
					session.update(obj);
				}else {
					session.save(obj);
				}
			}
			return managTransaction(true);
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	public static int executeUpdateBySql(String hql,Map<String,? extends Object> map){
		return executeUpdate(hql, map, 1);
	}
	
	public static int executeUpdate(String hql,Map<String,? extends Object> map){
		return executeUpdate(hql, map, 0);
	}
	
	public static Object getUniqueResult(String hql,String domainSimpleName,
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
			e.printStackTrace();
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
	public static List getQueryResult(String hql,String domainSimpleName,
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
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Page getQueryResult(String hql,String domainSimpleName,
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
			faging(pageSize, currentPage, createQuery);
			
			return setPage(currentPage, recordCount, pageSize, createQuery.list());
			//return createQuery.list();
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static List getQueryResultBysql(String sql,Map<String,? extends Object> param){
		try {
			Query createQuery = getSession().createSQLQuery(sql);
			if (param != null) {
				setMapParam(param, createQuery);
			}
			return createQuery.list();
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings("rawtypes")
	public static List getQueryResult(String hql,Map<String,? extends Object> map,int pageSize,int currentPage){
		try {
			Query createQuery = getSession().createQuery(hql);
			if (map != null) {
				setMapParam(map, createQuery);
			}
			faging(pageSize, currentPage, createQuery);
			return createQuery.list();
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return null;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings("rawtypes")
	public static List getQueryResult(String hql,Map<String,? extends Object> map){
		try {
			Query createQuery = getSession().createQuery(hql);
			if (map != null) {
				setMapParam(map, createQuery);
			}
			return createQuery.list();
		} catch (Exception e) {
			setException(e);
			e.printStackTrace();
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
			e.printStackTrace();
			setException(e);
			managTransaction(false);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 通过hql更新数据库，用于批量更新
	 * @param queryCondition 查询条件，同listAllObj的查询条件
	 * @param updated 存放更新的 字段---值
	 * @return 更新的记录数，失败返回-1
	 */
	public static int updateByQuery(BaseDomain queryCondition,Map<String, Object> updated){
		return updateByQuery(queryCondition.getClass(), putField2Map(queryCondition), updated);
	}
	
	/**
	 * 通过hql更新数据库，用于批量更新
	 * @param queryCondition 查询条件，同listAllObj的查询条件
	 * @param updated 存放更新的字段-值
	 * @return 更新的记录数，失败返回-1
	 */
	public static int updateByQuery(Class<?> clazz,Map<String, Object> queryCondition,Map<String, Object> updated){
		try {
			beginTransaction();
			
			StringBuffer updateHql = getUpdateHql(clazz, queryCondition,updated);
			for (Entry<String, Object> temp:updated.entrySet()) {
				queryCondition.put("steedUpdate_"+temp.getKey(), temp.getValue());
			}
			Query query = createQuery(queryCondition, updateHql);
			int count = query.executeUpdate();
			
			if(managTransaction(true)){
				return count;
			}else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			managTransaction(false);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 以obj为查询条件删除数据库记录
	 * @param obj 查询条件
	 * @return 删除的记录数（失败返回-1）
	 */
	public static int deleteByQuery(BaseRelationalDatabaseDomain obj){
		Map<String, Object> queryCondition = new HashMap<String, Object>();
		putField2Map(obj, queryCondition, "");
		return deleteByQuery(obj.getClass(), queryCondition);
	}
	
	/**
	 * 以query为查询条件删除数据库记录
	 * @return 删除的记录数（失败返回-1）
	 */
	public static int deleteByQuery(Class<? extends BaseRelationalDatabaseDomain> clazz,Map<String, Object> queryCondition){
		try {
			beginTransaction();
			Query query = createQuery(queryCondition, getDeleteHql(clazz, queryCondition));
			int count = query.executeUpdate();
			if(managTransaction(true)){
				return count;
			}else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			managTransaction(false);
			setException(e);
			return -1;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 级联删除,不推荐,应该重写实体类delete方法实现级联删除
	 * @param obj
	 * @return
	 */
	@Deprecated
	public static boolean cascadeDelete(BaseRelationalDatabaseDomain obj,List<Class<?>> domainSkip){
		beginTransaction();
		if (domainSkip == null) {
			domainSkip = new ArrayList<Class<?>>();
		}
		boolean delete = deleteConneced(obj,Integer.MAX_VALUE,domainSkip);
		managTransaction(delete);
		return delete;
	}
	
	
	/**
	 * 删除数据库记录
	 * @return
	 */
	public static boolean delete(Class<? extends BaseRelationalDatabaseDomain> clazz,Serializable key){
		BaseRelationalDatabaseDomain newInstance;
		try {
			newInstance = clazz.newInstance();
			DomainUtil.setDomainId(newInstance, key);
			return delete(newInstance);
		} catch (InstantiationException | IllegalAccessException e) {
			steed.util.logging.LoggerFactory.getLogger().error(clazz+"实例化失败！！",e);
			throw new RuntimeException(clazz+"实例化失败！！",e);
		}
	}
	
	/**
	 * 聪明的通过id删除实体类方法,会自动把ids转换成实体类id类型,比如实体类id为Long,ids一样可以传string
	 * @param clazz
	 * @param ids
	 * @return 删除的记录数（失败返回-1）
	 */
	public static int deleteByIds(Class<? extends BaseRelationalDatabaseDomain> clazz,Serializable... ids){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(DomainUtil.getDomainIDName(clazz)+"_not_join", ids);
		return deleteByQuery(clazz, map);
	}
	/**
	 * 聪明的通过id删除实体类方法,会自动把ids转换成实体类id类型,比如实体类id为Long,ids一样可以传string
	 * @param clazz
	 * @param ids
	 * @return 删除的记录数（失败返回-1）
	 */
	public static int smartDeleteByIds(Class<? extends BaseRelationalDatabaseDomain> clazz,String... ids){
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
	 * 删除obj对应的数据库记录
	 * @param obj
	 * @return
	 */
	public static boolean delete(BaseRelationalDatabaseDomain obj){
		Session session = null;
		try {
			session = getSession();
			beginTransaction();
			session.delete(obj);
			return managTransaction(true);
		} catch(NonUniqueObjectException e1){
			try {
				session.delete(smartGet(obj));
				return managTransaction(true);
			} catch (Exception e) {
				e.printStackTrace();
				setException(e);
				return managTransaction(false);
			}finally{
				closeSession();
			}
		}catch (Exception e) {
			e.printStackTrace();
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
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			session.delete(obj);
			return true;
		} catch (NonUniqueObjectException e1) {
			try {
				session.delete(smartGet(obj));
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				setException(e);
				return false;
			} finally {
				closeSession();
			}
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return false;
		} finally {
			closeSession();
		}
	}
	
	/*private static boolean isContainReferenceAnnotation(Field f,Class<?> clazz){
		List<Annotation> annotations = ReflectUtil.getAnnotations(clazz, f);
		if (annotations.contains(OneToOne.class)) {
			return true;
		}else if(annotations.contains(on.class)){
			
		}
	}*/
	
	
	/**
	 * 
	 * @param t
	 * @param currentPage
	 * @param pageSize
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @param queryRecordCount 是否查询总记录数
	 * @return
	 */
	public static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,List<String> desc,List<String> asc,boolean queryRecordCount){
		try {
			StringBuffer hql = getSelectHql(t, null, desc, asc);
			Long recordCount = Long.MAX_VALUE;
			if (queryRecordCount) {
				recordCount = getRecordCount(null, hql);
			}
			
			Query query = createQuery(null, hql);
			faging(pageSize,currentPage, query);
			@SuppressWarnings("unchecked")
			List<T> list = query.list();
			
			Page<T> page = setPage(currentPage, recordCount, pageSize, list);
			return page;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * @param t
	 * @param currentPage
	 * @param pageSize
	 * @param desc 需要降序排列的字段 可以为null
	 * @param asc 需要升序排列的字段 可以为null
	 * @return
	 * 
	 * @see #listObj(Class, int, int, List, List, boolean)
	 */
	public static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,List<String> desc,List<String> asc){
		return listObj(t, pageSize, currentPage, desc, asc, true);
	}
	
	public static <T extends BaseRelationalDatabaseDomain> List<T> listAllObj(Class<T> t,List<String> desc,List<String> asc){
		return listAllObj(t, null, desc, asc);
	}
	
	public static <T extends BaseRelationalDatabaseDomain> List<T> listAllObj(Class<T> t){
		/*try {
			Query query = createQuery(null, getSelectHql(t, null, null, null));
			List list = query.list();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}*/
		return listAllObj(t, null, null, null);
	}
	
	public static <T> T listOne(T t){
		return listOne(t,null,null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T listOne(Class<T> t,Map<String, Object> queryMap,List<String> desc,List<String> asc){
		try {
			StringBuffer hql = getSelectHql(t, queryMap, desc, asc);
			
			Query query = createQuery(queryMap,hql);
			faging(1, 1, query);
			return (T) query.uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T listOne(T t,List<String> desc,List<String> asc){
		return (T) listOne(t.getClass(), putField2Map(t), desc, asc);
	}
	
	/**
	 * 比如说，文章里有个用户实体类，
	 * 但是前台传过来的只有用户的id，我想获取用户的其他信息就要查数据库，
	 * 调用该方法会把baseDomain关联的所有BaseRelationalDatabaseDomain查出来
	 * @param baseDomain
	 */
	public static void getRefrenceById(BaseDomain baseDomain){
		for (Field f:baseDomain.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			Object temp;
			try {
				temp = f.get(baseDomain);
				if (temp != null & temp instanceof BaseRelationalDatabaseDomain) {
					f.set(baseDomain, smartGet((BaseRelationalDatabaseDomain)temp));
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	
	
	/**
	 * 获取所有查询对象
	 * @param <T>
	 * @param t 查询对象参数
	 * @return
	 */
	public static <T> List<T> listAllObj(T t,List<String> desc,List<String> asc){
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) t.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(t, map, "");
		return listAllObj(clazz, map,desc,asc);
	}
	/**
	 * 获取所有查询对象
	 * @param <T>
	 * @param t 查询对象参数
	 * @return
	 */
	public static <T> List<T> listAllObj(T t){
		return listAllObj(t,null,null);
	}
	
	/**
	 * 用obj做查询条件查询的结果集是否为空
	 * @param obj 查询条件
	 * @return
	 */
	public static boolean isResultNull(BaseRelationalDatabaseDomain obj){
		return isResultNull(obj.getClass(), DaoUtil.putField2Map(obj));
	}
	
	/**
	 * 用obj做查询条件查询的结果集是否为空
	 * @param obj 查询条件
	 * @return
	 */
	public static boolean isResultNull(Class<?> clazz,Map<String, Object> where){
		try {
			Query query = getSession().createQuery(getCountHql(getSelectHql(clazz, where, null, null)).toString());
			setMapParam(where, query);
			faging(1, 1, query);
			return ((Long) query.uniqueResult()) == 0;
		} catch (Exception e) {
			setException(e);
			return false;
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 获取记录数
	 * @param query
	 * @return
	 */
	public static long getCount(BaseRelationalDatabaseDomain query){
		Class<? extends BaseRelationalDatabaseDomain> t = query.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(query, map, "");
		return getCount(t, map);
	}
	/**
	 * 获取所有查询对象
	 * @param t
	 * @param constraint 查询参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> listAllObj(Class<T> t,Map<String, Object> constraint,List<String> desc,List<String> asc){
		return (List<T>) listAllCustomField(t, constraint, desc, asc);
	}
	/**
	 * 获取所有实体类指定的字段
	 * @param constraint 查询参数
	 * @param selectedFields 要查询的字段 可以带'.' 比如 user.role.name
	 * @return
	 */
	public static <T> List<T> listAllCustomField(BaseDomain constraint,String... selectedFields){
		return listAllCustomField(DaoUtil.putField2Map(constraint), constraint.getClass(), selectedFields);
	}
	/**
	 * 获取所有实体类指定的字段
	 * @param t
	 * @param constraint 查询参数
	 * @param selectedFields 要查询的字段 可以带'.' 比如 user.role.name
	 * @return
	 */
	public static <T> List<T> listAllCustomField(Map<String, Object> constraint,Class<?> t,String... selectedFields){
		return listAllCustomField(t, constraint, null, null, selectedFields);
	}
	/**
	 * 获取所有实体类指定的字段
	 * @param t
	 * @param constraint 查询参数
	 * @param desc
	 * @param asc
	 * @param selectedFields 要查询的字段 可以带'.' 比如 user.role.name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> listAllCustomField(Class<?> t,Map<String, Object> constraint,List<String> desc,List<String> asc,String... selectedFields){
		try {
			Query query = getSession().createQuery(getSelectHql(t, constraint, desc, asc,selectedFields).toString());
			setMapParam(constraint, query);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	/**
	 * 获取该查询条件能查到的记录数
	 * @param t
	 * @param map 查询数据
	 * @return
	 */
	public static long getCount(Class<? extends BaseRelationalDatabaseDomain> t,Map<String, Object> map){
		try {
			Query query = getSession().createQuery(getCountHql(getSelectHql(t, map, null, null)).toString());
			setMapParam(map, query);
			return (Long) query.uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}finally{
			closeSession();
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> Page<T> listObj(int pageSize,int currentPage,T obj,List<String> desc,List<String> asc){
		Class<T> t = (Class<T>) obj.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(obj, map, "");
		return listObj(t,pageSize,currentPage,map,desc,asc) ;
	}
	/**
	 * 随机取size条记录
	 * @param size
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> randomlistObj(int size,T obj){
		Class<T> t = (Class<T>) obj.getClass();
		Map<String, Object> map = new HashMap<String, Object>();
		putField2Map(obj, map, "");
		return randomlistObj(t, size, map);
	}
	
	/**
	 * 随机取size条记录
	 * @param t
	 * @param size 记录数
	 * @param map 查询条件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> randomlistObj(Class<T> t,int size,Map<String, Object> map){
		try {
			List<String> randList = new ArrayList<String>(1);
			randList.add("RAND()");
			StringBuffer hql = getSelectHql(t, map, null, null);
			hql.append(" order by RAND()");
			Query query = createQuery(map,hql);
			faging(size,1, query);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	public static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,Map<String, Object> map,List<String> desc,List<String> asc,boolean queryRecordCount){
		return listCustomField(t, pageSize, currentPage, map, desc, asc, queryRecordCount);
	}
	public static <T> Page<T> listObj(Class<T> t,int pageSize,int currentPage,Map<String, Object> map,List<String> desc,List<String> asc){
		return listObj(t, pageSize, currentPage, map, desc, asc, true);
	}
	
	/**
	 * 查询t里面指定的字段
	 * @param t 要查询的类
	 * @param pageSize
	 * @param currentPage
	 * @param constraint 查询条件
	 * @param desc
	 * @param asc
	 * @param queryRecordCount 是否查询总记录数(记录很多时查询较费时间),若传false,则返回的page实体类的记录数为Long.MAX_VALUE,<br>
	 * 			前端可做无限分页
	 * @param selectField 要查询的字段,若不传,则查询该类所有字段,page里面放的是实体类,否则放的是map,不过map里面的key的'.'会被替换成'__'<br>
	 * 			(这个是可变参数,没有请不传,切忌传null!)
	 * @return
	 */
	public static <T> Page<T> listCustomField(Class<?> t,int pageSize,int currentPage,Map<String, Object> constraint,
			List<String> desc,List<String> asc,boolean queryRecordCount,String... selectField){
		try {
			StringBuffer hql = getSelectHql(t, constraint, desc, asc,selectField);
			Long recordCount = Long.MAX_VALUE;
			if (queryRecordCount) {
				recordCount = getRecordCount(constraint, hql);
			}
			
			Query query = createQuery(constraint,hql);
			
			faging(pageSize,currentPage, query);
			@SuppressWarnings("unchecked")
			List<T> list = query.list();
			
			return setPage(currentPage, recordCount, pageSize, list);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseRelationalDatabaseDomain> T smartLoad(T domain){
		return (T) load(domain.getClass(), DomainUtil.getDomainId(domain));
	}
	/**
	 * 聪明的get方法
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseRelationalDatabaseDomain> T smartGet(T domain){
		return (T) get(domain.getClass(), DomainUtil.getDomainId(domain));
	}
	public static <T extends BaseRelationalDatabaseDomain> T get(Class<T> t,Serializable key){
		try {
			T t2 = (T) getSession().get(t, key);
			return t2;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	
	public static <T extends BaseRelationalDatabaseDomain> List<T> getList(Class<T> t,Serializable[] keys){
		try {
			List<T> list = new ArrayList<T>();
			for (Serializable s:keys) {
				list.add(get(t, s));
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			closeSession();
		}
	}
	
	
	
	public static <T extends BaseRelationalDatabaseDomain> T load(Class<T> t,Serializable key){
		try {
			Session session = getSession();
			return session.load(t, key);
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @return 
	 */
	private static boolean managTransaction(Boolean isCommit){
		if (shouldCommitNow()) {
			if (isCommit) {
				commitTransaction();
			}else if(!isCommit){
				rollbackTransaction();
			}
			return isCommit;
		}else{
			Boolean type = transactionType.get();
			if (type == null) {
				transactionType.set(isCommit);
			}else{
				transactionType.set(type&&isCommit);
			}
			return transactionType.get();
		}
	}
	/**
	 * 开启事务
	 */
	public static void beginTransaction(){
		if (currentTransaction.get() == null) {
			steed.util.logging.LoggerFactory.getLogger().debug("开启事务.....");
//			transactionType.set(true);
			currentTransaction.set(HibernateUtil.getSession().beginTransaction());
		}
	}
	public static Transaction getTransaction(){
		return currentTransaction.get();
	}
	
	/***
	 * 管理事务(你不用管他是提交还是回滚,框架会自动判断.如果多个数据库操作中的一个出错了,这个方法会自动回滚事务,否则提交事务)
	 * 
	 * @see #rollbackTransaction
	 * @see #commitTransaction
	 * @return 事务是成功提交还是回滚
	 */
	public static boolean managTransaction(){
		Boolean boolean1 = transactionType.get();
		try {
			if (boolean1 == null) {
				if (currentTransaction.get() != null) {
					steed.util.logging.LoggerFactory.getLogger().debug("当前事务未进行写操作,回滚事务,防止对查询出来的实体类的更改保存到数据库..");
					rollbackTransaction();
				}
				return true;
			}
			if (boolean1) {
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
	 * 释放资源
	 */
	public static void relese(){
		transactionType.remove();
		autoManagTransaction.remove();
		currentTransaction.remove();
		exception.remove();
	}
	
	/**
	 * 提交事务,不推荐直接调用该方法提交事务,推荐用下面这个方法让系统判断是提交事务还是回滚事务.
	 * @see #managTransaction
	 */
	public static void commitTransaction(){
		Transaction transaction = getTransaction();
		if (transaction != null) {
			transaction.commit();
			steed.util.logging.LoggerFactory.getLogger().debug("提交事务.....");
		}
		currentTransaction.remove();
	}
	
	/**
	 * 回滚事务
	 * @see #managTransaction()
	 */
	public static void rollbackTransaction(){
		Transaction transaction = getTransaction();
		if (transaction != null) {
			transaction.rollback();
			steed.util.logging.LoggerFactory.getLogger().debug("回滚事务.....");
		}
		currentTransaction.remove();
	}
	
	public static boolean save(BaseRelationalDatabaseDomain obj){
		try {
			Session session = getSession();
			beginTransaction();
			session.save(obj);
			return managTransaction(true);
		} catch (Exception e) {
			e.printStackTrace();
			setException(e);
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	/**
	 * update实体类中不为空的字段
	 * @param obj
	 * @param updateEvenNull 即使为空也更新到数据库中的字段，如果为null，
	 * 			则根据domain字段中的UpdateEvenNull注解进行更新，
	 * 			所以想跳过UpdateEvenNull注解只更新不为空的字段可以传一个空的list
	 * @return
	 */
	public static boolean updateNotNullField(BaseRelationalDatabaseDomain obj,List<String> updateEvenNull){
		return updateNotNullField(obj, updateEvenNull, false);
	}
	/**
	 * update实体类中不为空的字段
	 * @param obj
	 * @param updateEvenNull 即使为空也更新到数据库中的字段，如果为null，
	 * 			则根据domain字段中的UpdateEvenNull注解进行更新，
	 * 			所以想跳过UpdateEvenNull注解只更新不为空的字段可以传一个空的list
	 * @param strictlyMode 严格模式，如果为true则 字段==null才算空，
	 * 	否则调用BaseUtil.isObjEmpty判断字段是否为空
	 * @see BaseUtil#isObjEmpty
	 * @see DomainUtil#fillDomain
	 * @return
	 */
	public static boolean updateNotNullField(BaseRelationalDatabaseDomain obj,List<String> updateEvenNull,boolean strictlyMode){
		return update(DomainUtil.fillDomain(smartGet(obj), obj,updateEvenNull,strictlyMode));
	}
	
	public static boolean update(BaseRelationalDatabaseDomain obj){
		try {
			Session session = getSession();
			beginTransaction();
			session.update(obj);
			return managTransaction(true);
		} catch (Exception e) {
			e.printStackTrace();
			return managTransaction(false);
		}finally{
			closeSession();
		}
	}
	
	/**
	 * 不要求list里面所有对象一起update成功或失败,update单位是单个对象,
	 * 你可能需要另外一个方法updateList(List list)
	 * @see #updateList
	 * @param list
	 * @return update失败的对象数
	 */
	public static int updateListOneByOne(List<? extends BaseRelationalDatabaseDomain> list){
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
	 * @param map 查询数据
	 * @return 拼好的hql
	 */
	public static <T> StringBuffer getUpdateHql(Class<T> t,
			Map<String, Object> queryCondition,Map<String, Object> updated) {
		String fullClassName = t.getName();
		StringBuffer hql = new StringBuffer();
		String domainSimpleName = getDomainSimpleName(fullClassName);
		
		hql.append(" update ")
			.append(fullClassName)
			.append(" ")
			.append(domainSimpleName);
		hql.append(" set ");
		appendHqlUpdateSet(hql, domainSimpleName, updated);
		
		hql.append(" where 1=1 ");
		
		appendHqlWhere(domainSimpleName, hql, queryCondition);
		
		steed.util.logging.LoggerFactory.getLogger().debug("hql------>"+hql.toString());
		return hql;
	}
	/**
	 * 获取删除用的hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param map 查询数据
	 * @return 拼好的hql
	 */
	public static StringBuffer getDeleteHql(Class<? extends BaseRelationalDatabaseDomain> t,Map<String, Object> map) {
		return getHql(t, map, null, null,"delete");
	}
	
	/**
	 * 获取查询用的hql
	 * 除了t其它均可为null
	 * @param t 实体类
	 * @param map 查询数据
	 * @param desc 降序排列字段
	 * @param asc 升序排列字段
	 * @return 拼好的hql
	 */
	public static <T> StringBuffer getSelectHql(Class<T> t,Map<String, Object> map,List<String> desc,List<String> asc,String... selectedFields) {
		return getHql(t, map, desc, asc,"select",selectedFields);
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
	public static <T> StringBuffer getHql(Class<T> t, Map<String, Object> queryMap,List<String> desc,
			List<String> asc,String prefix,String... selectedFields) {
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
				for(String temp:selectedFields){
					Matcher matcher = RegUtil.getPattern(".+\\((.+)\\)").matcher(temp);
					if (matcher.find()) {
						//sum(weight),count(id)之类的
						//temp = temp.replace(matcher.group(1), domainSimpleName+"."+matcher.group(1));
						hql.append(temp).append(" as ").append(matcher.group(1).replace(".", "__").replace("\r", "")
								.replace("*", "_")).append(",");
					}else {
						if (!temp.contains(".")) {
						}else {
							domainSelected.add(temp.substring(0, temp.indexOf(".")));
						}
						hql.append(domainSimpleName).append(".").
							append(temp).append(" as ").append(temp.replace(".", "__")).append(",");
					}
				}
				
				hql.deleteCharAt(hql.length()-1);
				if (selectedFields.length > 1) {
					hql.append(" )");
				}
				
			}
		}else {
			hql.append(" from ")
			.append(fullClassName)
			.append(" ")
			.append(domainSimpleName)
			.append(" where 1=1 ");
		}
		
		if (queryMap != null) {
			for(String temp:queryMap.keySet()){
				if (temp.contains(".")) {
					String key = temp.substring(0, temp.indexOf("."));
					innerJoin.add(key);
					domainSelected.remove(key);
				}
			}
		}
		
		hql.append(" from ")
		.append(fullClassName)
		.append(" ")
		.append(domainSimpleName);
		
		for(String temp:domainSelected){
			hql.append(" left join ").append(domainSimpleName).append(".")
				.append(temp).append(" ");
		}
		for(String temp:innerJoin){
			hql.append(" inner join ").append(domainSimpleName).append(".")
			.append(temp).append(" ");
		}
		
		hql.append(" where  1=1 ");
		
		
		appendHqlWhere(domainSimpleName, hql, queryMap);
		appendHqlOrder(hql, desc, asc, domainSimpleName);
		
		steed.util.logging.LoggerFactory.getLogger().debug("hql------>%s",hql.toString());
		steed.util.logging.LoggerFactory.getLogger().debug("参数------>%s",queryMap.toString());
		
		return hql;
	}
	/**
	 * 根据查询对象生成hql
	 * @param domain
	 * @param desc 需要降序排列的字段
	 * @param asc 需要升序排列的字段
	 * @return 生成的hql
	 */
	public static StringBuffer getSelectHql(BaseRelationalDatabaseDomain domain,List<String> desc,List<String> asc) {
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
	public static StringBuffer getCountHql(StringBuffer hql) {
		return changeSelectHql(hql, "count(*)");
	}
	/**
	 * 获取该条查询的记录总数
	 * @param hql
	 * @return
	 */
	public static StringBuffer changeSelectHql(StringBuffer hql,String selectedField) {
		//select people_steed_00 inner join people_steed_00.roleSet people_steed_00roleSet from steed.domain.people.People people_steed_00 where 1=1 
		//and people_steed_00roleSet in (:roleSet) and people_steed_00roleSet not in (:roleSet_not_in_1)
		StringBuffer countHql = new StringBuffer(hql);
		Pattern p = RegUtil.getPattern("select .+? from");
		Matcher m = p.matcher(countHql);
		if (m.find()) {
			countHql.replace(m.start(0), m.end(0), "select "+selectedField+" from");
		}else {
			countHql.insert(0, "select "+selectedField+" ");
		}
		steed.util.logging.LoggerFactory.getLogger().debug("countHql--->"+countHql);
		return countHql;
	}
	
	/**
	 * 
	 * 生成query并设置查询参数,如果udpate,delete等语句,确保之前调用了DaoUtil其它自动开启事务的方法否则请手动调用DaoUtil.beginTransaction()
	 * 开启事务或手动管理事务,不然系统无法提交事务,造成udpate或delete不生效bug
	 * @param map
	 * @param hql
	 * @see #beginTransaction
	 * @return
	 */
	public static Query createQuery(Map<String, Object> map,StringBuffer hql) {
		steed.util.logging.LoggerFactory.getLogger().debug("hql---->"+hql.toString());
		steed.util.logging.LoggerFactory.getLogger().debug("参数---->"+map);
		Query query = getSession().createQuery(hql.toString());
		setMapParam(map, query);
		return query;
	}
	/**
	 * 生成SQLquery并设置查询参数
	 * @param map
	 * @param sql
	 * @return
	 */
	public static Query createSQLQuery(Map<String, Object> map,StringBuffer sql) {
		steed.util.logging.LoggerFactory.getLogger().debug("sql---->"+sql.toString());
		steed.util.logging.LoggerFactory.getLogger().debug("参数---->"+map);
		Query query = getSession().createSQLQuery(sql.toString());
		setMapParam(map, query);
		return query;
	}
	
	public static Session getSession(){
		Session session = HibernateUtil.getSession();
		if (Config.autoBeginTransaction) {
			beginTransaction();
		}
		return session;
	}
	
	/**
	 * 组装hql的order by 部分
	 * @param hql
	 * @param desc
	 * @param asc
	 * @param domainSimpleName
	 * @return
	 */
	public static StringBuffer appendHqlOrder(StringBuffer hql,List<String> desc,List<String> asc,String domainSimpleName){
		boolean hasOrderByAppened = false;
		if (desc != null && !desc.isEmpty()) {
			for (String name:desc) {
				if (!hasOrderByAppened) {
					hql.append("order by ");
					hasOrderByAppened = true;
				}else {
					hql.append(", ");
				}
				hql.append(domainSimpleName);
				hql.append(".");
				hql.append(name);
				hql.append(" desc");
			}
		}
		if (asc != null && !asc.isEmpty()) {
			for (String name:asc) {
				if (!hasOrderByAppened) {
					hql.append("order by ");
					hasOrderByAppened = true;
				}else {
					hql.append(", ");
				}
				hql.append(domainSimpleName);
				hql.append(".");
				hql.append(name);
				hql.append(" asc");
			}
		}
		return hql;
	}
	
	/**
	 * 根据配置关闭session
	 */
	private static void closeSession(){
		managTransaction(true);
		if (HibernateUtil.getColseSession() && shouldCommitNow()) {
			managTransaction();
			HibernateUtil.closeSession();
		}
	}
	/**
	 * 无论是否配置了框架管理session,马上关闭session
	 */
	public static void closeSessionNow(){
		managTransaction();
		HibernateUtil.closeSession();
	}
	/***************************数据库工具结束********************************/
	
	
	
	/***************************内部方法开始********************************/
	
	/**
	 * 获取domain的简称用作查询时的别名
	 * @param fullClassName 全类名
	 * @return
	 */
	private static String getDomainSimpleName(String fullClassName) {
		String domainSimpleName = StringUtil.firstChar2LowerCase(StringUtil.getClassSimpleName(fullClassName)+"_steed_00");
		return domainSimpleName;
	}
	
	/*@SuppressWarnings("unused")
	private static StringBuffer appendHqlGroupBy(StringBuffer hql,List<String> groupBy,String domainSimpleName){
		boolean hasOrderByAppened = false;
		if (!CollectionsUtil.isCollectionsEmpty(groupBy)) {
			for (String temp:groupBy) {
				if (!hasOrderByAppened) {
					hql.append("group by ");
					hasOrderByAppened = true;
				}else {
					hql.append(", and ");
				}
				if (domainSimpleName != null) {
					hql.append(domainSimpleName)
						.append(".");
				}
				hql.append(temp)
					.append(" ");
			}
		}
		return hql;
	}*/
	/**
	 *  把obj中非空字段放到map
	 * @param obj
	 * @return map
	 */
	public static Map<String, Object> putField2Map(Object obj) {
		Map<String, Object> map = new HashMap<>();
		putField2Map(obj, map, "");
		return map;
	}
	/**
	 * 把obj中非空字段放到map
	 * @return
	 */
	public static void putField2Map(Object obj,Map<String, Object> map,String prefixName) {
		putField2Map(obj, map, prefixName, true);
	}
	
	/**
	 * DaoUtil#putField2Map(Object, Map, String, boolean)的拦截器,
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
		 * @param obj 要put到map的对象
		 * @param map obj字段容器,用来生成hql或sqlwhere部分的查询条件
		 * @param prefixName 前缀,当put user里面的school时,prefixName="user.",原理比较复杂,具体可以看源码
		 * @param getFieldByGetter 是否用Getter方法来获取字段值,若传false,则用field.get直接获取字段值
		 * @return 是否运行把该对象put到map
		 * 
		 * @see DaoUtil#putField2Map(Object, Map, String, boolean)
		 */
		public boolean beforePutField2Map(Map<String, Object> map,String prefixName,boolean getFieldByGetter);
		
	}
	
	/**
	 * 
	 * @param obj 要put到map的对象
	 * @param map obj字段容器,用来生成hql或sqlwhere部分的查询条件
	 * @param prefixName 前缀,当put user里面的school时,prefixName="user.",原理比较复杂,具体可以看源码
	 * @param getFieldByGetter 是否用Getter方法来获取字段值,若传false,则用field.get直接获取字段值
	 * 
	 * @see DaoUtil.PutField2MapIntercepter
	 */
	public static void putField2Map(Object obj,Map<String, Object> map,String prefixName,boolean getFieldByGetter) {
		if (obj == null) {
			return;
		}
		if (obj instanceof PutField2MapIntercepter) {
			if(!((PutField2MapIntercepter)obj).beforePutField2Map(map, prefixName, getFieldByGetter)){
				return;
			}
		}
		try {
			Class<? extends Object> objClass = obj.getClass();
			List<Field> Fields = ReflectUtil.getNotFinalFields(obj);
			for (Field f:Fields) {
				String fieldName = f.getName();
				//不是索引字段且标有Transient即跳过
				if (isSelectIndex(fieldName) == 0) {
					if (ReflectUtil.getAnnotation(Transient.class, objClass, f) != null) {
						continue;
					}else if (ReflectUtil.getDeclaredMethod(objClass, StringUtil.getFieldGetterName(fieldName)) == null 
							&& ReflectUtil.getDeclaredMethod(objClass, StringUtil.getFieldIsMethodName(fieldName)) == null) {
						continue;
					}
				}
				
				Object value = null;
				if (getFieldByGetter) {
					value = ReflectUtil.getFieldValueByGetter(obj, fieldName);
				}
				if (value == null) {
					f.setAccessible(true);
					value = f.get(obj);
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
		} catch (IllegalArgumentException | IllegalAccessException e) {
			steed.util.logging.LoggerFactory.getLogger().debug("putField2Map出错",e);
		}
	}
	
	/**
	 * 是否属于查找索引字段
	 * @param fieldName 字段名..
	 * @return 如果不是索引字段返回0,是则返回索引后缀长度...方便subString那到真实字段名
	 */
	public static int isSelectIndex(String fieldName){
		for (String suffix:indexSuffix) {
			if (fieldName.endsWith(suffix)) {
				return suffix.length();
			}
		}
		return 0;
	}
	
	/**
	 * 往map里面put("personalHqlGenerator",steed.util.dao.HqlGenerator);
	 * 即可跳过默认的HqlGenerator,用个性化HqlGenerator生成hql
	 * 
	 * 组装map参数到hql的where部分
	 * 
	 * @see #personalHqlGeneratorKey
	 * @param domainSimpleName
	 * @param hql
	 * @param map
	 */
	public static StringBuffer appendHqlWhere(String domainSimpleName, StringBuffer hql,
			Map<String, Object> map) {
		if (map == null) {
			return hql;
		}
		Object object = map.get(personalHqlGeneratorKey);
		if (object != null && object instanceof HqlGenerator) {
			map.remove(personalHqlGeneratorKey);
			StringBuffer appendHqlWhere = ((HqlGenerator)object).appendHqlWhere(domainSimpleName, hql, map);
			map.put(personalHqlGeneratorKey, object);
			return appendHqlWhere;
		}
		return Config.defaultHqlGenerator.appendHqlWhere(domainSimpleName, hql, map);
	}
	
	/**
	 * 组装update类型的hql中set部分
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
	 * 获取总记录
	 * @param map 可以为null
	 * @param domainName
	 * @param hql 普通查询hql,会自动转换成查询记录总数的hql
	 * @return
	 */
	private static Long getRecordCount(Map<String, Object> map,StringBuffer hql) {
		Query query = createQuery(map, getCountHql(hql));
		Long recordCount = (Long) query.uniqueResult();
		return recordCount;
	}

	/**
	 * 设置page的大小，当前页等
	 * @param currentPage
	 * @param recordCount
	 * @param pageSize
	 * @param list
	 * @return
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
	 * @param map
	 * @param query
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
	 * @param currentPage
	 * @param query
	 * @return
	 */
	public static void faging(int pageSize,int currentPage, Query query) {
		query.setFirstResult((currentPage-1)*pageSize);
		query.setMaxResults(pageSize);
	}
	
	public static class ImmediatelyTransactionData{
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
