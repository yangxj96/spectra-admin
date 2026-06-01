package com.devops00.spectra.oa.meeting.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /// 创建一个会议
    @ULog("创建会议")
    @PostMapping
    public void create(@RequestBody MeetingCreateFrom from) {
        bindService.create(from);
    }

    @ULog("分页查询会议")
    @GetMapping("/page")
    public IPage<Meeting> page(PageFrom page) {
        return bindService.page(page.toPage());
    }


}
