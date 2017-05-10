package com.gshopper.model;

import java.util.Date;

/**
 * 日常报告
 * @author chenguyan
 * @date 2017年5月4日
 */
public class DaliyReport {
	
	private Date email_time;
	private String email_account;
	private String email_title;
	private long email_size;
	
	public DaliyReport(Date email_time, String email_account,
			String email_title, long email_size) {
		this.email_time = email_time;
		this.email_account = email_account;
		this.email_title = email_title;
		this.email_size = email_size;
	}
	
	public DaliyReport() {
	}

	public Date getEmail_time() {
		return email_time;
	}

	public void setEmail_time(Date email_time) {
		this.email_time = email_time;
	}

	public String getEmail_account() {
		return email_account;
	}

	public void setEmail_account(String email_account) {
		this.email_account = email_account;
	}

	public String getEmail_title() {
		return email_title;
	}

	public void setEmail_title(String email_title) {
		this.email_title = email_title;
	}

	public long getEmail_size() {
		return email_size;
	}

	public void setEmail_size(long email_size) {
		this.email_size = email_size;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "DaliyReport [" + email_time + ", " + email_account + ", " + email_title + ", " + email_size + "]";
	}

}
