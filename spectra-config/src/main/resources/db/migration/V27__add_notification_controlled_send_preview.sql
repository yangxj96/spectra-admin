-- 受控发送 Preview 短时快照；仅保存非敏感请求、摘要和一次性消费状态。
CREATE TABLE IF NOT EXISTS spectra_notification.ntf_send_preview (
    id                  UUID PRIMARY KEY,
    operator_user_id    UUID NOT NULL,
    request_hash        VARCHAR(64) NOT NULL,
    preview_token_hash  VARCHAR(64) NOT NULL,
    resolution_hash     VARCHAR(64) NOT NULL,
    request_snapshot    JSONB NOT NULL DEFAULT '{}'::jsonb,
    expires_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    consumed_at         TIMESTAMP(6) WITH TIME ZONE,
    applied_request_id  UUID,
    status              VARCHAR(20) NOT NULL DEFAULT 'PREVIEWED',
    created_by          UUID,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             TIMESTAMP(6) WITH TIME ZONE,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT "CK_NTF_SEND_PREVIEW_STATUS"
        CHECK (status IN ('PREVIEWED', 'APPLYING', 'APPLIED', 'EXPIRED')),
    CONSTRAINT "CK_NTF_SEND_PREVIEW_HASH"
        CHECK (request_hash ~ '^[0-9a-f]{64}$'
               AND preview_token_hash ~ '^[0-9a-f]{64}$'
               AND resolution_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX IF NOT EXISTS "IDX_NTF_SEND_PREVIEW_EXPIRES"
    ON spectra_notification.ntf_send_preview (expires_at, status);

CREATE INDEX IF NOT EXISTS "IDX_NTF_SEND_PREVIEW_OPERATOR"
    ON spectra_notification.ntf_send_preview (operator_user_id, created_at DESC)
    WHERE deleted IS NULL;

COMMENT ON TABLE spectra_notification.ntf_send_preview IS '受控发送短时 Preview 快照，不保存完整用户清单、地址或敏感参数';
COMMENT ON COLUMN spectra_notification.ntf_send_preview.request_snapshot IS '非敏感受控发送请求快照，过期后物理删除';
