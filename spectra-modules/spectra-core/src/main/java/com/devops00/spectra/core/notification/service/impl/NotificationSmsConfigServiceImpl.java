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

package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.notification.javabean.entity.NotificationSmsConfigEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationSmsConfigSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSmsConfigVO;
import com.devops00.spectra.core.notification.mapper.NotificationSmsConfigMapper;
import com.devops00.spectra.core.notification.service.NotificationSmsConfigService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 短信配置管理服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Service
@RequiredArgsConstructor
public class NotificationSmsConfigServiceImpl implements NotificationSmsConfigService {

    private final NotificationSmsConfigMapper mapper;
    private final TimeMapper timeMapper;

    @Override
    public List<NotificationSmsConfigVO> list() {
        return mapper.selectList(new LambdaQueryWrapper<NotificationSmsConfigEntity>()
                .isNull(NotificationSmsConfigEntity::getDeleted)
                .orderByDesc(NotificationSmsConfigEntity::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public NotificationSmsConfigVO detail(UUID id) {
        return toVO(owned(id));
    }

    @Override
    @Transactional
    public UUID created(NotificationSmsConfigSaveFrom from) {
        var entity = new NotificationSmsConfigEntity();
        apply(entity, from);
        entity.setActive(false);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, NotificationSmsConfigSaveFrom from) {
        var entity = owned(id);
        apply(entity, from);
        mapper.updateById(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        owned(id);
        mapper.update(null, new LambdaUpdateWrapper<NotificationSmsConfigEntity>()
                .eq(NotificationSmsConfigEntity::getId, id)
                .isNull(NotificationSmsConfigEntity::getDeleted)
                .set(NotificationSmsConfigEntity::getDeleted, Instant.now())
                .set(NotificationSmsConfigEntity::getActive, false));
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        owned(id);
        deactivateAll();
        mapper.update(null, new LambdaUpdateWrapper<NotificationSmsConfigEntity>()
                .eq(NotificationSmsConfigEntity::getId, id)
                .set(NotificationSmsConfigEntity::getActive, true));
    }

    @Override
    public NotificationSmsConfigEntity active() {
        return mapper.selectOne(new LambdaQueryWrapper<NotificationSmsConfigEntity>()
                .eq(NotificationSmsConfigEntity::getActive, true)
                .isNull(NotificationSmsConfigEntity::getDeleted)
                .last("LIMIT 1"));
    }

    /**
     * 停用全部短信配置。
     */
    private void deactivateAll() {
        mapper.update(null, new LambdaUpdateWrapper<NotificationSmsConfigEntity>()
                .set(NotificationSmsConfigEntity::getActive, false)
                .isNull(NotificationSmsConfigEntity::getDeleted));
    }

    /**
     * 将保存参数写入实体。
     *
     * @param entity 目标实体
     * @param from   保存参数
     */
    private void apply(NotificationSmsConfigEntity entity, NotificationSmsConfigSaveFrom from) {
        entity.setName(from.getName().trim());
        entity.setProvider(from.getProvider().name());
        entity.setAccessKeyId(from.getAccessKeyId());
        entity.setAccessKeySecret(from.getAccessKeySecret());
        entity.setSignName(from.getSignName());
        entity.setProviderTemplateCode(from.getProviderTemplateCode());
        entity.setRegion(from.getRegion());
        entity.setExtra(from.getExtra() == null ? Map.of() : Map.copyOf(from.getExtra()));
    }

    /**
     * 查询未删除配置。
     *
     * @param id 配置 ID
     * @return 配置实体
     */
    private NotificationSmsConfigEntity owned(UUID id) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationSmsConfigEntity>()
                .eq(NotificationSmsConfigEntity::getId, id)
                .isNull(NotificationSmsConfigEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("短信配置不存在");
        }
        return entity;
    }

    /**
     * 转换为响应对象。
     *
     * @param entity 配置实体
     * @return 响应对象
     */
    private NotificationSmsConfigVO toVO(NotificationSmsConfigEntity entity) {
        var vo = new NotificationSmsConfigVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setProvider(entity.getProvider());
        vo.setAccessKeyId(entity.getAccessKeyId());
        vo.setAccessKeySecret(entity.getAccessKeySecret());
        vo.setSignName(entity.getSignName());
        vo.setProviderTemplateCode(entity.getProviderTemplateCode());
        vo.setRegion(entity.getRegion());
        vo.setExtra(entity.getExtra());
        vo.setActive(entity.getActive());
        vo.setCreatedAt(timeMapper.toOffsetDateTime(entity.getCreatedAt()));
        return vo;
    }
}
