package com.devops00.spectra.notification.preference.controller;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.notification.preference.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.preference.service.NotificationPreferenceService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 当前用户通知偏好接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification-center/preferences")
public class NotificationPreferenceController {

    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);

    private final NotificationPreferenceService service;

    /** 查询当前用户用途×渠道偏好。 */
    @ULog("'查询通知偏好'")
    @GetMapping(version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationUserPreferenceEntity> list() {
        return service.list(SYSTEM_TENANT_ID, currentUserId());
    }

    /** 保存当前用户可选通知偏好。 */
    @ULog("'更新通知偏好'")
    @PutMapping(version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void save(@RequestParam String purpose, @RequestParam String channel, @RequestParam boolean enabled,
            @RequestParam(defaultValue = "false") boolean doNotDisturb) {
        service.save(SYSTEM_TENANT_ID, currentUserId(), purpose, channel, enabled, doNotDisturb);
    }

    private UUID currentUserId() {
        var userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("当前用户未登录");
        }
        return userId;
    }
}
