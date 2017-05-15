package com.boot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;
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
import com.gshopper.model.DaliyReport;
import com.gshopper.util.NetUtil;
import com.gshopper.util.PropUtil;
import com.gshopper.util.StringUtil;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

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
	    	if (args.length != 3) { // 参数数量不为3
				System.out.println("Incorrect number of parameters!!!");
				return false;
			}
			if(!StringUtil.dateFormat(args[1])) { // 日期格式不正确
				System.out.println("Incorrect start date format, please enter the following date: 2017.05.05!!!");
				return false;
			}
			if(!StringUtil.dateFormat(args[2])) {
				System.out.println("Incorrect end date format, please enter the following date: 2017.05.05!!!");
				return false;
			}
			if(!StringUtil.compareDate(args[1], args[2])) { // 开始大于结束日期
				System.out.println("Start date is greater than end date!!!");
				return false;
			}
			int port = Integer.parseInt(PropUtil.getValFromProp("/application.properties", "server.port")); // 获取配置文件中的端口
			if (NetUtil.isPortUsing("127.0.0.1", port)) { // 端口被占用
				System.out.println(port + " port is occupied, please close the process!!!");
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.out.println("Please enter a digital port number!!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
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
	        System.out.println("Connecting " + prop.getProperty("mail.user") + " POP service");
	        
	        Session mailSession = Session.getDefaultInstance(prop, null); // 获取会话
	        
	        Store store = mailSession.getStore("pop3"); // 创建商店并连接服务
	        store.connect(prop.getProperty("mail.pop.host"), prop.getProperty("mail.user"), prop.getProperty("mail.password"));
	        
	        Folder folder = store.getFolder("INBOX"); // 创建文件夹并打开
	        folder.open(Folder.READ_WRITE);
	        
	        /**
	         * 搜索关键词
	         */
	        System.out.println("Service and other connections completed, searching");
	        SearchTerm dateGETerm = new SentDateTerm(ComparisonTerm.GE, StringUtil.strToDate(args[1] + " 00:00:00")); // >=开始日期关键词
	        SearchTerm dateLETerm = new SentDateTerm(ComparisonTerm.LE, StringUtil.strToDate(args[2] + " 24:24:24")); // <=结束日期关键词
	        SearchTerm dateTerm = new AndTerm(dateGETerm, dateLETerm); // 合并日期关键词
	        SearchTerm subjectTerm = new SubjectTerm(args[0]); // 标题关键词
	        SearchTerm searchTerm = new AndTerm(subjectTerm, dateTerm); // 合并标题日期关键词
	        
	        Message[] messages = folder.search(searchTerm);
	        int messageCount = messages.length;
	        System.out.println("A total of " + messageCount + " daily reports search by subject: " + args[0] + "   , time interval: " + args[1] + " ~ " + args[2] + "");
	        
	        /**
	         * 遍历邮件到集合
	         */
	        List<DaliyReport> list = new ArrayList<DaliyReport>();
	        DaliyReport daliyReport = null;
	        String messageFrom = null;
        	for (Message message : messages) { // 遍历添加到list中
				messageFrom = StringUtil.getMailFromRegEx(message.getFrom()[0] + ""); // 字符过滤后的邮箱
        		daliyReport = new DaliyReport(message.getSentDate(), messageFrom, message.getSubject(), message.getSize());
        		list.add(daliyReport);
        	}
        				
			/**
			 * 批量插入
			 */
			if (messageCount > 0) { // 匹配到数据
				DaliyReportMapper daliyReportMapper = (DaliyReportMapper) SpringBeanTool.getBean(DaliyReportMapper.class); // 反射获取Mapper
				daliyReportMapper.addBatch(list); 
				System.out.println("Insert " + messageCount + " daily reports successfully");
			}
			
			/**
			 * 关闭
			 */
			folder.close(false); // 关闭文件夹
        	store.close(); // 关闭商店

			long endTime = System.currentTimeMillis(); // 结束时间
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
     * 无用方法
     * @author chenguyan
     * @date 2017年5月11日  
     * @throws Exception
     */
    public void uselessMethod() throws Exception {
    	final Properties prop = PropUtil.getPropFromMail(); 
        System.out.println("Connecting " + prop.getProperty("mail.user") + " POP service");
        
        Session mailSession = Session.getDefaultInstance(prop, null); // 获取会话
        
        Store store = mailSession.getStore("pop3"); // 创建商店并连接服务
        store.connect(prop.getProperty("mail.pop.host"), prop.getProperty("mail.user"), prop.getProperty("mail.password"));
        
        Folder folder = store.getFolder("INBOX"); // 创建文件夹并打开
        folder.open(Folder.READ_WRITE);
    	
    	/**
		 * 用IMAP协议创建文件夹
		 */
		IMAPStore iMAPStore = (IMAPStore) mailSession.getStore("imap");
		iMAPStore.connect(prop.getProperty("mail.imap.host"), prop.getProperty("mail.user"), prop.getProperty("mail.password"));
		
		String folderName = StringUtil.getFolderName(); System.out.println(folderName);
		IMAPFolder iMAPFolder = (IMAPFolder) iMAPStore.getFolder(folderName);
		if (!iMAPFolder.exists()) { // 不存在目录，则创建
			iMAPFolder.create(Folder.HOLDS_MESSAGES);
		}
		
		/**
		 * 移动文件
		 */
		Folder folderForMove = store.getFolder(folderName); // 创建文件夹并打开
		folderForMove.open(Folder.READ_WRITE);
		System.out.println("Moving files 1 months ago to " + folderName + " folder");
		
		
		SearchTerm dateLETermForMove = new SentDateTerm(ComparisonTerm.LT, StringUtil.strToDate(folderName + ".01 00:00:00")); // <当月1日
		Message[] messagesForMove = folder.search(dateLETermForMove);
		if (messagesForMove.length > 0) { // 有要移动的日报
			folder.copyMessages(messagesForMove, folderForMove); // 拷贝到新建目录
			folder.setFlags(messagesForMove, new Flags(Flag.DELETED), true); // 删除移动的日报
		}
    }
 
} 