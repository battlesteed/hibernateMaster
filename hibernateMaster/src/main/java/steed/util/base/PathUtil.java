package steed.util.base;

public class PathUtil {

	/**
	 * 获取classes目录在操作系统中的绝对路径 形如D:/workspaces/SMS/WebRoot/WEB-INF/classes/
	 * (junit环境下运行会返回test-class)
	 * @see #getFormalClassesPath
	 * @return
	 */
	public static String getClassesPath(){
		return PathUtil.class.getResource("/").getPath();
	}
}
