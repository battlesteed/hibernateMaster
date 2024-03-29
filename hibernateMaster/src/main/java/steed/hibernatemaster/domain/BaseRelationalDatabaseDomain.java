package steed.hibernatemaster.domain;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;
import javax.validation.ConstraintViolation;

import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentSet;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.base.DomainUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.Config;
import steed.hibernatemaster.exception.ValidateException;
import steed.hibernatemaster.listener.CRUDListener;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.HqlGenerator;
/**
 * 关系型数据库基础实体类
 * @author 战马
 *
 */
public class BaseRelationalDatabaseDomain extends BaseDatabaseDomain{
	private static final Logger logger = LoggerFactory.getLogger(BaseRelationalDatabaseDomain.class);
	
	private static final long serialVersionUID = -3084039108845387366L;
	protected HqlGenerator personalHqlGenerator;
	private transient boolean trimEmptyDomain;
	
	@Transient
	public HqlGenerator getPersonalHqlGenerator() {
		return personalHqlGenerator;
	}
	
	/**
	 * 设置属于本实体类的个性化的hql生成器
	 * @param personalHqlGenerator 自定义的hql生成器
	 * @see steed.hibernatemaster.util.SimpleHqlGenerator
	 */
	public void setPersonalHqlGenerator(HqlGenerator personalHqlGenerator) {
		this.personalHqlGenerator = personalHqlGenerator;
	}
	public void initialize(){
		Hibernate.initialize(this);
	}
	/**
	 * 不为null就initialize,避免子类getXXX().initializeAll()时
	 * 先要判断getXXX()是否为null
	 * 
	 * @param domain 要initializeAll的实体类
	 */
	protected void domainInitializeAll(BaseRelationalDatabaseDomain domain){
		if (domain != null) {
			domain.initializeAll();
		}
	}
	/**
	 * initialize set中的domain
	 * 
	 * @param set 要initializeAll的set
	 */
	protected void domainInitializeSetAll(Collection<? extends BaseRelationalDatabaseDomain> set){
		if (set != null) {
			for(BaseRelationalDatabaseDomain temp:set){
				temp.initializeAll();
			}
		}
	}
	
	/*
	 * initializeAll的深度，如过为零，则只initialize本身。
	 * @param deep
	 *public void initializeAll(int deep){
		if (deep > 0) {
			
		}else if (deep == 0) {
			this.initialize();
		}
		
	}*/
	/**
	 * initialize所有关联的实体类,对hibernate代理对象做序列化之前 请调用该方法
	 * 
	 * @see DaoUtil#load(Class, Serializable)
	 */
	public void initializeAll(){
		initialize();
	}
	
	protected boolean validate(){
		if (Config.validator == null || !Config.enableHibernateValidate) {
			return true;
		}
		
		Set<ConstraintViolation<BaseRelationalDatabaseDomain>> validate = Config.validator.validate(this);
		
		if (Config.devMode) {
			for (ConstraintViolation<BaseRelationalDatabaseDomain> temp:validate) {
				logger.debug("校验失败:"+temp.getMessage());
			}
		}
		
		boolean empty = validate.isEmpty();
		if (!empty) {
			throw getException(validate.iterator().next().getMessage());
		}
		
		return empty;
	}
	
	protected RuntimeException getException(String errorMessage){
		try {
			//若在战马web框架运行,则抛
			Class<?> forName = Class.forName("steed.exception.runtime.MessageRuntimeException");
			return (RuntimeException) forName.getConstructor(String.class).newInstance(errorMessage);
		} catch (Exception e) {
		} 
		return new ValidateException(errorMessage);
	}
	
	
	/**
	 * 用当前实例的id查询数据库记录
	 * @return 查询到的记录
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseDatabaseDomain> T smartGet(){
		return (T) DaoUtil.smartGet(this);
	}
	
	/**
	 * 用当前实例的id查询数据库记录
	 * @return 查询到的记录
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BaseDatabaseDomain> T smartLoad(){
		return (T) DaoUtil.smartLoad(this);
	}
	
	/**
	 * 把实体类全部字段更新到数据库.
	 * @see #updateNotNullField(List, boolean)
	 * @see #updateNotNullFieldByHql()
	 */
	@Override
	public boolean update(){
		validate();
		trimEmptyDomain();
		return DaoUtil.update(this);
	}
	
