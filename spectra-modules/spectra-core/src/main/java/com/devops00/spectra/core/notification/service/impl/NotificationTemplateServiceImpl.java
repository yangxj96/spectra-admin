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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateQueryFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.core.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.core.notification.service.NotificationTemplateService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 通知模板管理服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateMapper mapper;
    private final TimeMapper timeMapper;

    @Override
    public IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplateQueryFrom query) {
        var queryPage = page.<NotificationTemplateEntity>toPage(Map.of(
                "id", "id",
                "code", "code",
                "name", "name",
                "created_at", "created_at"));
        var wrapper = new LambdaQueryWrapper<NotificationTemplateEntity>().isNull(NotificationTemplateEntity::getDeleted);
        if (query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                wrapper.and(item -> item.like(NotificationTemplateEntity::getCode, query.getKeyword())
                        .or()
                        .like(NotificationTemplateEntity::getName, query.getKeyword()));
            }
            if (query.getChannel() != null) {
                wrapper.eq(NotificationTemplateEntity::getChannel, query.getChannel().name());
            }
            if (query.getEnabled() != null) {
                wrapper.eq(NotificationTemplateEntity::getEnabled, query.getEnabled());
            }
        }
        wrapper.orderByDesc(NotificationTemplateEntity::getCreatedAt);
        var result = mapper.selectPage(queryPage, wrapper);
        var voPage = new Page<NotificationTemplateVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public NotificationTemplateVO detail(UUID id) {
        return toVO(owned(id));
    }

    @Override
    @Transactional
    public UUID created(NotificationTemplateSaveFrom from) {
        assertUnique(from.getCode(), from.getChannel(), null);
        var entity = new NotificationTemplateEntity();
        apply(entity, from);
        entity.setBuiltin(false);
        mapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, NotificationTemplateSaveFrom from) {
        var entity = owned(id);
        assertUnique(from.getCode(), from.getChannel(), id);
        apply(entity, from);
        mapper.updateById(entity);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        var entity = owned(id);
        if (Boolean.TRUE.equals(entity.getBuiltin())) {
            throw new BusinessRuleViolationException("内置模板不能删除");
        }
        mapper.update(null, new LambdaUpdateWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getId, id)
                .isNull(NotificationTemplateEntity::getDeleted)
                .set(NotificationTemplateEntity::getDeleted, Instant.now()));
    }

    @Override
    @Transactional
    public void changeEnabled(UUID id, boolean enabled) {
        var entity = owned(id);
        entity.setEnabled(enabled);
        mapper.updateById(entity);
    }

    @Override
    public NotificationTemplateEntity require(String code, NotificationChannel channel) {
        if (!StringUtils.hasText(code) || channel == null) {
            throw new BusinessRuleViolationException("通知模板编码和渠道不能为空");
        }
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getCode, code)
                .eq(NotificationTemplateEntity::getChannel, channel.name())
                .eq(NotificationTemplateEntity::getEnabled, true)
                .isNull(NotificationTemplateEntity::getDeleted)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new DataNotExistException("通知模板不存在或未启用: " + code);
        }
        return entity;
    }

    /**
     * 将保存参数写入实体。
     *
     * @param entity 目标实体
     * @param from   保存参数
     */
    private void apply(NotificationTemplateEntity entity, NotificationTemplateSaveFrom from) {
        entity.setCode(from.getCode().trim());
        entity.setName(from.getName().trim());
        entity.setChannel(from.getChannel().name());
        entity.setTitleTemplate(from.getTitleTemplate());
        entity.setContentTemplate(from.getContentTemplate());
        entity.setHtmlTemplate(from.getHtmlTemplate());
        entity.setProviderTemplateCode(from.getProviderTemplateCode());
        entity.setEnabled(from.getEnabled() == null || from.getEnabled());
    }

    /**
     * 校验模板编码和渠道唯一。
     *
     * @param code    模板编码
     * @param channel 发送渠道
     * @param exclude 需要排除的模板 ID
     */
    private void assertUnique(String code, NotificationChannel channel, UUID exclude) {
        var wrapper = new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getCode, code.trim())
                .eq(NotificationTemplateEntity::getChannel, channel.name())
                .isNull(NotificationTemplateEntity::getDeleted);
        if (exclude != null) {
            wrapper.ne(NotificationTemplateEntity::getId, exclude);
        }
        if (mapper.selectCount(wrapper) > 0) {
            throw new DataExistException("同一渠道下模板编码已存在");
        }
    }

    /**
     * 查询未删除模板。
     *
     * @param id 模板 ID
     * @return 模板实体
     */
    private NotificationTemplateEntity owned(UUID id) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getId, id)
                .isNull(NotificationTemplateEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("通知模板不存在");
        }
        return entity;
    }

    /**
     * 转换为响应对象。
     *
     * @param entity 模板实体
     * @return 响应对象
     */
    private NotificationTemplateVO toVO(NotificationTemplateEntity entity) {
        var vo = new NotificationTemplateVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setChannel(entity.getChannel());
        vo.setTitleTemplate(entity.getTitleTemplate());
        vo.setContentTemplate(entity.getContentTemplate());
        vo.setHtmlTemplate(entity.getHtmlTemplate());
        vo.setProviderTemplateCode(entity.getProviderTemplateCode());
        vo.setBuiltin(entity.getBuiltin());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(timeMapper.toOffsetDateTime(entity.getCreatedAt()));
        return vo;
    }
}
