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

import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationDirectAddress;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.core.notification.javabean.entity.NotificationMessageEntity;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.core.notification.javabean.enums.NotificationMessageStatus;
import com.devops00.spectra.core.notification.mapper.NotificationMessageMapper;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.core.notification.sender.NotificationSendTarget;
import com.devops00.spectra.core.notification.sender.NotificationSender;
import com.devops00.spectra.core.notification.sender.RenderedMessage;
import com.devops00.spectra.core.notification.sender.SendOutcome;
import com.devops00.spectra.core.notification.service.NotificationTemplateService;
import com.devops00.spectra.core.notification.template.NotificationTemplateRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 同步通知服务实现；按模板渲染后逐个目标调用渠道发送器并记录发送历史。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationTemplateService templateService;
    private final NotificationRecipientDirectory recipientDirectory;
    private final NotificationMessageMapper messageMapper;
    private final NotificationModuleProperties properties;
    private final Map<NotificationChannel, NotificationSender> senders;

    /**
     * 构造通知服务。
     *
     * @param templateService    模板服务
     * @param recipientDirectory 收件人目录
     * @param messageMapper      发送历史 Mapper
     * @param properties         通知配置
     * @param senderList         渠道发送器
     */
    public NotificationServiceImpl(NotificationTemplateService templateService,
                                   NotificationRecipientDirectory recipientDirectory,
                                   NotificationMessageMapper messageMapper,
                                   NotificationModuleProperties properties,
                                   List<NotificationSender> senderList) {
        this.templateService = templateService;
        this.recipientDirectory = recipientDirectory;
        this.messageMapper = messageMapper;
        this.properties = properties;
        this.senders = senderList.stream()
                .collect(Collectors.toMap(NotificationSender::channel,
                        Function.identity(), (first, ignored) -> first,
                        () -> new EnumMap<>(NotificationChannel.class)));
    }

    @Override
    public NotificationReceipt send(NotificationSendRequest request) {
        validate(request);
        if (!properties.isEnabled()) {
            throw new BusinessRuleViolationException("通知模块未启用");
        }
        var recipients = recipientDirectory.resolve(request.recipientUserIds());
        var results = new ArrayList<NotificationReceipt.SendResult>();
        for (var channel : request.channels()) {
            var template = templateService.require(request.templateCode(), channel);
            var rendered = render(template, request);
            var sender = senders.get(channel);
            var targets = targets(channel, recipients, request.directAddresses());
            if (sender == null) {
                results.add(new NotificationReceipt.SendResult(channel, null, null, null, false,
                        "CHANNEL_NOT_CONFIGURED", "通知渠道未注册"));
                continue;
            }
            for (var target : targets) {
                results.add(dispatch(sender, target, template, rendered));
            }
        }
        return new NotificationReceipt(results);
    }

    /**
     * 渲染模板。
     *
     * @param template 模板实体
     * @param request  通知请求
     * @return 渲染内容
     */
    private RenderedMessage render(NotificationTemplateEntity template, NotificationSendRequest request) {
        var parameters = new LinkedHashMap<String, Object>(request.parameters());
        parameters.putAll(request.sensitiveParameters());
        var title = StringUtils.hasText(template.getTitleTemplate())
                ? NotificationTemplateRenderer.render(template.getTitleTemplate(), parameters)
                : template.getName();
        return new RenderedMessage(template.getCode(), title,
                NotificationTemplateRenderer.render(template.getContentTemplate(), parameters),
                StringUtils.hasText(template.getHtmlTemplate())
                        ? NotificationTemplateRenderer.render(template.getHtmlTemplate(), parameters)
                        : null,
                template.getProviderTemplateCode(), parameters);
    }

    /**
     * 收集指定渠道的发送目标。
     *
     * @param channel         发送渠道
     * @param recipients      用户收件人
     * @param directAddresses 直接地址
     * @return 去重后的发送目标
     */
    private List<NotificationSendTarget> targets(NotificationChannel channel, List<NotificationRecipient> recipients,
                                                 List<NotificationDirectAddress> directAddresses) {
        var targets = new LinkedHashMap<String, NotificationSendTarget>();
        for (var recipient : recipients) {
            if (recipient == null || recipient.userId() == null) {
                continue;
            }
            if (!recipient.active()) {
                log.warn("通知收件人不存在或已禁用: userId={}", recipient.userId());
                continue;
            }
            var address = recipient.addressFor(channel);
            if (channel != NotificationChannel.IN_APP && !StringUtils.hasText(address)) {
                log.warn("通知收件人缺少渠道地址: userId={}, channel={}", recipient.userId(), channel);
                continue;
            }
            var target = new NotificationSendTarget(channel, recipient.userId(), address);
            targets.putIfAbsent("U:" + recipient.userId(), target);
        }
        for (var directAddress : directAddresses) {
            if (directAddress == null
                    || directAddress.channel() != channel
                    || channel == NotificationChannel.IN_APP
                    || !StringUtils.hasText(directAddress.address())) {
                continue;
            }
            var target = new NotificationSendTarget(channel, null, directAddress.address());
            targets.putIfAbsent("A:" + directAddress.address(), target);
        }
        return List.copyOf(targets.values());
    }

    /**
     * 调用渠道发送器并写入发送历史。
     *
     * @param sender   渠道发送器
     * @param target   发送目标
     * @param template 模板实体
     * @param rendered 渲染内容
     * @return 单目标结果
     */
    private NotificationReceipt.SendResult dispatch(NotificationSender sender, NotificationSendTarget target,
                                                    NotificationTemplateEntity template, RenderedMessage rendered) {
        var outcome = sender.send(target, rendered);
        var message = record(target, template, rendered, outcome);
        return new NotificationReceipt.SendResult(target.channel(), target.userId(), target.address(),
                message.getId(), outcome.success(), outcome.errorCode(), outcome.errorMessage());
    }

    /**
     * 写入发送历史。
     *
     * @param target   发送目标
     * @param template 模板实体
     * @param rendered 渲染内容
     * @param outcome  发送结果
     * @return 历史实体
     */
    private NotificationMessageEntity record(NotificationSendTarget target, NotificationTemplateEntity template,
                                             RenderedMessage rendered, SendOutcome outcome) {
        var entity = new NotificationMessageEntity();
        entity.setChannel(target.channel().name());
        entity.setTemplateCode(template.getCode());
        entity.setTitle(rendered.title());
        entity.setContent(rendered.content());
        entity.setHtmlContent(rendered.htmlContent());
        entity.setStatus(outcome.success()
                ? NotificationMessageStatus.SUCCESS.name()
                : NotificationMessageStatus.FAILED.name());
        entity.setErrorCode(outcome.errorCode());
        entity.setErrorMessage(outcome.errorMessage());
        entity.setProvider(outcome.provider());
        entity.setConfigId(outcome.configId());
        entity.setRecipientUserId(target.userId());
        entity.setRecipientAddress(target.address());
        messageMapper.insert(entity);
        return entity;
    }

    /**
     * 校验通知请求必需字段。
     *
     * @param request 通知请求
     */
    private void validate(NotificationSendRequest request) {
        if (request == null
                || !StringUtils.hasText(request.templateCode())
                || request.channels().isEmpty()
                || (request.recipientUserIds().isEmpty() && request.directAddresses().isEmpty())) {
            throw new BusinessRuleViolationException("通知请求参数不完整");
        }
        if (request.recipientUserIds().stream().anyMatch(Objects::isNull)) {
            throw new BusinessRuleViolationException("通知收件人无效");
        }
    }
}
