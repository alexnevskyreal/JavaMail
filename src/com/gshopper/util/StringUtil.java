package com.gshopper.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具
 * @author chenguyan
 * @date 2017年5月5日
 */
public class StringUtil {
	
	/**
	 * 获取邮箱从正则表达式中
	 * @author chenguyan
	 * @date 2017年5月5日  
	 * @param mail
	 * @return 匹配的邮件地址
	 */
	public static String getMailFromRegEx(String mail) {
		Pattern pattern = Pattern.compile(Constant.REG_EX_MAIL);
		Matcher matcher = pattern.matcher(mail);
		if (matcher.find()) {
			mail = matcher.group(0);
			mail = mail.substring(1, mail.length()-1);
		}
		return mail;
	}
	
	/**
	 * 日期转换字符串
	 * @author chenguyan
	 * @date 2017年5月5日  
	 * @param date
	 * @return
	 */
	public static String dateToStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		return sdf.format(date);
	}
	
	/**
	 * 字符串转换日期
	 * @author chenguyan
	 * @date 2017年5月5日  
	 * @param str
	 * @return
	 */
	public static Date strToDate(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
		}
		return new Date();
	}
	
	/**
	 * 日期格式
	 * @author chenguyan
	 * @date 2017年5月5日  
	 * @param str
	 * @return
	 */
	public static boolean dateFormat(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		try {
			@SuppressWarnings("unused")
			Date date = sdf.parse(str);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 比较俩个日期
	 * @author chenguyan
	 * @date 2017年5月5日  
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static boolean compareDate(String startTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		try {
			Date startDate = sdf.parse(startTime);
			Date endDate = sdf.parse(endTime);
			if (startDate.getTime() <= endDate.getTime()) {
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
		}
		return false;
	}
 	
}
