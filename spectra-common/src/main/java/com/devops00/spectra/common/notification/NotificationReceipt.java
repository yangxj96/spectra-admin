package com.devops00.spectra.common.notification;

import java.util.UUID;

/** 通知入队回执。 */
public record NotificationReceipt(UUID requestId, String status, int taskCount, boolean idempotentReplay) {
}
