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

package com.devops00.spectra.core.security.secret.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.SecuritySecretUnavailableException;
import com.devops00.spectra.common.port.security.RuntimeSecret;
import com.devops00.spectra.common.port.security.SecretValueCipher;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretDefinitionEntity;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretVersionEntity;
import com.devops00.spectra.core.security.secret.javabean.enums.SecretVersionState;
import com.devops00.spectra.core.security.secret.mapper.SecretDefinitionMapper;
import com.devops00.spectra.core.security.secret.mapper.SecretVersionMapper;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 基于密钥定义和版本表的统一密钥服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
public class SecretManagementServiceImpl implements SecretManagementService, SecretRuntimeService {

    private final SecretDefinitionMapper definitionMapper;
    private final SecretVersionMapper versionMapper;
    private final SecretValueCipher cipher;

    public SecretManagementServiceImpl(SecretDefinitionMapper definitionMapper,
                                       SecretVersionMapper versionMapper,
                                       SecretValueCipher cipher) {
        this.definitionMapper = definitionMapper;
        this.versionMapper = versionMapper;
        this.cipher = cipher;
    }

    @Override
    public List<SecretDefinitionEntity> listDefinitions() {
        return definitionMapper.selectList(new QueryWrapper<SecretDefinitionEntity>()
                .isNull("deleted")
                .orderByAsc("category")
                .orderByAsc("code"));
    }

    @Override
    public List<SecretVersionEntity> listVersions(String code) {
        SecretDefinitionEntity definition = requireDefinition(code);
        return versionMapper.selectList(new QueryWrapper<SecretVersionEntity>()
                .eq("secret_definition_id", definition.getId())
                .isNull("deleted")
                .orderByDesc("version_no"));
    }

