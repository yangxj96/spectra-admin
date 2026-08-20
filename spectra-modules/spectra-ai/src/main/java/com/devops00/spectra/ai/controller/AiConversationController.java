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

package com.devops00.spectra.ai.controller;

import com.devops00.spectra.ai.javabean.entity.AiConversation;
import com.devops00.spectra.ai.javabean.from.AiConversationRenameFrom;
import com.devops00.spectra.ai.javabean.vo.ChatMessageVO;
import com.devops00.spectra.ai.service.AiConversationService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI 会话管理控制器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/26
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/conversation")
public class AiConversationController {

    private final AiConversationService conversationService;

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 获取当前用户的会话列表
     */
    @ULog("'查询AI会话列表'")
    @GetMapping(value = "/list", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'ai:read')")
    public List<AiConversation> list() {
        return conversationService.listByUser(securityContextAccessor.currentUserId());
    }

    /**
     * 重命名会话
     */
    @ULog("'重命名AI会话'")
    @PutMapping(value = "/{id}/title", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'ai:update')")
    public void rename(@PathVariable UUID id, @Validated @ModelAttribute AiConversationRenameFrom from) {
        conversationService.rename(id, securityContextAccessor.currentUserId(), from.getTitle());
    }

    /**
     * 删除会话
     */
    @ULog("'删除AI会话'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'ai:delete')")
    public void deleteById(@PathVariable UUID id) {
        conversationService.delete(id, securityContextAccessor.currentUserId());
    }

    /**
     * 获取对话历史消息
     */
    @ULog("'查询AI对话历史'")
    @GetMapping(value = "/{id}/messages", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'ai:read')")
    public List<ChatMessageVO> messages(@PathVariable UUID id) {
        return conversationService.getMessages(id, securityContextAccessor.currentUserId());
    }
}
