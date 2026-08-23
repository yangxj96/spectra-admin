-- Provider 消息 ID 是外部回执关联和幂等处理的唯一依据；只对未删除且非空记录建立约束。
CREATE UNIQUE INDEX IF NOT EXISTS "UK_NTF_DELIVERY_PROVIDER_MESSAGE"
    ON spectra_notification.ntf_delivery (provider, provider_message_id)
    WHERE provider_message_id IS NOT NULL AND deleted IS NULL;

COMMENT ON INDEX spectra_notification."UK_NTF_DELIVERY_PROVIDER_MESSAGE"
    IS 'Provider 与消息 ID 唯一，防止重复外部回执产生重复投递记录';
