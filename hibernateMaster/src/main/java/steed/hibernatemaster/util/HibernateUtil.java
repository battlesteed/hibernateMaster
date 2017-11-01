package steed.hibernatemaster.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import steed.hibernatemaster.Config;
/**
 * 获取线程安全的session<br>
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG 
   佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？
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
	
	
	static {
		try{
			buildFactory();
		}catch (Error e) {
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
	 * 需要手动开启和提交事务,这个事务并不受框架控制,若要框架控制事务,请用DaoUtil#getSession()
	 * 或调用该方法后,手动调用DaoUtil#beginTransaction()
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
	 * @param configFile
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
