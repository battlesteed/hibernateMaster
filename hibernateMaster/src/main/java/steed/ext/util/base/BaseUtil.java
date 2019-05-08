package steed.ext.util.base;

import java.net.MalformedURLException;
import java.net.URL;

import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;
/**
 * 基础工具类，拥有许多基础方法
 * @author 战马
 *
 */
public class BaseUtil {
	private final static Logger logger = LoggerFactory.getLogger();
	
	public static Logger getLogger(){
		return logger;
	}
	
	public static URL getResourceURL(String path) {
		URL url;

	      url = Thread.currentThread().getContextClassLoader().getResource(path);

	      if (url == null) {
	         // trying a different classloader now
	         url = new BaseUtil().getClass().getClassLoader().getResource(path);
	      }

	      if (url == null) {
	         try {
				url = new URL(path);
			} catch (MalformedURLException e) {
//				logger.info("获取资源路径失败!");
			}
	      }
	     return url;
	}
	
	/**
	 * 判断对象是否为空，集合为空时也会返回true
	 * @param obj
	 * @return 是否为空
	 */
	public static boolean isObjEmpty(Object obj){
		if (obj == null) {
			return true;
		}
		if (CollectionsUtil.isObjCollections(obj)) {
			return CollectionsUtil.isCollectionsEmpty(obj);
		}
		if (obj instanceof String) {
			return StringUtil.isStringEmpty((String)obj);
		}
		if (obj instanceof BaseDatabaseDomain) {
			return isObjEmpty(DomainUtil.getDomainId((BaseDomain) obj));
		}
		return null == obj;
	}
	/**
	 * 打印 prefix + "------&gt;" + String.valueOf(o)
	 * @param o
	 * @param prefix 前缀
	 * @return prefix + "------&gt;" + String.valueOf(o)
	 */
	public static String out(String prefix,Object o){
		String x = prefix + "------>" + o;
		System.out.println(x);
		return x;
	}
	
	
	/**
	 * 打印String.valueOf(o)
	 * @param o
	 */
	public static String out(Object o){
		return out("",o);
	}
	/**
	 * 打印 "------------------------"+o+"------------------------"
	 * @param o
	 */
	public static void outLine(Object o){
		String line = "------------------------"+o+"------------------------";
		out(line);
	}
	
	/**
	 * 比较相同实体类对象字段是否相等，必须是相同的实体类
	 * @param obj
	 * @param obj2
	 * @return
	 */
	public static boolean objectEquals(Object obj,Object obj2){
		if (obj == null) {
			return obj2 == null;
		}
		if (obj2 == null) {
			return false;
		}
		return obj.equals(obj2);
	}
	
	
}
