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

package com.devops00.spectra.ai.service;

import com.devops00.spectra.ai.javabean.entity.AiConversation;
import com.devops00.spectra.ai.javabean.vo.ChatMessageVO;
import com.devops00.spectra.common.base.BaseService;

import java.util.List;
import java.util.UUID;

/**
 * AI 会话管理 Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/26
 */
public interface AiConversationService extends BaseService<AiConversation> {

    /**
     * 创建新会话
     */
    UUID create(UUID userId, String firstMessage);

    /**
     * 获取当前用户的会话列表
     */
    List<AiConversation> listByUser(UUID userId);

    /**
     * 重命名会话
     */
    void rename(UUID conversationId, UUID userId, String title);

    /**
     * 删除会话（同时清理消息存储）
     */
    void delete(UUID conversationId, UUID userId);

    /**
     * 获取对话历史消息
     */
    List<ChatMessageVO> getMessages(UUID conversationId, UUID userId);
}
