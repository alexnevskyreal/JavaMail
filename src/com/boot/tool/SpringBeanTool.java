package com.boot.tool;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * SpringBean工具
 * @author chenguyan
 * @date 2017年5月4日
 */
@Configuration
public class SpringBeanTool implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public static Object getBean(Class<?> clazz) {
		return applicationContext.getBean(clazz);
	}
	
}


