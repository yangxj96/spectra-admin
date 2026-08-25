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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.javabean.converter.NotificationTemplateConverter;
import com.devops00.spectra.notification.javabean.domain.NotificationTemplateState;
import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplateChannelGroupVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplateGroupVO;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    private static final String DRAFT = NotificationTemplateState.DRAFT.name();

    private static final String PUBLISHED = NotificationTemplateState.PUBLISHED.name();

    private static final String DISABLED = NotificationTemplateState.DISABLED.name();

    private static final String ARCHIVED = NotificationTemplateState.ARCHIVED.name();

    private static final Set<String> STATES = Set.of(DRAFT, PUBLISHED, DISABLED, ARCHIVED);

    private final NotificationTemplateMapper mapper;

    private final NotificationTemplateConverter converter;

    private final NotificationTemplateRenderer renderer;

    private final NotificationPolicy policy;

    private final TimeMapper timeMapper;

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
        var latestByTemplate = new LinkedHashMap<String, NotificationTemplateEntity>();
        mapper.selectList(query).forEach(entity -> {
            var key = templateKey(entity);
            var current = latestByTemplate.get(key);
            if (current == null || isPreferred(entity, current)) {
                latestByTemplate.put(key, entity);
            }
        });
        var latestTemplates = List.copyOf(latestByTemplate.values());
        var pageSize = page.getPageSize();
        var pageNum = page.getPageNum();
        var offset = Math.max(0L, (pageNum - 1) * pageSize);
        var fromIndex = Math.toIntExact(Math.min(offset, latestTemplates.size()));
        var toIndex = Math.toIntExact(Math.min(offset + pageSize, latestTemplates.size()));
        var result = new Page<NotificationTemplateEntity>(pageNum, pageSize, latestTemplates.size());
        result.setRecords(latestTemplates.subList(fromIndex, toIndex));
        return converter.toPage(result);
    }

    @Override
    public IPage<NotificationTemplateGroupVO> groupPage(PageFrom page, NotificationTemplatePageFrom params) {
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
            validateState(params.getState());
        }
        var groups = new LinkedHashMap<String, NotificationTemplateGroupVO>();
        mapper.selectList(query).forEach(entity -> appendGroupVersion(groups, entity));
        var records = new ArrayList<>(groups.values());
        records.sort(Comparator.comparing(NotificationTemplateGroupVO::getUpdatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        var pageSize = page.getPageSize() == null ? 15L : Math.max(1L, page.getPageSize());
        var pageNum = page.getPageNum() == null ? 1L : Math.max(1L, page.getPageNum());
        var offset = Math.max(0L, (pageNum - 1) * pageSize);
        var fromIndex = Math.toIntExact(Math.min(offset, records.size()));
        var toIndex = Math.toIntExact(Math.min(offset + pageSize, records.size()));
        var result = new Page<NotificationTemplateGroupVO>(pageNum, pageSize, records.size());
        result.setRecords(records.subList(fromIndex, toIndex));
        return result;
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
        if (findDraft(entity.getTemplateGroupCode(), entity.getChannel()) != null) {
            throw new DataSaveException("该模板渠道已有草稿，请继续编辑现有草稿");
        }
        entity.setPurpose(purpose.name());
        entity.setVersionNo(nextVersionNo(entity.getTemplateGroupCode(), entity.getChannel()));
        entity.setState(DRAFT);
        entity.setVersion(0L);
        copyContent(params, entity);
        if (mapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知模板失败");
        }
        log.info("已创建通知模板草稿: templateId={}", entity.getId());
        return converter.toVO(entity);
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
                .in(NotificationTemplateEntity::getState, List.of(PUBLISHED, DISABLED))
                .isNull(NotificationTemplateEntity::getDeleted)
                .set(NotificationTemplateEntity::getState, ARCHIVED)
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
    public void enable(UUID id, NotificationTemplateActionFrom params) {
        var entity = getTemplate(id);
        ensureState(entity, DISABLED, "只有已停用模板可以启用");
        ensureVersion(entity, params.getVersion());
        entity.setState(PUBLISHED);
        if (mapper.updateById(entity) != 1) {
            throw new DataSaveException("启用通知模板失败");
        }
        log.info("已启用通知模板: templateId={}", id);
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
        result.setPreviewedAt(timeMapper.toLocalDateTime(Instant.now()));
        return result;
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateContent}）。
     */
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

    /**
     * 校验并确保数据满足当前约束（{@code validateDefinition}）。
     */
    private void validateDefinition(NotificationTemplateEntity entity) {
        var channel = parseChannel(entity.getChannel());
        policy.validateTemplateChannel(policy.parsePurpose(entity.getPurpose()), channel);
        policy.validateTemplateFields(channel, entity.getTitleTemplate(), entity.getHtmlTemplate(),
                entity.getProviderTemplateCode());
        renderer.validateDefinition(entity.getParameterSchema(), entity.getTitleTemplate(), entity.getContentTemplate(), entity.getHtmlTemplate());
    }

    /**
     * 转换、解析或规范化数据（{@code parseChannel}）。
     */
    private NotificationChannel parseChannel(String value) {
        if (!StringUtils.hasText(value)) {
            throw new DataSaveException("通知渠道不能为空");
        }
        try {
            return NotificationChannel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("通知渠道不合法", exception);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code copyContent}）。
     */
    private void copyContent(NotificationTemplateSaveFrom params, NotificationTemplateEntity entity) {
        entity.setTitleTemplate(normalizeNullable(params.getTitleTemplate()));
        entity.setContentTemplate(params.getContentTemplate().trim());
        entity.setHtmlTemplate(normalizeNullable(params.getHtmlTemplate()));
        entity.setParameterSchema(params.getParameterSchema() == null ? Map.of() : params.getParameterSchema());
        entity.setProviderTemplateCode(normalizeNullable(params.getProviderTemplateCode()));
        entity.setVersionDigest(NotificationTemplateDigest.calculate(entity));
    }

    /**
     * 处理内部业务逻辑（{@code nextVersionNo}）。
     */
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

    /**
     * 查询或获取目标数据（{@code getTemplate}）。
     */
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

    /**
     * 生成模板组和渠道的稳定键。
     */
    private String templateKey(NotificationTemplateEntity entity) {
        return entity.getTemplateGroupCode() + "\u0000" + entity.getChannel();
    }

    /**
     * 按模板展示规则选择候选版本：已发布版本优先，同一状态下版本号高的优先。
     */
    private boolean isPreferred(NotificationTemplateEntity candidate, NotificationTemplateEntity current) {
        var candidatePublished = PUBLISHED.equals(candidate.getState());
        var currentPublished = PUBLISHED.equals(current.getState());
        if (candidatePublished != currentPublished) {
            return candidatePublished;
        }
        var candidateVersion = candidate.getVersionNo() == null ? 0 : candidate.getVersionNo();
        var currentVersion = current.getVersionNo() == null ? 0 : current.getVersionNo();
        if (candidateVersion != currentVersion) {
            return candidateVersion > currentVersion;
        }
        return candidate.getUpdatedAt() != null
                && (current.getUpdatedAt() == null || candidate.getUpdatedAt().isAfter(current.getUpdatedAt()));
    }

    /**
     * 将一个渠道版本加入模板组管理视图。
     */
    private void appendGroupVersion(Map<String, NotificationTemplateGroupVO> groups,
                                    NotificationTemplateEntity entity) {
        var group = groups.computeIfAbsent(entity.getTemplateGroupCode(), key -> {
            var value = new NotificationTemplateGroupVO();
            value.setTemplateGroupCode(entity.getTemplateGroupCode());
            value.setTemplateName(entity.getTemplateName());
            value.setPurpose(entity.getPurpose());
            value.setChannels(new ArrayList<>());
            return value;
        });
        var entityUpdatedAt = timeMapper.toLocalDateTime(entity.getUpdatedAt());
        if (group.getUpdatedAt() == null
                || (entityUpdatedAt != null
                        && entityUpdatedAt.isAfter(group.getUpdatedAt()))) {
            group.setUpdatedAt(entityUpdatedAt);
        }
        var channel = group.getChannels()
                .stream()
                .filter(item -> Objects.equals(item.getChannel(), entity.getChannel()))
                .findFirst()
                .orElseGet(() -> {
                    var value = new NotificationTemplateChannelGroupVO();
                    value.setChannel(entity.getChannel());
                    group.getChannels().add(value);
                    return value;
                });
        var view = converter.toVO(entity);
        if (DRAFT.equals(entity.getState())) {
            if (channel.getDraft() == null || isNewer(entity, channel.getDraft())) {
                channel.setDraft(view);
            }
        } else if (channel.getCurrent() == null || isPreferredCurrent(entity, channel.getCurrent())) {
            channel.setCurrent(view);
        }
    }

    /**
     * 比较渠道草稿版本。
     */
    private boolean isNewer(NotificationTemplateEntity candidate, NotificationTemplateVO current) {
        var candidateVersion = candidate.getVersionNo() == null ? 0 : candidate.getVersionNo();
        var currentVersion = current.getVersionNo() == null ? 0 : current.getVersionNo();
        if (candidateVersion != currentVersion) {
            return candidateVersion > currentVersion;
        }
        var currentUpdatedAt = timeMapper.toInstant(current.getUpdatedAt());
        return candidate.getUpdatedAt() != null
                && (currentUpdatedAt == null || candidate.getUpdatedAt().isAfter(currentUpdatedAt));
    }

    /**
     * 按发布优先级选择渠道当前版本。
     */
    private boolean isPreferredCurrent(NotificationTemplateEntity candidate, NotificationTemplateVO current) {
        var candidateRank = currentRank(candidate.getState());
        var currentRank = currentRank(current.getState());
        if (candidateRank != currentRank) {
            return candidateRank > currentRank;
        }
        return isNewer(candidate, current);
    }

    /**
     * 返回渠道当前版本的发布优先级。
     */
    private int currentRank(String state) {
        if (PUBLISHED.equals(state))
            return 3;
        if (DISABLED.equals(state))
            return 2;
        if (ARCHIVED.equals(state))
            return 1;
        return 0;
    }

    /**
     * 查询指定模板渠道的唯一草稿。
     */
    private NotificationTemplateEntity findDraft(String groupCode, String channel) {
        return mapper.selectOne(new LambdaQueryWrapper<NotificationTemplateEntity>()
                .eq(NotificationTemplateEntity::getTemplateGroupCode, groupCode)
                .eq(NotificationTemplateEntity::getChannel, channel)
                .eq(NotificationTemplateEntity::getState, DRAFT)
                .isNull(NotificationTemplateEntity::getDeleted)
                .orderByDesc(NotificationTemplateEntity::getVersionNo)
                .last("LIMIT 1"));
    }

    /**
     * 校验模板列表状态。
     */
    private void validateState(String state) {
        if (StringUtils.hasText(state) && !STATES.contains(state)) {
            throw new DataSaveException("模板状态不合法");
        }
    }

    /**
     * 处理内部业务逻辑（{@code ensureVersion}）。
     */
    private void ensureVersion(NotificationTemplateEntity entity, Long version) {
        if (!Objects.equals(entity.getVersion(), version)) {
            throw new DataSaveException("通知模板已被其他人修改，请刷新后重试");
        }
    }

    /**
     * 处理内部业务逻辑（{@code ensureState}）。
     */
    private void ensureState(NotificationTemplateEntity entity, String expected, String message) {
        ensureState(entity, Set.of(expected), message);
    }

    /**
     * 处理内部业务逻辑（{@code ensureState}）。
     */
    private void ensureState(NotificationTemplateEntity entity, Set<String> expected, String message) {
        if (!expected.contains(entity.getState())) {
            throw new DataSaveException(message);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code normalize}）。
     */
    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeNullable}）。
     */
    private String normalizeNullable(String value) {
        var normalized = normalize(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }
}
