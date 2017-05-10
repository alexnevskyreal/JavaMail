package com.gshopper.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import org.apache.log4j.Logger;

/**
 * 邮件工具
 * @author chenguyan
 * @date 2017年5月3日
 */
public class MailUtil {
	
	private static Logger logger = Logger.getLogger(MailUtil.class);
	
	/**
	 * 获取邮件会话
	 * @author chenguyan
	 * @date 2017年5月3日  
	 * @param prop
	 * @return 会话
	 */
	public static Session getMailSession(final Properties prop) {
		// 构建授权信息，用于进行SMTP进行身份验证
		Authenticator auth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				String userName = prop.getProperty("mail.user");
                String password = prop.getProperty("mail.password");
				return new PasswordAuthentication(userName, password);
			}
		};
		
		// 使用环境属性和授权信息，创建邮件会话
        return Session.getInstance(prop, auth);
	}
	
	/**
	 * 发送邮件
	 * @author chenguyan
	 * @date 2017年5月3日  
	 * @param mailSession
	 * @param sendUser
	 * @param receiveUser
	 * @param subject
	 * @param content
	 * @return 发送成功返回true
	 * @exception 地址错误
	 */
	public static boolean sendMail(Session mailSession, String sendUser, String receiveUser, String subject, String content) {
		 // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        // 设置发件人
        InternetAddress form = null;
        // 设置收件人
        InternetAddress to = null;
		try {
			form = new InternetAddress(sendUser);
			message.setFrom(form);
			to = new InternetAddress(receiveUser);
			message.setRecipient(RecipientType.TO, to);
			
			// 设置邮件标题
	        message.setSubject(subject);
	        // 设置邮件的内容体
	        message.setContent(content, Constant.CONTENT_TYPE);

	        // 发送邮件
	        Transport.send(message);
	        return true;
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("地址无效");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 搜索邮件
	 * @author chenguyan
	 * @date 2017年5月8日  
	 * @param searchTerm
	 * @return
	 */
	public static Message[] searchMessage(SearchTerm searchTerm) {
		Store store = null;
		Folder folder = null;
		Message[] messages = null;
		try {
			final Properties prop = PropUtil.getPropFromMail(); 
	        System.out.println("Connecting " + prop.getProperty("mail.user") + " POP service...");
	        
	        Session mailSession = Session.getDefaultInstance(prop, null); // 获取会话
	        
	        store = mailSession.getStore("pop3"); // 创建商店并连接服务
	        store.connect(prop.getProperty("mail.pop.host"), prop.getProperty("mail.user"), prop.getProperty("mail.password"));
	        
	        folder = store.getFolder("INBOX"); // 创建文件夹并打开
	        folder.open(Folder.READ_WRITE);
	        
	        messages = folder.search(searchTerm);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			try {
				folder.close(false); // 关闭文件夹
				store.close(); // 关闭商店
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}
		}
		return messages;
	}

}
