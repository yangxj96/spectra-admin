package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.core.notification.constant.NotificationChannel;

/**
 * 外部通知渠道网关。短信、邮件供应商适配器通过此接口接入，业务层不直接依赖供应商 SDK。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationGateway {

    /**
     * 判断是否支持渠道。
     *
     * @param channel 通知渠道
     * @return 是否支持
     */
    boolean supports(NotificationChannel channel);

    /**
     * 发送已经渲染的通知内容。
     *
     * @param channel 通知渠道
     * @param address 收件地址
     * @param title 标题
     * @param content 正文
     * @return 供应商消息标识
     */
    String send(NotificationChannel channel, String address, String title, String content);
}
