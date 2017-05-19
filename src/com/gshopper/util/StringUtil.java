package com.gshopper.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

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
		try {
			Pattern pattern = Pattern.compile(Constant.REG_EX_MAIL);
			Matcher matcher = pattern.matcher(mail);
			if (matcher.find()) {
				mail = matcher.group(0);
				mail = mail.substring(1, mail.length() - 1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return mail;
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
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return new Date();
	}
	
	/**
	 * 日期转换为字符串
	 * @author chenguyan
	 * @date 2017年5月16日  
	 * @param date
	 * @return
	 */
	public static String dateToStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		return sdf.format(date);
	}
	
	/**
	 * 日期转换为字符串
	 * @author chenguyan
	 * @date 2017年5月16日  
	 * @param date
	 * @return
	 */
	public static String dateToStr2(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		return sdf.format(date);
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
	
	/**
	 * 比较俩个日期
	 * @author chenguyan
	 * @date 2017年5月5日  
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static boolean compareDate2(String startTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		try {
			Date startDate = sdf.parse(startTime);
			Date endDate = sdf.parse(endTime);
			if (startDate.getTime() < endDate.getTime()) {
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
		}
		return false;
	}
	
	/**
	 * 比较日期大小
	 * @author chenguyan
	 * @date 2017年5月16日  
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int compareDate(Date originalDate, Date startDate, Date endDate) {
		if (originalDate.getTime() < startDate.getTime()) {
			return -1;
		}
		if (originalDate.getTime() > endDate.getTime()) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * 比较日期大小
	 * @author chenguyan
	 * @date 2017年5月18日  
	 * @param startTime
	 * @param endDate
	 * @return
	 */
	public static boolean compareDate(String startTime, Date endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		try {
			Date startDate = sdf.parse(startTime);
			if (startDate.getTime() <= endDate.getTime()) {
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
		}
		return false;
	}
	
	/**
	 * 是否包含字符串
	 * @author chenguyan
	 * @date 2017年5月16日  
	 * @param originalStr
	 * @param containStr
	 * @return
	 */
	public static boolean isContainStr(String originalStr, String containStr) { 
		if (originalStr == null || StringUtils.isEmpty(originalStr)) {
			return false;
		}
		return  originalStr.contains(containStr);
	}
	
	/**
	 * 打印字符串数组
	 * @author chenguyan
	 * @date 2017年5月16日  
	 * @param strs
	 * @return
	 */
	public static String strsToString(String[] strs) {
		StringBuilder sb = new StringBuilder(strs.length);
		sb.append("[");
		for (String str : strs) {
			sb.append(" " + str + ", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(" ]");
		return sb.toString();
	}
	
	/**
	 * 获取几天之前或之后的日期
	 * @author chenguyan
	 * @date 2017年5月18日  
	 * @param time
	 * @param beforeOrAfterDay
	 * @return
	 */
	public static Date getDateFromDay(String time, int beforeOrAfterDay) {
		Date date = strToDate(time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day + beforeOrAfterDay);
		return calendar.getTime();
	}
	
}
