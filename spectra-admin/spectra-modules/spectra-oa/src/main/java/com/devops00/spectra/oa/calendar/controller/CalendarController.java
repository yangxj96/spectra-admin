package com.devops00.spectra.oa.calendar.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.calendar.javabean.entity.Calendar;
import com.devops00.spectra.oa.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 日历主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:21
@RestController
@RequestMapping("/oa/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService bindService;

    @ULog("分页查询日历")
    @GetMapping("/page")
    public IPage<Calendar> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
