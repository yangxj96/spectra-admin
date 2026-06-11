package com.devops00.spectra.oa.report.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.report.javabean.entity.Report;
import com.devops00.spectra.oa.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 报表主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:23
@RestController
@RequestMapping("/oa/report")
@RequiredArgsConstructor
public class ReportController {

    private ReportService bindService;

    @ULog("分页查询报表")
    @GetMapping("/page")
    public IPage<Report> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
