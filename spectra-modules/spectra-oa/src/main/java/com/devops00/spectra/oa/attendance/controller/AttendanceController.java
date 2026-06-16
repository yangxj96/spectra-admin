package com.devops00.spectra.oa.attendance.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.attendance.javabean.entity.Attendance;
import com.devops00.spectra.oa.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 考勤主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:21
@RestController
@RequestMapping("/oa/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService bindService;

    @ULog("'分页查询考勤'")
    @GetMapping("/page")
    public IPage<Attendance> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
