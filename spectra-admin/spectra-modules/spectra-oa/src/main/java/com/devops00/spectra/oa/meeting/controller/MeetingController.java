package com.devops00.spectra.oa.meeting.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.configure.ulog.annotation.ULog;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 会议主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:23
@RestController
@RequestMapping("/oa/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService bindService;

    @ULog("分页查询会议")
    @GetMapping("/page")
    public IPage<Meeting> page(PageFrom page) {
        return bindService.page(page.toPage());
    }


}
