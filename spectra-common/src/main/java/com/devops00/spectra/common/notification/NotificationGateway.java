package com.devops00.spectra.common.notification;

/** 业务模块使用的统一通知入口。 */
public interface NotificationGateway {

    /** 提交通知请求并返回投递回执。 */
    NotificationReceipt enqueue(NotificationRequest request);
}
