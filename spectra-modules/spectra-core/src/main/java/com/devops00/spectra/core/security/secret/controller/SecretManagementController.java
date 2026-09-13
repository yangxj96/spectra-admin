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

package com.devops00.spectra.core.security.secret.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretDefinitionEntity;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretVersionEntity;
import com.devops00.spectra.core.security.secret.javabean.from.SecretCryptoEnabledFrom;
import com.devops00.spectra.core.security.secret.javabean.from.SecretExportFrom;
import com.devops00.spectra.core.security.secret.javabean.from.SecretPublishFrom;
import com.devops00.spectra.core.security.secret.javabean.from.SecretVersionCreateFrom;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretDefinitionVO;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretExportVO;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretImportPreviewVO;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretManagementSettingsVO;
import com.devops00.spectra.core.security.secret.javabean.vo.SecretVersionVO;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretManagementSettingsService;
import com.devops00.spectra.core.security.secret.service.SecretTransferService;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 密钥管理接口；所有页面和接口操作都只允许 ROLE_DEV_OPS。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/security/secrets")
public class SecretManagementController {

    private final SecretManagementService managementService;
    private final SecretManagementSettingsService settingsService;
    private final SecretTransferService transferService;
    private final CryptoKeyManager cryptoKeyManager;

    /** 查询注册定义及当前版本脱敏状态。 */
    @Audit(value = "'查询密钥定义'", captureArguments = false, captureResult = false)
    @GetMapping(value = "/definitions", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public List<SecretDefinitionVO> definitions() {
        return managementService.listDefinitions().stream().map(this::toDefinition).toList();
    }

    /** 查询密钥管理页面的运行策略状态。 */
    @Audit(value = "'查询密钥管理设置'", captureArguments = false, captureResult = false)
    @Encrypt(response = false)
    @GetMapping(value = "/settings", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecretManagementSettingsVO settings() {
        return toSettings(settingsService.getCryptoSettings());
    }

    /** 修改接口加解密开关；开关状态保存到系统配置表。 */
    @Audit(value = "'修改接口加解密开关'", captureArguments = false, captureResult = false)
    @Encrypt(response = false)
    @PostMapping(value = "/settings/crypto", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecretManagementSettingsVO setCryptoEnabled(@Valid @RequestBody SecretCryptoEnabledFrom from) {
        settingsService.setCryptoEnabled(Boolean.TRUE.equals(from.getEnabled()));
        return toSettings(settingsService.getCryptoSettings());
    }

    /** 查询指定密钥版本元数据，不返回密文和明文。 */
    @Audit(value = "'查询密钥版本'", captureArguments = false, captureResult = false)
    @GetMapping(value = "/{code}/versions", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public List<SecretVersionVO> versions(@PathVariable @NotBlank(message = "密钥编码不能为空") String code) {
        return managementService.listVersions(code).stream().map(this::toVersion).toList();
    }

    /** 创建待启用版本。 */
    @Audit(value = "'创建密钥待启用版本'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/{code}/versions", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecretVersionVO createVersion(@PathVariable @NotBlank(message = "密钥编码不能为空") String code,
                                         @Valid @RequestBody SecretVersionCreateFrom from) {
        return toVersion(managementService.createPending(code, from.getValue(), from.getSource()));
    }

    /** 发布待启用版本。 */
    @Audit(value = "'发布密钥版本'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/{code}/versions/{versionId}/publish", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void publish(@PathVariable @NotBlank(message = "密钥编码不能为空") String code,
                        @PathVariable java.util.UUID versionId,
                        @Valid @RequestBody SecretPublishFrom from) {
        managementService.publish(code, versionId);
    }

    /** 退役当前版本，保留版本审计元数据。 */
    @Audit(value = "'退役密钥版本'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/{code}/versions/{versionId}/retire", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void retire(@PathVariable @NotBlank(message = "密钥编码不能为空") String code,
                       @PathVariable java.util.UUID versionId) {
        managementService.retire(code, versionId);
    }

    /** 导出当前启用版本；服务器生成的口令只在本次响应返回，不落库。 */
    @Audit(value = "'导出当前启用密钥'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/export", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecretExportVO export(@Valid @RequestBody(required = false) SecretExportFrom from) {
        return transferService.exportCurrent(from == null ? null : from.getCategory());
    }

    /** 预览导入包，只解密校验，不写数据库。 */
    @Audit(value = "'预览密钥导入包'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecretImportPreviewVO preview(@RequestPart(name = "file") MultipartFile file,
                                         @RequestPart(name = "passphrase") @NotBlank(message = "导入口令不能为空") String passphrase)
            throws java.io.IOException {
        validateImportFile(file);
        return transferService.preview(file.getBytes(), passphrase);
    }

    /** 写入导入包；不覆盖原版本，所有导入版本保持待启用。 */
    @Audit(value = "'导入密钥待启用版本'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public int importSecrets(@RequestPart(name = "file") MultipartFile file,
                             @RequestPart(name = "passphrase") @NotBlank(message = "导入口令不能为空") String passphrase)
            throws java.io.IOException {
        validateImportFile(file);
        return transferService.importPending(file.getBytes(), passphrase);
    }

    /** 刷新支持热加载的本地运行态。 */
    @Audit(value = "'刷新密钥运行态'", captureArguments = false, captureResult = false)
    @PostMapping(value = "/refresh", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void refresh() {
        cryptoKeyManager.refresh();
    }

    /**
     * 转换密钥。
     */
    private SecretDefinitionVO toDefinition(SecretDefinitionEntity definition) {
        List<SecretVersionEntity> versions = managementService.listVersions(definition.getCode());
        SecretVersionEntity active = versions.stream()
                .filter(version -> "ACTIVE".equals(version.getState()))
                .findFirst()
                .orElse(null);
        long pending = versions.stream().filter(version -> "PENDING".equals(version.getState())).count();
        return new SecretDefinitionVO(definition.getCode(), definition.getName(), definition.getCategory(),
                definition.getValueType(), definition.getOwnerModule(), definition.getDescription(),
                Boolean.TRUE.equals(definition.getMutable()), Boolean.TRUE.equals(definition.getHotReload()),
                Boolean.TRUE.equals(definition.getExportable()), active == null ? null : active.getVersionNo(), pending,
                active == null ? null : active.getFingerprint(), definition.getUpdatedAt());
    }

    /**
     * 转换版本。
     */
    private SecretVersionVO toVersion(SecretVersionEntity version) {
        return new SecretVersionVO(version.getId(), version.getVersionNo(), version.getState(),
                version.getCipherAlgorithm(), version.getFingerprint(), version.getSource(), version.getEffectiveAt(),
                version.getRetiredAt(), version.getCreatedAt());
    }

    /**
     * 转换配置项。
     */
    private SecretManagementSettingsVO toSettings(SecretManagementSettingsService.CryptoSettings settings) {
        return new SecretManagementSettingsVO(settings.enabled(), settings.ready(), settings.state().name());
    }

    /**
     * 校验文件。
     */
    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("密钥导入包不能为空");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("密钥导入包不能超过2MB");
        }
    }
}
