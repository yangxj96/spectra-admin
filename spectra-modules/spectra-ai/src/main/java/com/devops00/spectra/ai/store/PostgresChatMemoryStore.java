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

package com.devops00.spectra.ai.store;

import com.devops00.spectra.ai.base.AiMemoryId;
import com.devops00.spectra.ai.javabean.entity.AiChatMemory;
import com.devops00.spectra.ai.mapper.AiChatMemoryMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/// 基于 PostgreSQL 的 ChatMemoryStore 实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/26
@Component
@RequiredArgsConstructor
public class PostgresChatMemoryStore implements ChatMemoryStore {

    private final AiChatMemoryMapper mapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = extractKey(memoryId);
        AiChatMemory row = mapper.selectById(key);
        if (row == null || row.getMessages() == null || row.getMessages().isBlank()) {
            return List.of();
        }
        return ChatMessageDeserializer.messagesFromJson(row.getMessages());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = extractKey(memoryId);
        String json = ChatMessageSerializer.messagesToJson(messages);
        mapper.upsert(key, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = extractKey(memoryId);
        mapper.deleteById(key);
    }

    private String extractKey(Object memoryId) {
        if (memoryId instanceof AiMemoryId ami) {
            return ami.conversationId();
        }
        return memoryId.toString();
    }
}
