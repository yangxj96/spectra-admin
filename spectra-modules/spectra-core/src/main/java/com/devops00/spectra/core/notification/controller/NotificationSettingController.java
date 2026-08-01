package com.devops00.spectra.core.notification.controller;

import com.devops00.spectra.core.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSettingVO;
import com.devops00.spectra.core.notification.service.NotificationSettingService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/// 通知设置控制器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Slf4j
@RestController
@RequestMapping("/notification/setting")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    /// 获取消息设置
    @ULog("'查询消息设置'")
    @GetMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION_SETTING:QUERY')")
    public NotificationSettingVO getSetting() {
        var userId = SecUtil.getCurrentUserId();
        return notificationSettingService.getSetting(userId);
    }

    /// 更新消息设置
    @ULog("'更新消息设置'")
    @PutMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'NOTIFICATION_SETTING:UPDATE')")
    public void updateSetting(@RequestBody NotificationSettingFrom from) {
        var userId = SecUtil.getCurrentUserId();
        notificationSettingService.updateSetting(userId, from);
    }
}
