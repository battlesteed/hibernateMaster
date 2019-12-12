package steed.hibernatemaster.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import steed.ext.util.logging.LoggerFactory;
import steed.hibernatemaster.Config;
/**
 * 获取线程安全的session
 * @author 战马
 */
public class HibernateUtil{
	private static ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
	private static SessionFactory factory;
	private static ThreadLocal<Boolean> closeSession = new ThreadLocal<Boolean>();
	private static ThreadLocal<String> currentDatabase = new ThreadLocal<String>();
	private static ThreadLocal<Map<String, Session>> sessionStory = new ThreadLocal<Map<String, Session>>();
//	private static boolean whole_closeSession = false;
	private static Map<String, SessionFactory> factoryMap = new HashMap<String, SessionFactory>();
	public static final String mainFactory = "hibernate.cfg.xml";
	
//	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
	
	static {
		try{
			buildFactory();
		}catch (Throwable e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static boolean getColseSession(){
		Boolean closeSession = HibernateUtil.closeSession.get();
		if (closeSession == null) {
			return true;
		}
		return closeSession;
	}
	
	public static void setCloseSession(boolean closeSession){
		HibernateUtil.closeSession.set(closeSession);
	}
	public static void setSession(Session session){
		threadLocal.set(session);
	}
	private static void buildFactory() {
		factory = buildFactory(mainFactory);
	}

	private static SessionFactory buildFactory(String configFile) {
		SessionFactory buildFactory = Config.factoryEngine.buildFactory(configFile);
		factoryMap.put(configFile, buildFactory);
		return buildFactory;
	}
	
	/**
	 * 注意,通过该方法获取的session,若之前未调用daoutil的开启事务方法,则返回的session是未开启事务的
	 * 需要手动开启和提交事务,这个事务并不受框架控制,若要框架控制事务,请用<code> {@link DaoUtil#getSession() } </code>
	 * 或调用该方法后,手动调用<code> {@link DaoUtil#beginTransaction() } </code>
	 * 
	 * 
	 * @see DaoUtil#getSession()
	 * @see DaoUtil#beginTransaction()
	 * 
	 * @return session
	 */
	public static Session getSession(){
		Session session = null;
		if (Config.isSignalDatabase) {
			session = threadLocal.get();
		}else {
			session = getSessionMap().get(getCurrentDatabase());
		}
		if(session == null){
			session = getFactory().openSession();
			if (Config.isSignalDatabase) {
				threadLocal.set(session);
			}else {
				getSessionMap().put(getCurrentDatabase(), session);
			}
		}
		return session;
	}
	
	private static Map<String, Session> getSessionMap(){
		if (sessionStory.get() == null) {
			sessionStory.set(new HashMap<String, Session>());
		}
		return sessionStory.get();
	}
	private static String getCurrentDatabase(){
		return currentDatabase.get()==null?mainFactory:currentDatabase.get();
	}
	
	public static SessionFactory getFactory(){
		if (Config.isSignalDatabase) {
			return factory;
		}else {
			SessionFactory sessionFactory = factoryMap.get(getCurrentDatabase());
			if (sessionFactory == null) {
				return buildFactory(currentDatabase.get());
			}
			return sessionFactory;
		}
	}
	
	/**
	 * 切换数据库
	 * @param configFile hibernate配置文件名
	 */
	public static void switchDatabase(String configFile){
		currentDatabase.set(configFile);
	} 
	
	public static void closeSession() {
		Session session = null;
		if (Config.isSignalDatabase) {
			session = threadLocal.get();
		}else {
			session = getSessionMap().get(getCurrentDatabase());
		}
		if(session != null && session.isOpen()){
			session.close();
		}
		threadLocal.remove();
		closeSession.remove();
	}
	
	public static void release(){
		closeSession();
		for (Entry<String, Session> e:getSessionMap().entrySet()) {
			Session value = e.getValue();
			if (value!= null&&value.isOpen()) {
				value.close();
			}
		}
		currentDatabase.remove();
		sessionStory.remove();
	}
}
