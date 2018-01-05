package steed.util.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;


public class StringUtil {
	public static boolean isStringEmpty(String str){
		return null == str || "".equals(str);
	}
	/**
	 * 格式化Double
	 * @param d
	 * @param precision 精度
	 * @return
	 */
	public static String formatNumber(double d,int precision){
		return String.format("%."+precision+"f", d);
	}
	/**
	 * 格式化float
	 * @param f
	 * @param precision 精度
	 * @return
	 */
	public static String formatNumber(float f,int precision){
		return String.format("%."+precision+"f", f);
	}
	/**
	 * 安全地截取string,不用担心越界
	 * @param str
	 * @param begin
	 * @param end
	 * @return
	 */
	public static String safeSubString(String str,int begin,int end){
		if (str == null) {
			return null;
		}
		if (end > str.length()) {
			end = str.length();
		}
		if (begin < 0) {
			begin = 0;
		}
		return str.substring(begin, end);
	}
	/**
	 * 根据全路径获取文件名
	 * @param path 路径
	 * @return
	 */
	public static String getFileName(String path){
		path = path.substring(path.indexOf("/")+1, path.length());
		return path.substring(path.indexOf("\\")+1,path.length());
	}
	
	public static String getString(InputStream in){
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in,StringUtil.getCharacterSet()));
			StringBuffer sb = new StringBuffer();
			String readLine = null;
			while ((readLine = bufferedReader.readLine()) != null) {
				sb.append(readLine);
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 若str为null则返回""否则原样返回
	 * @param str
	 * @return
	 */
	public static String getNotNullString(String str){
		return str == null ? "":str;
	}
	/**
	 * 获取类名简写
	 * @return
	 */
	public static String getClassSimpleName(String classFullName){
		return classFullName.substring(classFullName.lastIndexOf(".")+1, classFullName.length());
	}
	
	/**
	 * 根据字段名获取该字段的set方法名
	 * @param fieldName
	 * @return
	 */
	public static String getFieldSetterName(String fieldName){
		StringBuffer sb = new StringBuffer("set");
		sb.append(firstChar2UpperCase(fieldName));
		return sb.toString();
	}
	/**
	 * 根据字段名获取该字段的get方法名,不支持boolean类型，
	 * 如字段为Boolean类型请使用getFieldISMethodName
	 * @param fieldName
	 * @return
	 */
	public static String getFieldGetterName(String fieldName){
		StringBuffer sb = new StringBuffer("get");
		sb.append(firstChar2UpperCase(fieldName));
		return sb.toString();
	}
	/**
	 * 根据字段名获取该字段的is方法名,
	 * 如字段不是Boolean类型请使用getFieldGetMethodName
	 * @param fieldName
	 * @return
	 */
	public static String getFieldIsMethodName(String fieldName){
		StringBuffer sb = new StringBuffer("is");
		sb.append(firstChar2UpperCase(fieldName));
		return sb.toString();
	}
	/**
	 * 将str的第一个字母改为大写
	 * @param str
	 * @return
	 */
	public static String firstChar2UpperCase(String str){
		StringBuffer sb = new StringBuffer(String.valueOf(str.charAt(0)).toUpperCase());
		sb.append(str.substring(1, str.length()));
		return sb.toString();
	}
	/**
	 * 将str的第一个字母改为小写
	 * @param str
	 * @return
	 */
	public static String firstChar2LowerCase(String str){
		StringBuffer sb = new StringBuffer(String.valueOf(str.charAt(0)).toLowerCase());
		sb.append(str.substring(1, str.length()));
		return sb.toString();
	}
	
	/**
	 * 转义str中的&lt;&gt;&amp;"'\,防止跨站脚本攻击
	 * @return 转义后的str
	 */
	public static String transferrCharacter(String str){
		char[] content = str.toCharArray();
		StringBuffer sb = new StringBuffer(content.length + 30);
		for (char temp:content) {
			switch (temp) {
			case '<':
				sb.append("&lt;");
                break;
            case '>':
            	sb.append("&gt;");
                break;
            case '&':
            	sb.append("&amp;");
                break;
            case '"':
            	sb.append("&quot;");
                break;
            case '\'':
            	sb.append("&apos;");
            	break;
            default:
            	sb.append(temp);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 按系统编码获取str的字节数组
	 * @param str
	 * @return
	 */
	public static byte[] getSystemCharacterSetBytes(String str){
		try {
			return str.getBytes(getCharacterSet());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("不支持%s编码!!", getCharacterSet()));
		}
	}
	/**
	 * 获取系统编码
	 * @return
	 */
	public static String getCharacterSet() {
		return "UTF-8";
	}
	/**
	 * 判断与str相等的string是否在数组里
	 * @param str 不能为null
	 * @param strs 为null时返回false
	 * @return
	 */
	public static boolean isStringInArray(String str,String[] strs){
		if (strs == null) {
			return false;
		}
		for (String temp:strs) {
			if (str.equals(temp)) {
				return true;
			}
		}
		return false;
	}
	
	public static String encodeUrl(String url){
		try {
			return URLEncoder.encode(url, getCharacterSet());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("config.properties中的项目编码配置不正确！！！", e);
		}
	}
	public static String decodeUrl(String url){
		try {
			return URLDecoder.decode(url, getCharacterSet());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("config.properties中的项目编码配置不正确！！！", e);
		}
	}
	
	/**
	 * 获取uuid(不含'-')
	 * @return
	 */
	public static String getUUID(){
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	
	/**
	 * 按系统编码获取string
	 * @param bytes 
	 * @return
	 */
	public static String getSystemCharacterSetString(byte[] bytes){
		try {
			return new String(bytes,getCharacterSet());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("不支持%s编码!!", getCharacterSet()));
		}
	}
}
