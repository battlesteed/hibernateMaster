package steed.hibernatemaster.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import steed.ext.util.base.PathUtil;
import steed.ext.util.file.FileUtil;
import steed.hibernatemaster.Config;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.domain.DomainScanner;

/**
 * 单数据库实体类扫描器
 * @author 战马
 *
 */
public class SingleDomainScanner implements DomainScanner{
	
	@SuppressWarnings("unchecked")
	public List<Class<? extends BaseDatabaseDomain>> scan(String configFile){
		List<Class<? extends BaseDatabaseDomain>> list = new ArrayList<>();
//		if(!Config.isSignalDatabase){
//			return list;
//		}
		String classesPath = PathUtil.getClassesPath();
		
		List<File> allFile = new ArrayList<>();
		allFile.addAll(new FileUtil().getAllFile(classesPath,null));
		if (classesPath.contains("test-classes")) {
			allFile.addAll(new FileUtil().getAllFile(classesPath.replaceFirst("test-classes", "classes"),null));
		}else {
			allFile.addAll(new FileUtil().getAllFile(classesPath.replaceFirst("classes", "test-classes"),null));
		}
		
		for (File f:allFile) {
			String absolutePath = f.getAbsolutePath();
			if (!absolutePath.endsWith(".class") || (!absolutePath.contains("domain") && !absolutePath.contains("model"))) {
				continue;
			}
			String replaceAll = absolutePath.substring(absolutePath.indexOf("classes")+"classes.".length()).replace("\\", ".").replace("/", ".");
			try {
				String domainClassName = replaceAll.substring(0,replaceAll.length() - 6);
//				steed.ext.util.logging.LoggerFactory.getLogger().debug("扫描%s",domainClassName);
				Class<?> domainClass = Class.forName(domainClassName);
				if (BaseRelationalDatabaseDomain.class.isAssignableFrom(domainClass)) {
					if (domainClass.getAnnotation(Entity.class) != null) {
						list.add((Class<? extends BaseDatabaseDomain>) domainClass);
					}
				}
			} catch (Exception e) {
				
			}catch (Error e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
