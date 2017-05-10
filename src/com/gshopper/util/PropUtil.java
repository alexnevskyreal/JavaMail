package com.gshopper.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件工具
 * @author chenguyan
 * @date 2017年5月3日
 */
public class PropUtil {
	
	private final static String MAIL = "/mail.properties"; // 邮件配置文件的路径
	
	/**
	 * 获取配置文件
	 * @author chenguyan
	 * @date 2017年5月3日  
	 * @return 配置文件
	 * @exception 文件的路径错误可导致空指针异常
	 */
	public static Properties getPropFromMail() {
		Properties prop = new Properties();
		try {
			InputStream is = Thread.currentThread().getClass().getResourceAsStream(MAIL);
			prop.load(is);		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return prop;
	}
	
	/**
	 * 获取值从配置文件中
	 * @author chenguyan
	 * @date 2017年5月4日  
	 * @param propPath
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public static String getValFromProp(String propPath, String key) throws IOException {
		Properties prop = new Properties();
		InputStream is = Thread.currentThread().getClass().getResourceAsStream(propPath);
		prop.load(is);	
		return prop.getProperty(key);
	}

}
