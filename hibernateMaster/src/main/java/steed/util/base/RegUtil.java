package steed.util.base;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 * @author 战马
 *
 */
public  class RegUtil {
	/**
	 * 中文和字母数字
	 */
	public final static String regChineseAndChar = "([\\u4E00-\\u9FA5]*\\w*_*)*";
	/**
	 * 中文
	 */
	public final static String regChinese = "[\\u4E00-\\u9FFF]+";
	/**
	 * ip地址
	 */
	public final static String regIpAddress = "\\d+\\.\\d+\\.\\d+\\.\\d+";
	/**
	 * 邮箱地址
	 */
	public final static String regEmail = "\\S+\\@\\S+\\.\\S+";
	/**
	 * 不严格的邮箱地址
	 */
	public final static String regNotStrictEmail = "\\S+\\@\\S+";
	/**
	 * 手机号码(不包括电话号码)
	 */
	public final static String mobilePhoneNumber = "1\\d{10}";
	
	/**
	 * 校验字符串,reg不需要^开头和$结尾,validate会自动加上
	 * @param reg
	 * @param str
	 * @return
	 */
	public static boolean validate(String reg,String str){
		if (!reg.startsWith("^")) {
			reg = "^"+reg;
		}
		if (!reg.endsWith("$")) {
			reg = reg+"$";
		}
		Pattern p = Pattern.compile(reg);  
        Matcher m = p.matcher(StringUtil.getNotNullString(str));  
        return m.find();
	}
	public static final Map<String, Pattern> patternCache = new HashMap<>();
	
	public static Pattern getPattern(String regex) {
		Pattern pattern = patternCache.get(regex);
		if (pattern != null) {
			return pattern;
		}
		pattern = Pattern.compile(regex);
		patternCache.put(regex, pattern);
		return pattern;
	}
	
	/*public static boolean find(String reg,String str){
		Pattern p = Pattern.compile(reg);  
		Matcher m = p.matcher(StringUtil.getNotNullString(str));  
		return m.find();
	}*/
	/*
	public boolean test(String reg,String tested){
        Pattern p = Pattern.compile(reg);  
        Matcher m = p.matcher(tested);  
        return m.find();
	}*/
}
