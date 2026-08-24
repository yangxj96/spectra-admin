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

package com.devops00.spectra.core.security.initialization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devops00.spectra.common.constant.ConfiguredValueType;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.core.security.authorization.service.SystemGuideAuthorization;
import com.devops00.spectra.core.security.initialization.constant.SystemStateKeys;
import com.devops00.spectra.core.security.initialization.javabean.entity.SystemState;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemGuideCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemGuideStatusVO;
import com.devops00.spectra.core.security.initialization.mapper.SystemStateMapper;
import com.devops00.spectra.core.security.initialization.service.SystemGuideService;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/** DEV_OPS 系统设置引导默认实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemGuideServiceImpl implements SystemGuideService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SystemStateMapper stateMapper;
    private final SystemGuideAuthorization guideAuthorization;
    private final ConfiguredService configuredService;
    private final CryptoKeyManager cryptoKeyManager;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;
    private final UserDepartmentMembershipMapper userDepartmentMembershipMapper;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public SystemGuideStatusVO status() {
        SystemState state = loadState(false);
        boolean required = guideAuthorization.isDevOps() && !SystemStateKeys.COMPLETED.equals(state.getState());
        return new SystemGuideStatusVO(state.getState(), SystemStateKeys.COMPLETED.equals(state.getState()), required);
    }

    @Override
    @Transactional
    public void complete(SystemGuideCompleteFrom from) {
        guideAuthorization.assertDevOps();
        SystemState systemState = loadStateByKey(SystemStateKeys.SYSTEM, false);
        if (!SystemStateKeys.INITIALIZED.equals(systemState.getState())) {
            throw new IllegalStateException("系统尚未完成首次初始化");
        }

        SystemState guideState = loadState(true);
        if (SystemStateKeys.COMPLETED.equals(guideState.getState())) {
            return;
        }

        Department rootDepartment = createRootDepartment(from.getRootDepartmentName(), from.getRootDepartmentRegionId(),
                from.getRootDepartmentType(), requireCurrentUserId());
        applyCrypto(Boolean.TRUE.equals(from.getCryptoEnabled()));
        applyNotification(Boolean.TRUE.equals(from.getNotificationEnabled()));
        applyCopyright(Boolean.TRUE.equals(from.getCopyrightEnabled()), from.getCopyrightName(), from.getCopyrightUrl());
        guideState.setState(SystemStateKeys.COMPLETED);
        if (stateMapper.updateById(guideState) != 1) {
            throw new DataSaveException("保存系统引导状态失败");
        }
        log.info("系统设置引导完成，根部门={}, 接口加解密={}, 通知模块={}", rootDepartment.getName(),
                from.getCryptoEnabled(), from.getNotificationEnabled());
    }

    /**
     * 更新或推进目标状态（{@code applyCopyright}）。
     */
    private void applyCopyright(boolean enabled, String name, String url) {
        String copyrightName = name == null ? "" : name.trim();
        String copyrightUrl = url == null ? "" : url.trim();
        if (enabled) {
            if (copyrightName.isBlank()) {
                throw new DataSaveException("底部版权名称不能为空");
            }
            if (!isHttpUrl(copyrightUrl)) {
                throw new DataSaveException("底部版权跳转地址必须是有效的 HTTP/HTTPS 地址");
            }
        }
        configuredService.upsert(SystemConfigKeys.COPYRIGHT_ENABLED, Boolean.toString(enabled), ConfiguredValueType.BOOL,
                "系统设置引导中配置的底部版权开关");
        configuredService.upsert(SystemConfigKeys.COPYRIGHT_NAME, copyrightName, ConfiguredValueType.TEXT,
                "系统底部版权名称");
        configuredService.upsert(SystemConfigKeys.COPYRIGHT_URL, copyrightUrl, ConfiguredValueType.TEXT,
                "系统底部版权点击跳转地址");
    }

    /**
     * 判断条件是否满足（{@code isHttpUrl}）。
     */
    private boolean isHttpUrl(String value) {
        if (value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * 创建或构建目标数据（{@code createRootDepartment}）。
     */
    private Department createRootDepartment(String name, UUID regionId, Short type, UUID userId) {
        String departmentName = name == null ? "" : name.trim();
        if (departmentName.isBlank()) {
            throw new DataSaveException("根部门名称不能为空");
        }
        if (regionId == null) {
            throw new DataSaveException("根部门所属区域不能为空");
        }
        if (type == null) {
            throw new DataSaveException("根部门类型不能为空");
        }
        Department rootDepartment = new Department();
        rootDepartment.setName(departmentName);
        rootDepartment.setCode(IdWorker.get32UUID().toUpperCase());
        rootDepartment.setType(type.toString());
        rootDepartment.setRegionId(regionId);
        rootDepartment.setPath(departmentName);
        rootDepartment.setSort(0);
        rootDepartment.setCreatedBy(userId);
        rootDepartment.setUpdatedBy(userId);
        if (departmentMapper.insert(rootDepartment) != 1) {
            throw new DataSaveException("创建初始化根部门失败");
        }
        departmentMapper.clearClosure();
        if (departmentMapper.rebuildClosure() < 1) {
            throw new DataSaveException("创建初始化根部门层级关系失败");
        }

        var userUpdate = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getDepartmentId, rootDepartment.getId());
        if (userMapper.update(null, userUpdate) != 1) {
            throw new DataSaveException("关联 DEV_OPS 根部门失败");
        }
        if (userDepartmentMembershipMapper.insertPrimary(userId, rootDepartment.getId()) != 1) {
            throw new DataSaveException("建立 DEV_OPS 根部门关系失败");
        }
        return rootDepartment;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireCurrentUserId}）。
     */
    private UUID requireCurrentUserId() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new IllegalStateException("无法识别当前 DEV_OPS 用户");
        }
        return userId;
    }

    /**
     * 更新或推进目标状态（{@code applyCrypto}）。
     */
    private void applyCrypto(boolean enabled) {
        if (!enabled) {
            configuredService.upsert(SystemConfigKeys.CRYPTO_ENABLED, "false", ConfiguredValueType.BOOL,
                    "系统设置引导中配置的接口加解密开关");
            cryptoKeyManager.refresh();
            return;
        }

        if (!cryptoKeyManager.isEnabled()) {
            saveKeyPair(generateRsaKeyPair(), generateRsaKeyPair());
        }
        configuredService.upsert(SystemConfigKeys.CRYPTO_ENABLED, "true", ConfiguredValueType.BOOL,
                "系统设置引导中配置的接口加解密开关");
        cryptoKeyManager.refresh();
        if (!cryptoKeyManager.isEnabled()) {
            throw new DataSaveException("接口加解密密钥未能正确加载");
        }
    }

    /**
     * 更新或推进目标状态（{@code applyNotification}）。
     */
    private void applyNotification(boolean enabled) {
        configuredService.upsert(SystemConfigKeys.NOTIFICATION_ENABLED, Boolean.toString(enabled),
                ConfiguredValueType.BOOL, "系统设置引导中配置的通知模块开关");
        configuredService.upsert(SystemConfigKeys.NOTIFICATION_CLEANUP_ENABLED, Boolean.toString(enabled),
                ConfiguredValueType.BOOL, "通知模块启用时同步启用敏感载荷清理");
        if (!enabled) {
            return;
        }
        ensureAesKey(SystemConfigKeys.NOTIFICATION_ADDRESS_ENCRYPTION_KEY, "通知外部地址 AES-GCM 密钥");
        ensureAesKey(SystemConfigKeys.NOTIFICATION_SENSITIVE_PAYLOAD_KEY, "通知敏感载荷 AES-GCM 密钥");
        configuredService.upsert(SystemConfigKeys.NOTIFICATION_ALLOWED_LINK_PREFIXES,
                "/login,/security/authentication/,/oa/,/workflow/,/notification/,/notification-center/,/system/,/file/,/ai/",
                ConfiguredValueType.TEXT, "消息中心允许的站内路由前缀");
    }

    /**
     * 处理内部业务逻辑（{@code ensureAesKey}）。
     */
    private void ensureAesKey(String key, String remarks) {
        var existing = configuredService.findValue(key).orElse(null);
        if (isValidAesKey(existing)) {
            return;
        }
        var bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        configuredService.upsert(key, Base64.getEncoder().encodeToString(bytes), ConfiguredValueType.TEXT, remarks);
    }

    /**
     * 判断条件是否满足（{@code isValidAesKey}）。
     */
    private boolean isValidAesKey(String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            return false;
        }
        try {
            var length = Base64.getDecoder().decode(encodedKey).length;
            return length == 16 || length == 24 || length == 32;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * 更新或推进目标状态（{@code saveKeyPair}）。
     */
    private void saveKeyPair(KeyPair serverPair, KeyPair clientPair) {
        String remarks = "系统设置引导自动生成 RSA 密钥对";
        configuredService.upsert("crypto.server.public-key", RSAUtils.getPublicKeyBase64(serverPair.getPublic()),
                ConfiguredValueType.TEXT, remarks);
        configuredService.upsert("crypto.server.private-key", RSAUtils.getPrivateKeyBase64(serverPair.getPrivate()),
                ConfiguredValueType.TEXT, remarks);
        configuredService.upsert("crypto.client.public-key", RSAUtils.getPublicKeyBase64(clientPair.getPublic()),
                ConfiguredValueType.TEXT, remarks);
        configuredService.upsert("crypto.client.private-key", RSAUtils.getPrivateKeyBase64(clientPair.getPrivate()),
                ConfiguredValueType.TEXT, remarks);
    }

    /**
     * 创建或构建目标数据（{@code generateRsaKeyPair}）。
     */
    private KeyPair generateRsaKeyPair() {
        try {
            return RSAUtils.generateKeyPair();
        } catch (Exception exception) {
            throw new DataSaveException("生成接口加解密密钥失败", exception);
        }
    }

    /**
     * 查询或获取目标数据（{@code loadState}）。
     */
    private SystemState loadState(boolean lock) {
        return loadStateByKey(SystemStateKeys.SYSTEM_GUIDE, lock);
    }

    /**
     * 查询或获取目标数据（{@code loadStateByKey}）。
     */
    private SystemState loadStateByKey(String stateKey, boolean lock) {
        SystemState state = lock
                ? stateMapper.selectForUpdateByStateKey(stateKey)
                : stateMapper.selectOne(new LambdaQueryWrapper<SystemState>().eq(SystemState::getStateKey, stateKey));
        if (state == null) {
            throw new IllegalStateException("系统状态不存在: " + stateKey);
        }
        return state;
    }
}
