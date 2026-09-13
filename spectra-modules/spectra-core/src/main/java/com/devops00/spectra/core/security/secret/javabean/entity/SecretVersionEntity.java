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

package com.devops00.spectra.core.security.secret.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * 密钥的加密版本记录及其版本元数据。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Getter
@Setter
@ToString(exclude = {"nonce", "ciphertext"})
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_secret_version", schema = "spectra_security")
public class SecretVersionEntity extends BaseEntity {

    /** 所属密钥定义。 */
    @TableField("secret_definition_id")
    private UUID secretDefinitionId;
    /** 业务版本号。 */
    @TableField("version_no")
    private Integer versionNo;
    /** 生命周期状态。 */
    @TableField("state")
    private String state;
    /** 密文算法。 */
    @TableField("cipher_algorithm")
    private String cipherAlgorithm;
    /** 加密随机数。 */
    @TableField("nonce")
    private byte[] nonce;
    /** 根密钥保护后的密文。 */
    @TableField("ciphertext")
    private byte[] ciphertext;
    /** 明文 SHA-256 摘要。 */
    @TableField("fingerprint")
    private String fingerprint;
    /** 版本来源。 */
    @TableField("source")
    private String source;
    /** 生效时间。 */
    @TableField("effective_at")
    private Instant effectiveAt;
    /** 退役时间。 */
    @TableField("retired_at")
    private Instant retiredAt;
}
