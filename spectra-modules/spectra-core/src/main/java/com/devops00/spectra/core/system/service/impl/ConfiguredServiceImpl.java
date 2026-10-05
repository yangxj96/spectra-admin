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

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.framework.persistence.base.BaseServiceImpl;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.foundation.lang.StrUtils;
import com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider;
import com.devops00.spectra.core.system.javabean.converter.ConfiguredConverter;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchItemFrom;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredCategory;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;
import com.devops00.spectra.core.system.mapper.ConfiguredMapper;
import com.devops00.spectra.core.system.service.ConfiguredService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统配置Service层默认实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/6 00:00
 */
@Slf4j
@Service
public class ConfiguredServiceImpl extends BaseServiceImpl<ConfiguredMapper, Configured> implements ConfiguredService {

    private final ConfiguredConverter configuredConverter;

    private final PasswordEncoder passwordEncoder;

    private final SecurityPasswordPolicyProvider securityPasswordPolicyProvider;

    public ConfiguredServiceImpl(ConfiguredConverter configuredConverter,
                                 PasswordEncoder passwordEncoder,
                                 SecurityPasswordPolicyProvider securityPasswordPolicyProvider) {
        this.configuredConverter = configuredConverter;
        this.passwordEncoder = passwordEncoder;
        this.securityPasswordPolicyProvider = securityPasswordPolicyProvider;
    }

    @Override
    public List<ConfiguredVO> settings() {
        var wrapper = new LambdaQueryWrapper<Configured>().orderByAsc(Configured::getKey);
        return configuredConverter.toVOList(this.list(wrapper));
    }

    @Override
    @Transactional
    public void modifyBatch(ConfiguredBatchFrom params) {
        validateBatch(params);
        var settings = loadBatchSettings(params);
        saveBatchSettings(params, settings);
    }

    private void validateBatch(ConfiguredBatchFrom params) {
        if (params == null || params.getCategory() == null || params.getItems() == null || params.getItems().isEmpty()) {
            throw new DataException("系统配置表单不能为空");
        }
    }

    private List<Configured> loadBatchSettings(ConfiguredBatchFrom params) {
        var ids = new HashSet<java.util.UUID>();
        var settings = new ArrayList<Configured>();
        for (ConfiguredBatchItemFrom item : params.getItems()) {
            validateBatchItem(item, ids);
            var setting = this.getById(item.getId());
            validateSetting(setting, params.getCategory());
            validateSecret(item, setting);
            settings.add(setting);
        }
        return settings;
    }

    private void validateBatchItem(ConfiguredBatchItemFrom item, Set<java.util.UUID> ids) {
        if (item == null || item.getId() == null || item.getValue() == null || !ids.add(item.getId())) {
            throw new DataException("系统配置表单项无效或重复");
        }
    }

    private void validateSetting(Configured setting, ConfiguredCategory category) {
        if (setting == null) {
            throw new DataNotExistException("系统配置不存在");
        }
        if (ConfiguredCategory.fromKey(setting.getKey()) != category || ConfiguredCategory.isSystemManagedKey(setting.getKey())) {
            throw new DataException("配置项不属于当前分类或不允许修改");
        }
    }

    private void validateSecret(ConfiguredBatchItemFrom item, Configured setting) {
        if (setting.getType() != ConfiguredValueType.SECRET || StrUtils.isBlank(item.getValue())) {
            return;
        }
        try {
            securityPasswordPolicyProvider.current().assertAccepts(item.getValue());
        } catch (IllegalArgumentException exception) {
            throw new DataException("秘密配置不符合当前密码策略", exception);
        }
    }

    private void saveBatchSettings(ConfiguredBatchFrom params, List<Configured> settings) {
        for (int index = 0; index < settings.size(); index++) {
            var setting = settings.get(index);
            var item = params.getItems().get(index);
            if (setting.getType() != ConfiguredValueType.SECRET || StrUtils.isNotBlank(item.getValue())) {
                setting.setValue(setting.getType() == ConfiguredValueType.SECRET
                        ? passwordEncoder.encode(item.getValue()) : item.getValue());
            }
            setting.setRemarks(item.getRemarks());
            if (!this.updateById(setting)) {
                throw new DataException("系统配置保存失败");
            }
        }
    }

    @Override
    @Transactional
    public void upsert(String key, String value, ConfiguredValueType type, String remarks) {
        upsertInternal(new UpsertRequest(key, value, type, null, remarks, false));
    }

    @Override
    @Transactional
    public void upsertWithDictCode(String key, String value, ConfiguredValueType type, String dictCode, String remarks) {
        if (type != ConfiguredValueType.SELECT || StrUtils.isBlank(dictCode)) {
            throw new DataException("SELECT 配置必须指定字典编码");
        }
        upsertInternal(new UpsertRequest(key, value, type, dictCode, remarks, true));
    }

    private void upsertInternal(UpsertRequest request) {
        var existing = this.getOne(new LambdaQueryWrapper<Configured>().eq(Configured::getKey, request.key()));
        if (existing != null) {
            existing.setValue(request.value());
            existing.setType(request.type());
            existing.setRemarks(request.remarks());
            if (request.updateDictCode()) {
                existing.setDictCode(request.dictCode());
            }
            this.updateById(existing);
        } else {
            var entity = new Configured();
            entity.setKey(request.key());
            entity.setValue(request.value());
            entity.setType(request.type());
            if (request.updateDictCode()) {
                entity.setDictCode(request.dictCode());
            }
            entity.setRemarks(request.remarks());
            this.save(entity);
        }
    }

    private record UpsertRequest(String key, String value, ConfiguredValueType type, String dictCode,
                                 String remarks, boolean updateDictCode) {
    }

    @Override
    @Transactional
    public void ensureExists(String key, String value, ConfiguredValueType type, String remarks) {
        var existing = this.getOne(new LambdaQueryWrapper<Configured>().eq(Configured::getKey, key));
        if (existing != null) {
            return;
        }
        var entity = new Configured();
        entity.setKey(key);
        entity.setValue(value);
        entity.setType(type);
        entity.setRemarks(remarks);
        if (!this.save(entity)) {
            throw new DataException("初始化系统配置失败");
        }
    }

    @Override
    public Optional<String> findValue(String key) {
        if (StrUtils.isBlank(key)) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.getOne(new LambdaQueryWrapper<Configured>()
                .eq(Configured::getKey, key)))
                .map(Configured::getValue)
                .filter(StrUtils::isNotBlank)
                .map(String::trim);
    }
}
