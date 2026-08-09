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

package com.devops00.spectra.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.ai.javabean.entity.AiChatMemory;
import com.devops00.spectra.ai.javabean.entity.AiConversation;
import com.devops00.spectra.ai.javabean.enums.ChatRole;
import com.devops00.spectra.ai.javabean.vo.ChatMessageVO;
import com.devops00.spectra.ai.mapper.AiChatMemoryMapper;
import com.devops00.spectra.ai.mapper.AiConversationMapper;
import com.devops00.spectra.ai.service.AiConversationService;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.exception.DataNotExistException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// AI 会话管理 Service 实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/26
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl
        extends
            BaseServiceImpl<AiConversationMapper, AiConversation>
        implements
            AiConversationService {

    private final AiChatMemoryMapper chatMemoryMapper;

    @Override
    public UUID create(UUID userId, String firstMessage) {
        AiConversation conversation = new AiConversation();
        conversation.setId(UUID.randomUUID());
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(firstMessage));
        conversation.setStatus("active");
        baseMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    public List<AiConversation> listByUser(UUID userId) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .isNull(AiConversation::getDeleted)
                        .orderByDesc(AiConversation::getUpdatedAt));
    }

    @Override
    public void rename(UUID conversationId, UUID userId, String title) {
        AiConversation conversation = getOwnedConversation(conversationId, userId);
        conversation.setTitle(title);
        baseMapper.updateById(conversation);
    }

    @Override
    @Transactional
    public void delete(UUID conversationId, UUID userId) {
        getOwnedConversation(conversationId, userId);
        baseMapper.deleteById(conversationId);
        chatMemoryMapper.deleteById(conversationId.toString());
    }

    @Override
    public List<ChatMessageVO> getMessages(UUID conversationId, UUID userId) {
        getOwnedConversation(conversationId, userId);
        AiChatMemory memory = chatMemoryMapper.selectById(conversationId.toString());
        if (memory == null || memory.getMessages() == null || memory.getMessages().isBlank()) {
            return List.of();
        }
        List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(memory.getMessages());
        List<ChatMessageVO> result = new ArrayList<>(messages.size());
        for (ChatMessage msg : messages) {
            if (msg instanceof UserMessage um) {
                result.add(new ChatMessageVO(ChatRole.USER, um.singleText()));
            } else if (msg instanceof AiMessage am) {
                result.add(new ChatMessageVO(ChatRole.ASSISTANT, am.text()));
            } else if (msg instanceof SystemMessage sm) {
                result.add(new ChatMessageVO(ChatRole.SYSTEM, sm.text()));
            }
        }
        return result;
    }

    private AiConversation getOwnedConversation(UUID conversationId, UUID userId) {
        AiConversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new DataNotExistException("会话不存在");
        }
        return conversation;
    }

    private String generateTitle(String message) {
        if (message == null || message.isBlank()) {
            return "新对话";
        }
        String trimmed = message.strip();
        return trimmed.length() <= 30 ? trimmed : trimmed.substring(0, 30) + "...";
    }
}
