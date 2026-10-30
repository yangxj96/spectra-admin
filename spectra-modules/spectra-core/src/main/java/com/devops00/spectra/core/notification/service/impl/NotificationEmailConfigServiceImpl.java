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
import com.devops00.spectra.core.notification.javabean.entity.NotificationEmailConfigEntity;
import com.devops00.spectra.core.notification.javabean.enums.EmailSslMode;
import com.devops00.spectra.core.notification.javabean.from.NotificationEmailConfigSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationEmailConfigVO;
import com.devops00.spectra.core.notification.mapper.NotificationEmailConfigMapper;
import com.devops00.spectra.core.notification.service.NotificationEmailConfigService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 邮件配置管理服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Service
@RequiredArgsConstructor
public class NotificationEmailConfigServiceImpl implements NotificationEmailConfigService {

    private final NotificationEmailConfigMapper mapper;
    private final TimeMapper timeMapper;

    @Override
    public List<NotificationEmailConfigVO> list() {
        return mapper.selectList(new LambdaQueryWrapper<NotificationEmailConfigEntity>()
                .isNull(NotificationEmailConfigEntity::getDeleted)
                .orderByDesc(NotificationEmailConfigEntity::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public NotificationEmailConfigVO detail(UUID id) {
        return toVO(owned(id));
    }

    @Override
    @Transactional
    public UUID created(NotificationEmailConfigSaveFrom from) {
        var entity = new NotificationEmailConfigEntity();
        apply(entity, from);
        entity.setActive(false);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, NotificationEmailConfigSaveFrom from) {
        var entity = owned(id);
        apply(entity, from);
        mapper.updateById(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        owned(id);
        mapper.update(null, new LambdaUpdateWrapper<NotificationEmailConfigEntity>()
                .eq(NotificationEmailConfigEntity::getId, id)
                .isNull(NotificationEmailConfigEntity::getDeleted)
                .set(NotificationEmailConfigEntity::getDeleted, Instant.now())
                .set(NotificationEmailConfigEntity::getActive, false));
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        owned(id);
        deactivateAll();
        mapper.update(null, new LambdaUpdateWrapper<NotificationEmailConfigEntity>()
                .eq(NotificationEmailConfigEntity::getId, id)
                .set(NotificationEmailConfigEntity::getActive, true));
    }

    @Override
    public NotificationEmailConfigEntity active() {
        return mapper.selectOne(new LambdaQueryWrapper<NotificationEmailConfigEntity>()
                .eq(NotificationEmailConfigEntity::getActive, true)
                .isNull(NotificationEmailConfigEntity::getDeleted)
                .last("LIMIT 1"));
    }

    /**
     * 停用全部邮件配置。
     */
    private void deactivateAll() {
        mapper.update(null, new LambdaUpdateWrapper<NotificationEmailConfigEntity>()
                .set(NotificationEmailConfigEntity::getActive, false)
                .isNull(NotificationEmailConfigEntity::getDeleted));
    }

    /**
     * 将保存参数写入实体。
     *
     * @param entity 目标实体
     * @param from   保存参数
     */
    private void apply(NotificationEmailConfigEntity entity, NotificationEmailConfigSaveFrom from) {
        entity.setName(from.getName().trim());
        entity.setProvider(from.getProvider().name());
        entity.setHost(from.getHost());
        entity.setPort(from.getPort());
        entity.setUsername(from.getUsername());
        entity.setPassword(from.getPassword());
        entity.setFromAddress(from.getFromAddress());
        entity.setFromName(from.getFromName());
        entity.setSslMode(from.getSslMode() == null ? EmailSslMode.NONE.name() : from.getSslMode().name());
        entity.setExtra(from.getExtra() == null ? Map.of() : Map.copyOf(from.getExtra()));
    }

    /**
     * 查询未删除配置。
     *
     * @param id 配置 ID
     * @return 配置实体
     */
    private NotificationEmailConfigEntity owned(UUID id) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationEmailConfigEntity>()
                .eq(NotificationEmailConfigEntity::getId, id)
                .isNull(NotificationEmailConfigEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("邮件配置不存在");
        }
        return entity;
    }

    /**
     * 转换为响应对象。
     *
     * @param entity 配置实体
     * @return 响应对象
     */
    private NotificationEmailConfigVO toVO(NotificationEmailConfigEntity entity) {
        var vo = new NotificationEmailConfigVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setProvider(entity.getProvider());
        vo.setHost(entity.getHost());
        vo.setPort(entity.getPort());
        vo.setUsername(entity.getUsername());
        vo.setPassword(entity.getPassword());
        vo.setFromAddress(entity.getFromAddress());
        vo.setFromName(entity.getFromName());
        vo.setSslMode(entity.getSslMode());
        vo.setExtra(entity.getExtra());
        vo.setActive(entity.getActive());
        vo.setCreatedAt(timeMapper.toOffsetDateTime(entity.getCreatedAt()));
        return vo;
    }
}
