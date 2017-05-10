package com.boot;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
     * 主程序入口方法
     * @author chenguyan
     * @date 2017年5月5日  
     * @param args
     */
    public static void main(String[] args) {
    	try {
    		/**
    		 * ==========================================初始化及参数判断==========================================
    		 */
    		long startTime = System.currentTimeMillis();
    		if (args.length != 3) { // 参数数量不为3
    			System.out.println("Incorrect number of parameters!!!");
    			System.exit(1);
    		}
    		if(!StringUtil.dateFormat(args[1])) { // 日期格式不正确
    			System.out.println("Incorrect start date format, please enter the following date: 2017.05.05!!!");
    			System.exit(1);
    		}
    		if(!StringUtil.dateFormat(args[2])) {
    			System.out.println("Incorrect end date format, please enter the following date: 2017.05.05!!!");
    			System.exit(1);
    		}
    		if(!StringUtil.compareDate(args[1], args[2])) { // 开始大于结束日期
    			System.out.println("Start date is greater than end date!!!");
    			System.exit(1);
    		}
    		int port = Integer.parseInt(PropUtil.getValFromProp("/application.properties", "server.port")); // 获取配置文件中的端口
			if (NetUtil.isPortUsing("127.0.0.1", port)) { // 端口被占用
				System.out.println(port + " port is occupied, please close the process!!!");
    			System.exit(1);
			}
    		
    		
			/**
    		 * ==========================================SpringBoot启动==========================================
    		 */
			SpringApplication.run(Application.class, args);
			System.out.println("============= SpringBoot Start Success =============");
			
			
			
			/**
			 * ==========================================JavaMail==========================================
			 */
			/**
			 * 各种配置及连接
			 */
	        final Properties prop = PropUtil.getPropFromMail(); 
	        System.out.println("Connecting " + prop.getProperty("mail.user") + " POP service...");
	        
	        Session mailSession = Session.getDefaultInstance(prop, null); // 获取会话
	        
	        Store store = mailSession.getStore("pop3"); // 创建商店并连接服务
	        store.connect(prop.getProperty("mail.pop.host"), prop.getProperty("mail.user"), prop.getProperty("mail.password"));
	        
	        Folder folder = store.getFolder("INBOX"); // 创建文件夹并打开
	        folder.open(Folder.READ_WRITE);
	        
	        /**
	         * 搜索关键词
	         */
	        System.out.println("Connect successfully, searching。。。");
	        SearchTerm dateGETerm = new SentDateTerm(ComparisonTerm.GE, StringUtil.strToDate(args[1] + " 00:00:00")); // >=开始日期关键词
	        SearchTerm dateLETerm = new SentDateTerm(ComparisonTerm.LE, StringUtil.strToDate(args[2] + " 24:24:24")); // <=结束日期关键词
	        SearchTerm dateTerm = new AndTerm(dateGETerm, dateLETerm); // 合并日期关键词
	        SearchTerm subjectTerm = new SubjectTerm(args[0]); // 标题关键词
	        SearchTerm searchTerm = new AndTerm(subjectTerm, dateTerm); 
	        
	        Message[] messages = folder.search(searchTerm);
	        int messageCount = messages.length;
	        System.out.println("A total of " + messageCount + " daily reports search by \"" + args[0] + "   " + args[1] + " ~ " + args[2] + "\".");
	        
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
        	 * 关闭连接
        	 */
			folder.close(false); // 关闭文件夹
			store.close(); // 关闭商店
			
			/**
			 * 批量插入
			 */
			if (messageCount > 0) { // 匹配到数据
				DaliyReportMapper daliyReportMapper = (DaliyReportMapper) SpringBeanTool.getBean(DaliyReportMapper.class); // 反射获取Mapper
				daliyReportMapper.addBatch(list); 
				System.out.println("Insert " + messageCount + " daily reports successfully.");
			}
			
			/**
			 * 结果输出
			 */
			long endTime = System.currentTimeMillis(); 
			System.out.println("Program usage time: " + (endTime - startTime) / 1000 + "s, please press Ctrl+C to close the process!!!");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("SpringBoot服务端口出现数字类型异常!!!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}
    }
 
} 