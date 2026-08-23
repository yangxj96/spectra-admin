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

package com.devops00.spectra.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.notification.javabean.converter.NotificationTemplateConverter;
import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplatePreviewVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.notification.service.NotificationTemplateService;
import com.devops00.spectra.notification.strategy.NotificationPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 通知模板生命周期服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private static final String DRAFT = "DRAFT";

    private static final String PUBLISHED = "PUBLISHED";

    private static final String DISABLED = "DISABLED";

    private static final String ARCHIVED = "ARCHIVED";

    private static final Set<String> STATES = Set.of(DRAFT, PUBLISHED, DISABLED, ARCHIVED);

    private final NotificationTemplateMapper mapper;

    private final NotificationTemplateConverter converter;

    private final NotificationTemplateRenderer renderer;

    private final NotificationPolicy policy;

    @Override
    public IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplatePageFrom params) {
        var query = new LambdaQueryWrapper<NotificationTemplateEntity>()
                .isNull(NotificationTemplateEntity::getDeleted)
                .orderByDesc(NotificationTemplateEntity::getUpdatedAt)
                .orderByDesc(NotificationTemplateEntity::getVersionNo);
        if (params != null) {
            query.like(StringUtils.hasText(params.getTemplateGroupCode()),
                    NotificationTemplateEntity::getTemplateGroupCode, params.getTemplateGroupCode())
                    .eq(StringUtils.hasText(params.getChannel()), NotificationTemplateEntity::getChannel, params.getChannel())
                    .eq(StringUtils.hasText(params.getPurpose()), NotificationTemplateEntity::getPurpose, params.getPurpose())
                    .eq(StringUtils.hasText(params.getState()), NotificationTemplateEntity::getState, params.getState());
            if (StringUtils.hasText(params.getState()) && !STATES.contains(params.getState())) {
                throw new DataSaveException("模板状态不合法");
            }
        }
        return converter.toPage(mapper.selectPage(page.toPage(), query));
    }

    @Override
    public NotificationTemplateVO detail(UUID id) {
        return converter.toVO(getTemplate(id));
    }

    @Override
    @Transactional
    public NotificationTemplateVO create(NotificationTemplateSaveFrom params) {
        var purpose = validateContent(params);
        var entity = new NotificationTemplateEntity();
        entity.setTemplateGroupCode(normalize(params.getTemplateGroupCode()));
        entity.setTemplateName(normalize(params.getTemplateName()));
        entity.setChannel(params.getChannel().name());
        entity.setPurpose(purpose.name());
        entity.setVersionNo(nextVersionNo(entity.getTemplateGroupCode(), entity.getChannel()));
        entity.setState(DRAFT);
        copyContent(params, entity);
        if (mapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知模板失败");
        }
        log.info("已创建通知模板草稿: templateId={}", entity.getId());
        return converter.toVO(entity);
    }

    @Override
    @Transactional
    public NotificationTemplateVO copy(UUID id) {
        var source = getTemplate(id);
        var draft = copyToDraft(source);
        if (mapper.insert(draft) != 1) {
            throw new DataSaveException("复制通知模板失败");
        }
        log.info("已复制通知模板草稿: sourceId={}, draftId={}", id, draft.getId());
        return converter.toVO(draft);
    }

    @Override
    @Transactional
    public NotificationTemplateVO update(NotificationTemplateSaveFrom params) {
        var entity = getTemplate(params.getId());
        ensureState(entity, DRAFT, "只有草稿模板可以编辑");
        ensureVersion(entity, params.getVersion());
        var purpose = validateContent(params);
        if (!Objects.equals(entity.getTemplateGroupCode(), normalize(params.getTemplateGroupCode()))
                || !Objects.equals(entity.getChannel(), params.getChannel().name())) {
            throw new DataSaveException("草稿的模板组和渠道不可修改，请创建新的模板版本");
        }
        entity.setPurpose(purpose.name());
        entity.setTemplateName(normalize(params.getTemplateName()));
        copyContent(params, entity);
        if (mapper.updateById(entity) != 1) {
            throw new DataSaveException("修改通知模板失败");
        }
        log.info("已修改通知模板草稿: templateId={}", entity.getId());
        return converter.toVO(entity);
    }

    @Override
    @Transactional
    public void publish(UUID id, NotificationTemplateActionFrom params) {
        var entity = getTemplate(id);
        ensureState(entity, DRAFT, "只有草稿模板可以发布");
        ensureVersion(entity, params.getVersion());
        validateDefinition(entity);
        mapper.update(null, new LambdaUpdateWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getTemplateGroupCode, entity.getTemplateGroupCode())
                .eq(NotificationTemplateEntity::getChannel, entity.getChannel())
                .eq(NotificationTemplateEntity::getState, PUBLISHED)
                .isNull(NotificationTemplateEntity::getDeleted)
                .set(NotificationTemplateEntity::getState, DISABLED)
                .set(NotificationTemplateEntity::getUpdatedAt, Instant.now()));
        entity.setState(PUBLISHED);
        entity.setVersionDigest(NotificationTemplateDigest.calculate(entity));
        if (mapper.updateById(entity) != 1) {
            throw new DataSaveException("发布通知模板失败");
        }
        log.info("已发布通知模板: templateId={}", id);
    }

    @Override
    @Transactional
    public void disable(UUID id, NotificationTemplateActionFrom params) {
        var entity = getTemplate(id);
        ensureState(entity, PUBLISHED, "只有已发布模板可以停用");
        ensureVersion(entity, params.getVersion());
        entity.setState(DISABLED);
        if (mapper.updateById(entity) != 1) {
            throw new DataSaveException("停用通知模板失败");
        }
        log.info("已停用通知模板: templateId={}", id);
    }

    @Override
    @Transactional
    public void archive(UUID id, NotificationTemplateActionFrom params) {
        var entity = getTemplate(id);
        if (PUBLISHED.equals(entity.getState())) {
            throw new DataSaveException("已发布模板必须先停用再归档");
        }
        ensureState(entity, Set.of(DRAFT, DISABLED), "当前模板状态不可归档");
        ensureVersion(entity, params.getVersion());
        entity.setState(ARCHIVED);
        if (mapper.updateById(entity) != 1) {
            throw new DataSaveException("归档通知模板失败");
        }
        log.info("已归档通知模板: templateId={}", id);
    }

    @Override
    public List<NotificationTemplateVO> versions(UUID id) {
        var source = getTemplate(id);
        return mapper.selectList(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getTemplateGroupCode, source.getTemplateGroupCode())
                .eq(NotificationTemplateEntity::getChannel, source.getChannel())
                .isNull(NotificationTemplateEntity::getDeleted)
                .orderByDesc(NotificationTemplateEntity::getVersionNo))
                .stream()
                .map(converter::toVO)
                .toList();
    }

    @Override
    @Transactional
    public NotificationTemplateVO rollback(UUID id) {
        var source = getTemplate(id);
        if (DRAFT.equals(source.getState())) {
            throw new DataSaveException("草稿模板不能回滚");
        }
        var draft = copyToDraft(source);
        if (mapper.insert(draft) != 1) {
            throw new DataSaveException("创建回滚草稿失败");
        }
        log.info("已从通知模板历史版本创建回滚草稿: sourceId={}, draftId={}", id, draft.getId());
        return converter.toVO(draft);
    }

    private NotificationTemplateEntity copyToDraft(NotificationTemplateEntity source) {
        var draft = new NotificationTemplateEntity();
        draft.setTemplateGroupCode(source.getTemplateGroupCode());
        draft.setTemplateName(source.getTemplateName());
        draft.setChannel(source.getChannel());
        draft.setPurpose(source.getPurpose());
        draft.setVersionNo(nextVersionNo(source.getTemplateGroupCode(), source.getChannel()));
        draft.setState(DRAFT);
        draft.setTitleTemplate(source.getTitleTemplate());
        draft.setContentTemplate(source.getContentTemplate());
        draft.setHtmlTemplate(source.getHtmlTemplate());
        draft.setParameterSchema(source.getParameterSchema());
        draft.setProviderTemplateCode(source.getProviderTemplateCode());
        draft.setVersionDigest(NotificationTemplateDigest.calculate(draft));
        return draft;
    }

    @Override
    public NotificationTemplatePreviewVO preview(NotificationTemplatePreviewFrom params) {
        var template = params.getTemplateId() == null ? null : getTemplate(params.getTemplateId());
        var groupCode = template == null ? null : template.getTemplateGroupCode();
        var channel = template == null ? params.getChannel() : parseChannel(template.getChannel());
        var purpose = template == null ? normalize(params.getPurpose()) : template.getPurpose();
        var title = template == null ? params.getTitleTemplate() : template.getTitleTemplate();
        var content = template == null ? params.getContentTemplate() : template.getContentTemplate();
        var html = template == null ? params.getHtmlTemplate() : template.getHtmlTemplate();
        var providerTemplateCode = template == null ? null : template.getProviderTemplateCode();
        var schema = template == null ? params.getParameterSchema() : template.getParameterSchema();
        if (!StringUtils.hasText(content)) {
            throw new DataSaveException("正文模板不能为空");
        }
        var purposeValue = policy.parsePurpose(purpose);
        policy.validateTemplateChannel(purposeValue, channel);
        policy.validateTemplateFields(channel, title, html, providerTemplateCode);
        renderer.validateDefinition(schema, title, content, html);
        renderer.validateParameterSecurity(schema, params.getParameters(), params.getSensitiveParameters());
        var parameters = new HashMap<String, Object>();
        if (params.getParameters() != null) {
            parameters.putAll(params.getParameters());
        }
        if (params.getSensitiveParameters() != null) {
            parameters.putAll(params.getSensitiveParameters());
        }
        renderer.validateAll(parameters, title, content, html);

        var result = new NotificationTemplatePreviewVO();
        result.setTemplateId(template == null ? null : template.getId());
        result.setTemplateGroupCode(groupCode);
        result.setChannel(channel.name());
        result.setPurpose(purposeValue.name());
        result.setVersionNo(template == null ? null : template.getVersionNo());
        result.setTitle(renderer.render(title, parameters));
        result.setContent(renderer.render(content, parameters));
        result.setHtml(renderer.render(html, parameters));
        result.setPreviewedAt(Instant.now());
        return result;
    }

    private NotificationPurpose validateContent(NotificationTemplateSaveFrom params) {
        if (params.getChannel() == null) {
            throw new DataSaveException("通知渠道不能为空");
        }
        if (!StringUtils.hasText(params.getTemplateGroupCode())
                || !StringUtils.hasText(params.getTemplateName())
                || !StringUtils.hasText(params.getPurpose())) {
            throw new DataSaveException("模板名称、模板组编码和通知用途不能为空");
        }
        if (!StringUtils.hasText(params.getContentTemplate())) {
            throw new DataSaveException("正文模板不能为空");
        }
        var purpose = policy.parsePurpose(params.getPurpose());
        policy.validateTemplateChannel(purpose, params.getChannel());
        policy.validateTemplateFields(params.getChannel(), params.getTitleTemplate(), params.getHtmlTemplate(),
                params.getProviderTemplateCode());
        renderer.validateDefinition(params.getParameterSchema(), params.getTitleTemplate(), params.getContentTemplate(), params.getHtmlTemplate());
        return purpose;
    }

    private void validateDefinition(NotificationTemplateEntity entity) {
        var channel = parseChannel(entity.getChannel());
        policy.validateTemplateChannel(policy.parsePurpose(entity.getPurpose()), channel);
        policy.validateTemplateFields(channel, entity.getTitleTemplate(), entity.getHtmlTemplate(),
                entity.getProviderTemplateCode());
        renderer.validateDefinition(entity.getParameterSchema(), entity.getTitleTemplate(), entity.getContentTemplate(), entity.getHtmlTemplate());
    }

    private NotificationChannel parseChannel(String value) {
        if (!StringUtils.hasText(value)) {
            throw new DataSaveException("通知渠道不能为空");
        }
        try {
            return NotificationChannel.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("通知渠道不合法");
        }
    }

    private void copyContent(NotificationTemplateSaveFrom params, NotificationTemplateEntity entity) {
        entity.setTitleTemplate(normalizeNullable(params.getTitleTemplate()));
        entity.setContentTemplate(params.getContentTemplate().trim());
        entity.setHtmlTemplate(normalizeNullable(params.getHtmlTemplate()));
        entity.setParameterSchema(params.getParameterSchema() == null ? Map.of() : params.getParameterSchema());
        entity.setProviderTemplateCode(normalizeNullable(params.getProviderTemplateCode()));
        entity.setVersionDigest(NotificationTemplateDigest.calculate(entity));
    }

    private int nextVersionNo(String groupCode, String channel) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getTemplateGroupCode, groupCode)
                .eq(NotificationTemplateEntity::getChannel, channel)
                .isNull(NotificationTemplateEntity::getDeleted))
                .stream()
                .mapToInt(entity -> entity.getVersionNo() == null ? 0 : entity.getVersionNo())
                .max()
                .orElse(0) + 1;
    }

    private NotificationTemplateEntity getTemplate(UUID id) {
        if (id == null) {
            throw new DataNotExistException("通知模板不存在");
        }
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getId, id)
                .isNull(NotificationTemplateEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("通知模板不存在");
        }
        return entity;
    }

    private void ensureVersion(NotificationTemplateEntity entity, Long version) {
        if (!Objects.equals(entity.getVersion(), version)) {
            throw new DataSaveException("通知模板已被其他人修改，请刷新后重试");
        }
    }

    private void ensureState(NotificationTemplateEntity entity, String expected, String message) {
        ensureState(entity, Set.of(expected), message);
    }

    private void ensureState(NotificationTemplateEntity entity, Set<String> expected, String message) {
        if (!expected.contains(entity.getState())) {
            throw new DataSaveException(message);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        var normalized = normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
