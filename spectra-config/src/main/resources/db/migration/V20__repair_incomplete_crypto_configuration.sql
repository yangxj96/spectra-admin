/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

-- 历史版本可能在密钥迁移完成前保存了开启状态；不完整时先关闭，避免启动后以故障状态进入加密响应链路。
-- 仅修复四个 RSA 密钥未形成完整 ACTIVE 集合的配置，不生成、删除或覆盖任何密钥版本。
UPDATE spectra_core.sys_config AS config
SET value = 'false',
    remarks = '密钥管理页面配置的接口加解密开关',
    updated_at = CURRENT_TIMESTAMP,
    version = config.version + 1
WHERE config.key = 'crypto.enabled'
  AND lower(btrim(config.value)) = 'true'
  AND config.deleted IS NULL
  AND (
      SELECT COUNT(*)
      FROM spectra_security.sec_secret_definition AS definition
      JOIN spectra_security.sec_secret_version AS secret_version
        ON secret_version.secret_definition_id = definition.id
       AND secret_version.state = 'ACTIVE'
       AND secret_version.deleted IS NULL
      WHERE definition.code IN (
          'crypto.server.public-key',
          'crypto.server.private-key',
          'crypto.client.public-key',
          'crypto.client.private-key'
      )
        AND definition.deleted IS NULL
  ) <> 4;
