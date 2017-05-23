package com.boot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.boot.tool.SpringBeanTool;
import com.gshopper.mapper.DaliyReportMapper;
import com.gshopper.thread.MailThread;
import com.gshopper.util.Constant;
import com.gshopper.util.NetUtil;
import com.gshopper.util.PropUtil;
import com.gshopper.util.StringUtil;

/**
 * 应用程序启动入口
 * @author chenguyan
 * @date 2017年5月4日
 */
@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan
@MapperScan("com.gshopper.mapper")
public class Application {
	
    private static Logger logger = Logger.getLogger(Application.class);
    
    private static int firstMiddleLoc; // 第一次中间位置
    private static int secondMiddleLoc; // 第二次中间位置
    
    /**
     * DataSource配置
     * @author chenguyan
     * @date 2017年5月4日  
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix="spring.datasource") // 根据前缀在application.properties中匹配值
    public DataSource dataSource() {
        return new org.apache.tomcat.jdbc.pool.DataSource();
    }
 
    /**
     * 提供SqlSeesion
     * @author chenguyan
     * @date 2017年5月4日  
     * @return
     * @throws Exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
 
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
 
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/*.xml"));
 
        return sqlSessionFactoryBean.getObject();
    }
 
    /**
     * 事物管理
     * @author chenguyan
     * @date 2017年5月5日  
     * @return
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    
    /**
     * 验证
     * @author chenguyan
     * @date 2017年5月11日  
     * @param args
     * @return
     * @exception 数字类型转换异常
     */
    public static boolean isValid(String[] args) {
    	try {
    		int port = Integer.parseInt(PropUtil.getValFromProp("/application.properties", "server.port")); // 获取配置文件中的端口
			if (NetUtil.isPortUsing("127.0.0.1", port)) { // 端口被占用
				System.out.println(port + " port is occupied, please close the process!!!");
				return false;
			}
	    	if (args.length < 3) { // 参数数量小于3
				System.out.println("Incorrect number of parameters!!!");
				return false;
			}
			if (!StringUtil.dateFormat(args[0]) || !StringUtil.dateFormat(args[1])) { // 日期格式不正确
				System.out.println("Incorrect date format, please enter date format: 2017.05.05!!!");
				return false;
			}
			if (!StringUtil.compareDate(args[0], args[1])) { // 开始大于结束日期
				System.out.println("Start date is greater than end date!!!");
				return false;
			}
			if (!StringUtil.compareDate(args[1], new Date())) { // 结束日期大于当天
				System.out.println("End date is greater than today!!!");
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.out.println("Please enter a number port!!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		return false;
    }
 
    /**
     * 主程序入口方法
     * @author chenguyan
     * @date 2017年5月5日  
     * @param args
     */
    public static void main(String[] args) {
    	try {
    		long startTime = System.currentTimeMillis(); // 开始时间
    		/**
    		 * ==========================================参数验证==========================================
    		 */
    		if (!isValid(args)) { // 验证失败
    			System.exit(1); // 退出程序
    		}
    		
    		
    		
			/**
    		 * ==========================================SpringBoot启动==========================================
    		 */
			SpringApplication.run(Application.class, args);
			System.out.println("============================ SpringBoot Start Success ============================");
			
			
			
			/**
			 * ==========================================JavaMail==========================================
			 */
			/**
			 * 配置及连接
			 */
	        final Properties prop = PropUtil.getPropFromMail();
	        Session mailSession = Session.getDefaultInstance(prop, null); // 邮件会话
	        
	        Store store = mailSession.getStore(Constant.MAIL_PROTOCAL); // 过去商店并连接
	        store.connect(prop.getProperty(Constant.MAIL_HOST), prop.getProperty(Constant.MAIL_USER), prop.getProperty(Constant.MAIL_PASSWORD));
	        
	        Folder folder = store.getFolder(Constant.MAIL_FOLDER); // 打开收件箱
	        folder.open(Folder.READ_ONLY);

	        /**
	         * 搜索所有邮件
	         */
	        System.out.println("Connected " + prop.getProperty(Constant.MAIL_USER) + " successfully");
	        Message[] messages = folder.getMessages(); // 获取所有邮件，时间从早到晚排序（移动邮件可导致排序紊乱）
	        int messageCount = messages.length; // 邮件数量 
	        System.out.println("There are " + messageCount + " mails in the mailbox");

	        /**
	         * 相关算法（扩大客户端输入的开始结束俩个时间点，分别扩大一些时间，中间排序获取俩个时间点的位置，再用多线程批量跑）
	         */
	        Date firstStartDate = StringUtil.getDateFromDay(args[0] + Constant.START_TIME_SUFFIX_FOR_FIRST, -1); // 第一次开始时间，扩大客户端输入的开始时间
	        Date firstEndDate = StringUtil.getDateFromDay(args[0] + Constant.END_TIME_SUFFIX_FOR_FIRST, -1); // 第一次结束时间
	        searchMiddleLocForMuiltiThread(messages, firstStartDate, firstEndDate, messageCount, messageCount / 2, true); // 搜索中间位置为多线程，第一次
    		System.out.println("First middle loc: " + firstMiddleLoc);
	        
	        if (!StringUtil.compareDate2(args[1], StringUtil.dateToStr2(new Date()))) { // 结束日期大于当前时间，则改为最后一位位置
	        	secondMiddleLoc = messageCount; 
	        } else { // 否则计算位置
		        Date secondStartDate = StringUtil.getDateFromDay(args[1] + Constant.START_TIME_SUFFIX_FOR_SECOND, 1); // 第二次开始时间，扩大客户端输入的结束时间
		        Date secondEndDate = StringUtil.getDateFromDay(args[1] + Constant.END_TIME_SUFFIX_FOR_SECOND, 1); // 第二次结束时间
		        searchMiddleLocForMuiltiThread(messages, secondStartDate, secondEndDate, messageCount, messageCount / 2, false); // 搜索中间位置为多线程，第二次
	        }
    		System.out.println("Second middle loc: " + secondMiddleLoc);
        	
	        /**
	         * 配置线程所用属性
	         */
	        DaliyReportMapper daliyReportMapper = (DaliyReportMapper) SpringBeanTool.getBean(DaliyReportMapper.class); // 反射获取Mapper
	        Date startDate = StringUtil.strToDate(args[0] + Constant.START_DATE_SUFFIX); // 开始时间
	        Date endDate = StringUtil.strToDate(args[1] + Constant.END_DATE_SUFFIX); // 结束时间
	        String[] subjects = new String[args.length - 2]; // 标题数组，不算开始结束时间
	        for (int i = 2; i < args.length; i++) { // 循环赋值标题
	        	subjects[i - 2] = args[i];
	        }
	        
	        /**
	         * 线程开启
	         */
	        System.out.println("searching");
	        int threadPoolSize = Integer.parseInt(PropUtil.getValFromProp("/thread.properties", "thread.pool.size")); // 线程池大小
	        int insertTotal = 0; // 插入总数
	        if (secondMiddleLoc - firstMiddleLoc > Constant.AVG_MAX_MAIL_COUNT * threadPoolSize) { // 计算的邮件量大，采用多线程（保证每个线程至少有几封邮件）
	        	ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize); // 固定线程池
	        	
	        	List<Future<Integer>> list = new ArrayList<Future<Integer>>();  
	        	int startLoc = 0; // 开始位置
	        	int endLoc = 0; // 结束位置
	        	int averMessageCount = (secondMiddleLoc - firstMiddleLoc) / threadPoolSize; // 计算每个线程的邮件数
	        	
	        	for (int i = 1; i <= threadPoolSize; i++) {
	        		if (i == 1) {
	        			startLoc = firstMiddleLoc;
	        		} else {
	        			startLoc = firstMiddleLoc + (i - 1) * averMessageCount;
	        		}
	        		if (i == threadPoolSize) { // 最后一个线程
	        			endLoc = (secondMiddleLoc + Constant.APPEND_MAIL_COUNT) >= messageCount ? messageCount : (secondMiddleLoc + Constant.APPEND_MAIL_COUNT);
	        		} else {
	        			endLoc = startLoc + averMessageCount;
	        		}
	        		
	        		MailThread umailThread = new MailThread(daliyReportMapper, messages, startLoc, endLoc, startDate, endDate, subjects);
	        		list.add(executor.submit(umailThread));	
	        	}
	        	executor.shutdown(); 
	        	while (true) { // 判断线程池中的任务是否都执行完
					if (executor.isTerminated()) {
						break;
					}
				}
	        	
	        	try { // 因为是线程异步回调结果，所以增加超时时间，捕获抛出的异常信息
	        		for (Future<Integer> future : list) { // 计算线程回调返回的结果
		                insertTotal += future.get(1, TimeUnit.DAYS); // 超时时间为天
		            }
				} catch (TimeoutException e) {
					// TODO: handle exception
					e.printStackTrace();
					System.out.println("线程回调结果超时");
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					logger.error(e.getMessage());
				}
	        } else { // 计算的邮件量小，采用单线程
	        	if (firstMiddleLoc > secondMiddleLoc) { // 互换位置
	        		int temp = firstMiddleLoc;
	        		firstMiddleLoc = secondMiddleLoc;
	        		secondMiddleLoc = temp;
	        	}
	        	MailThread mailThread = new MailThread(daliyReportMapper, messages, firstMiddleLoc, (secondMiddleLoc + Constant.APPEND_MAIL_COUNT) >= messageCount ? messageCount : (secondMiddleLoc + Constant.APPEND_MAIL_COUNT), startDate, endDate, subjects);
	        	insertTotal = mailThread.call();
	        }
	        
        	/**
        	 * 关闭
        	 */
			if (folder.isOpen()) {
				folder.close(false); // 关闭文件夹
			}
			if (store.isConnected()) {
				store.close(); // 关闭商店
			}
        	
        	/**
        	 * 结束
        	 */
			long endTime = System.currentTimeMillis(); // 结束时间
			System.out.println("Insert " + insertTotal + " daily reports successfully by " + StringUtil.dateToStr2(startDate) + " ~ " + StringUtil.dateToStr2(endDate) + " , and subjects: " + StringUtil.strsToString(subjects));
			System.out.println("Program usage time: " + (endTime - startTime) / 1000 + "s");
			
			System.exit(1); // 关闭、退出程序
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		} 
    }
    
    /**
     * 搜索邮件位置降低多线程搜索数量
     * @author chenguyan
     * @date 2017年5月17日  
     * @param messages
     * @param startDate
     * @param endDate
     * @param middlePreLoc 上一次的结果
     * @param middleNextLoc 下一次的结果
     * @return 数组是防止递归重复赋值
     * @throws Exception 
     */
    private static void searchMiddleLocForMuiltiThread(Message[] messages, Date startDate, Date endDate, int middlePreLoc, int middleNextLoc, boolean isFirst) {
    	try {
        	Message message = messages[middleNextLoc - 1];
        	int compare = StringUtil.compareDate(message.getSentDate(), startDate, endDate);
        	switch (compare) { 
			case 1: // 大于结束日期
				middlePreLoc = middleNextLoc;
				middleNextLoc /= 2;		
				if (middleNextLoc == 0) { // 计算到最后的位置，返回
					if (isFirst) {
						firstMiddleLoc = middleNextLoc;
					} else {
						secondMiddleLoc = middleNextLoc;
					}
					return ; // 退出递归
				}
				searchMiddleLocForMuiltiThread(messages, startDate, endDate, middlePreLoc, middleNextLoc, isFirst); // 再次递归
				break;
			case -1: // 小于开始日期
				int tempLoc = middlePreLoc; // 临时变量
				middlePreLoc = middleNextLoc;
				if (tempLoc > middleNextLoc) { // 向前移动
					middleNextLoc = (tempLoc + middleNextLoc) / 2;
				} else { // 向后移动
					middleNextLoc = middleNextLoc + (middleNextLoc - tempLoc) / 2;
				}
				if (middlePreLoc == middleNextLoc) { // 旧与新计算的相同，则返回
					if (isFirst) {
						firstMiddleLoc = middleNextLoc;
					} else {
						secondMiddleLoc = middleNextLoc;
					}
					return ; // 退出递归
				}
				searchMiddleLocForMuiltiThread(messages, startDate, endDate, middlePreLoc, middleNextLoc, isFirst); // 再次递归
				break;
			default: // 开始结束日期之间
				if (isFirst) {
					firstMiddleLoc = middleNextLoc;
				} else {
					secondMiddleLoc = middleNextLoc;
				}
				return ; // 退出递归
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}
    }
    
} 