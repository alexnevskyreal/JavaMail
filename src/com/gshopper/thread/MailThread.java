package com.gshopper.thread;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.mail.Folder;
import javax.mail.Message;

import com.gshopper.mapper.DaliyReportMapper;
import com.gshopper.model.DaliyReport;
import com.gshopper.util.StringUtil;

/**
 * 邮件线程
 * @author chenguyan
 * @date 2017年5月16日
 */
public class MailThread implements Callable<Integer> {
	
	private CopyOnWriteArrayList<DaliyReport> list = new CopyOnWriteArrayList<DaliyReport>(); // 防止并发
	private DaliyReportMapper daliyReportMapper;
	private Message[] messages; // 邮件
	private int startLoc;
	private int endLoc;
	private Date startDate;
	private Date endDate;
	private String[] subjects;

	public MailThread(DaliyReportMapper daliyReportMapper, Message[] messages,
			int startLoc, int endLoc, Date startDate, Date endDate,
			String[] subjects) {
		this.daliyReportMapper = daliyReportMapper;
		this.messages = messages;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.startDate = startDate;
		this.endDate = endDate;
		this.subjects = subjects;
	}

	@Override
	public Integer call() throws Exception {
		// TODO Auto-generated method stub
		Message message = null;
		boolean isMatch; // 是否匹配条件
		int compareSign = 0;
		
		for (int i = startLoc; i < endLoc; i++) { // 遍历邮件，从开始位置到结束
			message = messages[i]; 
			if (!message.getFolder().isOpen()) {
				message.getFolder().open(Folder.READ_ONLY);
			}
			
			isMatch = false;
			compareSign = StringUtil.compareDate(message.getSentDate(), startDate, endDate);
			
			if (compareSign == 1) { // 大于日期，则立即退出
				break;
			}
			
			if (compareSign == 0) { // 发送日期在开始结束时期之间
				isMatch = true;
			} else {
				continue; // 进行下一个
			}
			
			for (String containStr : subjects) { // 遍历标题
				if (StringUtil.isContainStr(message.getSubject(), containStr)) { // 包含任意一个标题，则添加到列表
					isMatch = true;
					break; // 匹配成功则跳出循环
				} else {
					isMatch = false;
				}
			}
			
			if (isMatch) { // 标题已匹配
				DaliyReport daliyReport = new DaliyReport(message.getSentDate(), message.getFrom().length > 0 ? StringUtil.getMailFromRegEx(message.getFrom()[0] + "") : "", message.getSubject(), message.getSize()); // 三目表达式获取邮件来源
				list.add(daliyReport); // 满足条件则添加
			}
		}
		
		if (list.size() > 0) {
			daliyReportMapper.addBatch(list); // 批量插入
			System.out.println("(Thread: " + Thread.currentThread().getName() + ": " + startLoc + " ~ " + endLoc + ") A total of " + list.size() + " mails were searched");
		} else {
			System.out.println("(Thread: " + Thread.currentThread().getName() + ": " + startLoc + " ~ " + endLoc + ") No search for mail");
		}
		return list.size();
	}

}
