package com.devops00.spectra.core.notification.service;

/** 待发送通知任务处理器。 */
public interface NotificationTaskWorker {

    /** 处理一批到期任务。 */
    int processPending(int limit);
}
