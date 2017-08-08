package com.gshopper.util;

/**
 * 不变常量
 * @author chenguyan
 * @date 2017年5月3日
 */
public class Constant {
	
	public final static String CONTENT_TYPE = "text/html;charset=UTF-8"; // 文本类型
	public final static String REG_EX_MAIL = "(<[\\s\\S]*?@gshopper.com>)"; // 正则邮箱
	public final static String START_DATE_SUFFIX = " 00:00:00";
	public final static String END_DATE_SUFFIX = " 23:59:59";
	public final static String START_TIME_SUFFIX_FOR_FIRST = " 19:00:00"; 
	public final static String END_TIME_SUFFIX_FOR_FIRST = " 23:59:59";
	public final static String START_TIME_SUFFIX_FOR_SECOND = " 00:00:00"; 
	public final static String END_TIME_SUFFIX_FOR_SECOND = " 21:00:00";
	public final static int AVG_MAX_MAIL_COUNT = 5;
	public final static int APPEND_MAIL_COUNT = 200;
	
	/**
	 * 邮件配置
	 */
	public final static String MAIL_PROTOCAL = "pop3";
	public final static String MAIL_HOST = "mail.pop.host";
	public final static String MAIL_USER = "mail.user";
	public final static String MAIL_PASSWORD = "mail.password";
	public final static String MAIL_FOLDER = "INBOX";

}
