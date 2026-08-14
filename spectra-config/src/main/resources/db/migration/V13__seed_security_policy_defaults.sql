-- Seed the policy rows consumed by the security runtime.
--
-- The ids are deterministic so a re-run or a fresh target database gets the
-- same client identity without introducing a second mapping table.
INSERT INTO spectra_security.security_client (id, code, name, state)
VALUES
    (md5('security-client:web')::uuid, 'web', 'Web 浏览器', 'ACTIVE'),
    (md5('security-client:app')::uuid, 'app', '移动 App', 'ACTIVE'),
    (md5('security-client:mini')::uuid, 'mini', '微信小程序', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO spectra_security.session_policy (
    client_id,
    concurrency_mode,
    allow_concurrent,
    max_sessions,
    access_ttl_seconds,
    refresh_ttl_seconds,
    absolute_ttl_seconds,
    idle_ttl_seconds
)
SELECT id, 'ALLOW', TRUE, 5, 300, 604800, NULL, NULL
FROM spectra_security.security_client
WHERE code IN ('web', 'app', 'mini')
ON CONFLICT (client_id) DO NOTHING;

INSERT INTO spectra_security.password_policy (
    policy_key,
    min_length,
    require_uppercase,
    require_lowercase,
    require_digit,
    require_special,
    max_age_days
)
VALUES ('SYSTEM', 12, TRUE, TRUE, TRUE, TRUE, NULL)
ON CONFLICT (policy_key) DO NOTHING;
