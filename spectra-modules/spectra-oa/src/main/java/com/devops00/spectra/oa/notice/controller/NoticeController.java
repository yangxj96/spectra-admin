package com.devops00.spectra.oa.notice.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.notice.javabean.entity.Notice;
import com.devops00.spectra.oa.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 公告主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:23
@RestController
@RequestMapping("/oa/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService bindService;

    @ULog("'分页查询公告'")
    @GetMapping("/page")
    public IPage<Notice> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
