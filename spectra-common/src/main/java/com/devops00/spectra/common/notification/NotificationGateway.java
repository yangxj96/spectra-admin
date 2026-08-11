package com.devops00.spectra.common.notification;

/** 业务模块使用的统一通知入口。 */
public interface NotificationGateway {

    /** 查询渠道是否已配置并可接受投递。 */
    NotificationChannelAvailability availability(NotificationChannel channel);

    /** 提交通知请求并返回投递回执。 */
    NotificationReceipt enqueue(NotificationRequest request);
}