	/**
	 * 删除当前实例的id对应的数据库记录
	 * 可以重写该方法做级联删除,也可以直接设置数据对应外键的删除策略
	 */
	@Override
	public boolean delete(){
		CRUDListener[] listeners = Config.CRUDListenerManager.getListeners(getClass());
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].beforeDelete(this);
			}
		}
		boolean delete = DaoUtil.delete(this);
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].afterDelete(this);
			}
		}
		return delete;
	}
	
	/**
	 * 
	 * @return 是否需要框架帮忙把id为空字符串的实体类裁剪,防止找不到空字符串的外键,导致保存或更新失败
	 * 
	 * @see #trimEmptyDomain()
	 * @see #setTrimEmptyDomain(boolean)
	 */
	@Transient
	public boolean isTrimEmptyDomain() {
		return trimEmptyDomain;
	}

	/**
	 * 设置是否需要框架帮忙把id为空字符串的实体类裁剪,防止找不到空字符串的外键,导致保存或更新失败
	 * @param trimEmptyDomain 是否把id为空字符串的实体类裁剪,防止找不到空字符串的外键,导致保存或更新失败
	 * @see #trimEmptyDomain()
	 * @see #isTrimEmptyDomain()
	 */
	public void setTrimEmptyDomain(boolean trimEmptyDomain) {
		this.trimEmptyDomain = trimEmptyDomain;
	}

	/**
	 * 把id为空字符串的实体类裁剪,防止找不到空字符串的外键,导致保存或更新失败
	 * @see #trimEmptyDomain()
	 * @see #setTrimEmptyDomain(boolean)
	 */
	protected void trimEmptyDomain(){
		if (!isTrimEmptyDomain()) {
			return;
		}
		List<Field> notFinalFields = ReflectUtil.getNotFinalFields(this);
		for (Field temp:notFinalFields) {
			if (BaseRelationalDatabaseDomain.class.isAssignableFrom(temp.getType())) {
				try {
					temp.setAccessible(true);
					Object obj = temp.get(this);
					if (obj != null && BaseUtil.isObjEmpty(obj)) {
						temp.set(this, null);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public boolean save(){
		trimEmptyDomain();
		validate();
		CRUDListener[] listeners = Config.CRUDListenerManager.getListeners(getClass());
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].beforSave(this);
			}
		}
		boolean save = DaoUtil.save(this);
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].afterSave(this);
			}
		}
		return save;
	}
	
	@Override
	public boolean saveOrUpdate(){
		//TODO 移除id是否为空判断,若为null,肯定不会调saveOrUpdate,无法update,只能save
		Serializable domainId = DomainUtil.getDomainId(this);
		if (BaseUtil.isObjEmpty(domainId)) {
			return save();
		}else {
			Map<String, Object> map = new HashMap<>();
			Class<? extends BaseRelationalDatabaseDomain> clazz = getClass();
			String domainIDName = DomainUtil.getDomainIDName(clazz);
			//TODO 处理联合主键情况
			map.put(domainIDName, domainId);
			if (!DaoUtil.isResultNull(clazz, map)) {
				return update();
			}else {
				return save();
			}
		}
	}
	

	
	/**
	 * update不为空的字段,直接用hql update,只update需要update的字段,比{@link #updateNotNullField(List, boolean) }性能好,若对象刚从数据库查询出来,请直接{@link #update}
	 * 
	 */
	public boolean updateNotNullFieldByHql(){
		return updateNotNullFieldByHql(null, true);
	}
	
	/**
	 * update不为空的字段,直接用hql update,只update需要update的字段,比{@link #updateNotNullField(List, boolean) }性能好,若对象刚从数据库查询出来,请直接{@link #update}
	 * 若要update clazz.id 为1 的student 实体类的 inDate为 当前时间,type为8,则可以这样写:
	 * <pre>{@code
	 * 	Student student = new Student();
		Clazz clazz = new Clazz();
		clazz.setId("5");
		student.setClazz(clazz);
		student.setInDate(new Date());
		student.setType(8);
		student.updateNotNullFieldByHql("clazz");//也可以student.updateNotNullFieldByHql("clazz.id");
	 * }</pre>
	 * 
	 * @param whereField 要作为where条件的字段
	 */
	public boolean updateNotNullFieldByHql(String... whereField){
		return updateNotNullFieldByHql(null, true, whereField);
	}
	
	/**
	 * 用id做where条件(调用该方法之前一定要设置实例id),update其它不为空的字段,直接用hql update,只update需要update的字段,比{@link #updateNotNullField(List, boolean) }性能好,若对象刚从数据库查询出来,请直接{@link #update}
	 * 
	 * @param updateEvenNull 即使为null也update的字段,如果没有可以传null
	 * @param strictlyMode 严格模式，如果为true则 字段==null才算空， 否则调用{@link BaseUtil#isObjEmpty(Object)} 判断字段是否为空
	 */
	public boolean updateNotNullFieldByHql(List<String> updateEvenNull,boolean strictlyMode){
		String domainIDName = DomainUtil.getDomainIDName(getClass());
		//TODO 校验是否设置了id
		return updateNotNullFieldByHql(updateEvenNull, strictlyMode, domainIDName);
	}
	/**
	 * update不为空的字段,直接用hql update,只update需要update的字段,比{@link #updateNotNullField(List, boolean) }性能好.
	 * 若要update clazz.id 为1 的student 实体类的 inDate为 当前时间,type为8,则可以这样写:
	 * <pre>{@code
	 * 	Student student = new Student();
		Clazz clazz = new Clazz();
		clazz.setId("5");
		student.setClazz(clazz);
		student.setInDate(new Date());
		student.setType(8);
		student.updateNotNullFieldByHql(null, true, "clazz");//也可以student.updateNotNullFieldByHql(null, true, "clazz.id");
	 * }</pre>
	 * 
	 * @param updateEvenNull 即使为null也update的字段,如果没有可以传null
	 * @param strictlyMode 严格模式，如果为true则 字段==null才算空， 否则调用{@link BaseUtil#isObjEmpty(Object)} 判断字段是否为空
	 * @param whereField 要作为where条件的字段
	 */
	public boolean updateNotNullFieldByHql(List<String> updateEvenNull,boolean strictlyMode,String... whereField){
		/*
		 * Map<String, Object> field2Map = DaoUtil.putField2Map(this); List<String>
		 * keys4Remove = new ArrayList<String>(); field2Map.entrySet().forEach((e)->{ if
		 * (e.getKey().contains(".")) { keys4Remove.add(e.getKey()); return; } if
		 * (strictlyMode) {
		 * 
		 * } });
		 */
		Map<String, Object> field2Map = ReflectUtil.field2Map(this, strictlyMode, true, true);
		if (updateEvenNull != null) {
			updateEvenNull.forEach((temp)->{
				if (!field2Map.containsKey(temp)) {
					field2Map.put(temp, null);
				}
			});
		}
		HashMap<String, Object> where = new HashMap<String, Object>();
		for (String temp:whereField) {
			if (field2Map.containsKey(temp)) {
				where.put(temp, field2Map.get(temp));
				field2Map.remove(temp);
			}else {
				where.put(temp, ReflectUtil.getChainValue(temp, this));
				field2Map.remove(temp.substring(0, temp.lastIndexOf(".")));
			}
		}
		return DaoUtil.updateByQuery(getClass(), where, field2Map) > 0;
	}
	
	/**
	 * update不为空的字段(把本实例为空的字段从数据库查出来填充进去),然后调用{@link update}方法,update整个实例
	 * 
	 *    若想做aop,可以直接重写update方法即可,不用重写该方法,推荐用{@link #updateNotNullFieldByHql(List, boolean)}
	 * 
	 * @param updateEvenNull 即使为null也update的字段,如果没有可以传null
	 * 
	 * @see #updateNotNullFieldByHql(List, boolean)
	 */
	@Deprecated
	@Override
	public boolean updateNotNullField(List<String> updateEvenNull){
		return updateNotNullField(updateEvenNull, true);
	}
	
	/**
	 * update不为空的字段(把本实例为空的字段从数据库查出来填充进去),然后调用{@link update}方法,update整个实例
	 * 
	 *    若想做aop,可以直接重写update方法即可,不用重写该方法,推荐用{@link #updateNotNullFieldByHql(List, boolean)}
	 * 
	 * @param updateEvenNull 即使为null也update的字段,如果没有可以传null
	 * @param strictlyMode 严格模式，如果为true则 字段==null才算空， 否则调用{@link BaseUtil#isObjEmpty(Object)} 判断字段是否为空
	 * 
	 * @see #updateNotNullFieldByHql(List, boolean)
	 */
	@Deprecated
	@Override
	public boolean updateNotNullField(List<String> updateEvenNull,boolean strictlyMode){
		BaseRelationalDatabaseDomain smartGet = this.smartGet();
		DomainUtil.fillDomain(smartGet, this,updateEvenNull,strictlyMode);
		return smartGet.update();
	}
	/*public void initializeAll(){
		this.initialize();
		Field[] fields = this.getClass().getDeclaredFields();
		for(Field f:fields){
			try {
				f.setAccessible(true);
				Object obj = f.get(this);
				if (BaseUtil.isObjEmpty(obj)) {
					continue;
				}
				if (obj instanceof BaseDatabaseDomain) {
					((BaseDatabaseDomain) obj).initializeAll();
				}else if (CollectionsUtil.isObjCollections(obj)) {
					Object[] objects = CollectionsUtil.collections2Array(obj);
					if (objects == null || objects.length < 1) {
						continue;
					}
					if (!(objects[0] instanceof BaseDatabaseDomain)) {
						continue;
					}
					for (BaseDatabaseDomain o:(BaseDatabaseDomain[])objects) {
						if (BaseUtil.isObjEmpty(o)) {
							continue;
						}
						o.initializeAll();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}*/
	
	/*	public boolean validate(){
		Class<? extends BaseDatabaseDomain> clazz = this.getClass();
		Properties IDReg = PropertyUtil.getProperties("IDReg.properties");
		String reg = IDReg.getProperty(clazz.getName());
		if (BaseUtil.isStringEmpty(reg)) {
			return true;
		}
		Serializable domainId = DomainUtil.getDomainId(this);
		if (BaseUtil.isObjEmpty(domainId)) {
			return true;
		}
		Pattern p = Pattern.compile(reg);
		return p.matcher(domainId.toString()).find();
	}*/
	@SuppressWarnings("unchecked")
	public <T> List<T> listAll(){
		return (List<T>) DaoUtil.listAllObj(this);
	}
	
	/**
	 * hibernate代理Collection转java内置Collection
	 * 一般用在序列化之前,防止反序列化时没有session导致抛异常
	 * 
	 * @return java内置Collection
	 */
	@SuppressWarnings("unchecked")
	public BaseRelationalDatabaseDomain hibernateProxCollection2javaCollection(){
		initializeAll();
		try {
			for (Field f:ReflectUtil.getNotFinalFields(this)) {
				f.setAccessible(true);
				try {
					Object value = f.get(this);
					if (value instanceof Collection<?>) {
						Collection<?> temp = (Collection<?>) value;
						value = getCollection(f.getType(), (Collection<?>) value);
						for (Object o:temp) {
							if (o instanceof BaseRelationalDatabaseDomain) {
								o = ((BaseRelationalDatabaseDomain) o).hibernateProxCollection2javaCollection();
							}
							((Collection<Object>)value).add(o);
						}
					}else if (value instanceof BaseRelationalDatabaseDomain) {
						value = ((BaseRelationalDatabaseDomain)value).hibernateProxCollection2javaCollection();
					}else{
						continue;
					}
					f.set(this, value);
				} catch (IllegalAccessException e) {
					logger.debug("copyObj", e);
				} 
			}
		} catch (Exception e) {
			logger.error("hibernate代理Collection转java内置Collection失败!!",e);
//			throw new RuntimeException(e);
		}
		return this;
	}
	
	protected final Collection<?> getCollection(Class<?> type,Collection<?> value) {
		if (value instanceof PersistentSet && type.isAssignableFrom(HashSet.class)) {
			return new HashSet<>();
		}else if(value instanceof PersistentList && type.isAssignableFrom(ArrayList.class)){
			return new ArrayList<>();
		}
		return value;
	}
	
}

