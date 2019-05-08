package steed.ext.util.base;

public class PathUtil {

	/**
	 * 获取classes目录在操作系统中的绝对路径 形如D:/workspaces/SMS/WebRoot/WEB-INF/classes/
	 * @return
	 */
	public static String getClassesPath(){
		return PathUtil.class.getResource("/").getPath();
	}
	
	/**
	 * 合并路径,防止出现双斜杠或者没有斜杠
	 * @return
	 */
	public static String mergePath(String path1,String path2){
		return mergePath(path1, path2, "/");
	}
	
	public static String mergePath(String path1,String path2,String separator){
		if (path2.startsWith(separator)&&path1.endsWith(separator)) {
			return path1 + path2.substring(1);
		}else if(!path2.startsWith(separator)&&!path1.endsWith(separator)){
			return path1 + separator + path2;
		}else if(path2.startsWith(separator)&&path1.endsWith(separator)){
			return path1.substring(0, path1.length()-1) + path2;
		}else {
			return path1 + path2;
		}
	}
}
