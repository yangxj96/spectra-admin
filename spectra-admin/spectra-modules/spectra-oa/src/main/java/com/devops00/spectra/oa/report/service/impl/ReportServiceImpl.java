package com.devops00.spectra.oa.report.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.report.javabean.entity.Report;
import com.devops00.spectra.oa.report.mapper.ReportMapper;
import com.devops00.spectra.oa.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报表表-服务默认实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/30 11:44
 */
@Slf4j
@Service
public class ReportServiceImpl extends BaseServiceImpl<ReportMapper, Report> implements ReportService {
}
