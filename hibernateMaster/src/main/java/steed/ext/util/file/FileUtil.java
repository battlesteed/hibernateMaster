package steed.ext.util.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	
	/**
	 * 扫描该目录下所有文件，并放在list中（只放文件，目录不放）
	 * @param root 要扫描的根目录
	 * @param filter 可以为null
	 * @param list 扫描到的文件（不包括目录）
	 */
	public void scanAllFile(File root,List<File> list,FilenameFilter filter){
		File[] listFiles;
		if (filter == null) {
			listFiles = root.listFiles();
		}else {
			listFiles = root.listFiles(filter);
		}
		if (listFiles != null) {
			for(File f:listFiles){
				if (f.isFile()) {
					list.add(f);
				}else {
					scanAllFile(f, list,filter);
				}
			}
		}
	}
	
	
	/**
	 * 替换文件后缀
	 * @param fileName
	 * @param suffix 新后缀
	 * @return
	 */
	public static String replaceFileSuffix(String fileName,String suffix){
		return fileName.substring(0,fileName.lastIndexOf('.'))+suffix;
	}
	
	/**
	 * 获取文件后缀,不带"."
	 * @param fileName
	 * @return
	 */
	public static String getFileSuffix(String fileName){
		return fileName.substring(fileName.lastIndexOf('.'));
	}
	
	/**
	 * 获取文件后缀
	 * @param file
	 * @return
	 */
	public static String getFileSuffix(File file){
		return getFileSuffix(file.getName());
	}
	
	/**
	 * 获取rootPath下所有文件，不包括目录
	 * @param rootPath
	 * @param filter 可以为null
	 * @return rootPath下所有文件，不包括目录
	 */
	public List<File> getAllFile(String rootPath,FilenameFilter filter){
		List<File> list = new ArrayList<File>();
		scanAllFile(new File(rootPath), list,filter);
		return list;
	}
	
}