    @Override
    public List<ActiveSecret> currentActiveSecrets(String category) {
        return listDefinitions().stream()
                .filter(definition -> category == null || category.isBlank() || category.equals(definition.getCategory()))
                .map(definition -> findActive(definition.getCode())
                        .map(secret -> new ActiveSecret(secret.code(), definition.getCategory(), secret.version(),
                                secret.fingerprint(), secret.value())))
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    @Transactional
    public SecretVersionEntity createPending(String code, String plaintext, String source) {
        SecretDefinitionEntity definition = requireDefinition(code);
        if (!StringUtils.hasText(plaintext)) {
            throw new DataSaveException("密钥值不能为空");
        }
        if (!StringUtils.hasText(source)) {
            throw new DataSaveException("密钥版本来源不能为空");
        }
        source = source.trim().toUpperCase();
        if (!List.of("MANUAL", "IMPORT", "MIGRATION", "GENERATED").contains(source)) {
            throw new DataSaveException("不支持的密钥版本来源");
        }
        int nextVersion = Optional.ofNullable(versionMapper.selectOne(new QueryWrapper<SecretVersionEntity>()
                .eq("secret_definition_id", definition.getId())
                .isNull("deleted")
                .orderByDesc("version_no")
                .last("LIMIT 1")))
                .map(SecretVersionEntity::getVersionNo)
                .map(version -> version + 1)
                .orElse(1);
        SecretValueCipher.EncryptedValue encrypted = cipher.encrypt(code, plaintext);
        var version = new SecretVersionEntity();
        version.setSecretDefinitionId(definition.getId());
        version.setVersionNo(nextVersion);
        version.setState(SecretVersionState.PENDING.name());
        version.setCipherAlgorithm(encrypted.algorithm());
        version.setNonce(encrypted.nonce());
        version.setCiphertext(encrypted.ciphertext());
        version.setFingerprint(fingerprint(plaintext));
        version.setSource(source);
        version.setEffectiveAt(null);
        versionMapper.insert(version);
        return version;
    }

    @Override
    @Transactional
    public void publish(UUID versionId) {
        SecretVersionEntity target = requireVersion(versionId);
        if (!SecretVersionState.PENDING.name().equals(target.getState())) {
            throw new DataSaveException("只有待启用版本可以发布");
        }
        int retired = versionMapper.update(null, new UpdateWrapper<SecretVersionEntity>()
                .set("state", SecretVersionState.RETIRED.name())
                .set("retired_at", Instant.now())
                .setSql("version = version + 1")
                .eq("secret_definition_id", target.getSecretDefinitionId())
                .eq("state", SecretVersionState.ACTIVE.name())
                .isNull("deleted"));
        int activated = versionMapper.update(null, new UpdateWrapper<SecretVersionEntity>()
                .set("state", SecretVersionState.ACTIVE.name())
                .set("effective_at", Instant.now())
                .setSql("version = version + 1")
                .eq("id", target.getId())
                .eq("state", SecretVersionState.PENDING.name())
                .isNull("deleted"));
        if (activated != 1) {
            throw new DataSaveException("密钥版本已被其他操作修改，请刷新后重试");
        }
        if (retired > 1) {
            throw new DataSaveException("密钥存在多个启用版本，发布已拒绝");
        }
    }

    @Override
    @Transactional
    public void publish(String code, UUID versionId) {
        SecretVersionEntity target = requireVersion(versionId);
        requireDefinitionMatches(code, target.getSecretDefinitionId());
        publish(versionId);
    }

    @Override
    @Transactional
    public void retire(UUID versionId) {
        SecretVersionEntity target = requireVersion(versionId);
        if (!SecretVersionState.ACTIVE.name().equals(target.getState())) {
            throw new DataSaveException("只有启用版本可以退役");
        }
        int updated = versionMapper.update(null, new UpdateWrapper<SecretVersionEntity>()
                .set("state", SecretVersionState.RETIRED.name())
                .set("retired_at", Instant.now())
                .setSql("version = version + 1")
                .eq("id", versionId)
                .eq("state", SecretVersionState.ACTIVE.name())
                .isNull("deleted"));
        if (updated != 1) {
            throw new DataSaveException("密钥版本已被其他操作修改，请刷新后重试");
        }
    }

    @Override
    @Transactional
    public void retire(String code, UUID versionId) {
        SecretVersionEntity target = requireVersion(versionId);
        requireDefinitionMatches(code, target.getSecretDefinitionId());
        retire(versionId);
    }

    @Override
    public Optional<RuntimeSecret> findActive(String code) {
        SecretDefinitionEntity definition = requireDefinition(code);
        SecretVersionEntity version;
        try {
            version = versionMapper.selectOne(new QueryWrapper<SecretVersionEntity>()
                    .eq("secret_definition_id", definition.getId())
                    .eq("state", SecretVersionState.ACTIVE.name())
                    .isNull("deleted")
                    .orderByDesc("version_no")
                    .last("LIMIT 1"));
        } catch (RuntimeException exception) {
            throw unavailable(code, exception);
        }
        if (version == null) {
            return Optional.empty();
        }
        try {
            String value = cipher.decrypt(code, new SecretValueCipher.EncryptedValue(
                    version.getCipherAlgorithm(), version.getNonce(), version.getCiphertext()));
            return Optional.of(new RuntimeSecret(code, version.getVersionNo(), value, version.getFingerprint()));
        } catch (RuntimeException exception) {
            throw unavailable(code, exception);
        }
    }

    /**
     * 校验密钥。
     */
    private SecretDefinitionEntity requireDefinition(String code) {
        if (!StringUtils.hasText(code)) {
            throw new DataSaveException("密钥编码不能为空");
        }
        SecretDefinitionEntity definition = definitionMapper.selectOne(new QueryWrapper<SecretDefinitionEntity>()
                .eq("code", code)
                .isNull("deleted"));
        if (definition == null) {
            throw new DataNotExistException("未注册的密钥编码");
        }
        return definition;
    }

    /**
     * 校验版本。
     */
    private SecretVersionEntity requireVersion(UUID versionId) {
        if (versionId == null) {
            throw new DataSaveException("密钥版本ID不能为空");
        }
        SecretVersionEntity version = versionMapper.selectById(versionId);
        if (version == null || version.getDeleted() != null) {
            throw new DataNotExistException("密钥版本不存在");
        }
        return version;
    }

    /**
     * 校验密钥。
     */
    private void requireDefinitionMatches(String code, UUID definitionId) {
        SecretDefinitionEntity definition = requireDefinition(code);
        if (!definition.getId().equals(definitionId)) {
            throw new DataSaveException("密钥版本与路径编码不匹配");
        }
    }

    /**
     * 处理密钥相关数据。
     */
    private SecuritySecretUnavailableException unavailable(String code, Throwable cause) {
        return new SecuritySecretUnavailableException("密钥运行态不可用: " + code, cause);
    }

    /**
     * 处理密钥相关数据。
     */
    private static String fingerprint(String plaintext) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持SHA-256", exception);
        }
    }
}
