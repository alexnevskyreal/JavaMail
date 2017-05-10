package com.gshopper.mapper;

import java.util.List;

import com.gshopper.model.DaliyReport;

/**
 * 日常报告接口
 * @author chenguyan
 * @date 2017年5月4日
 */
public interface DaliyReportMapper {
	
	public void addBatch(List<DaliyReport> list);

}
