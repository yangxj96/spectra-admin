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

package com.devops00.spectra.ai.javabean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/// AI 对话消息持久化记录
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/26
@Getter
@Setter
@ToString
@TableName(value = "ai_chat_memory", schema = "spectra_ai")
public class AiChatMemory {

    /// 会话 ID（= ai_conversation.id::text）
    @TableId(value = "memory_id", type = IdType.INPUT)
    private String memoryId;

    /// 序列化的消息 JSON
    @TableField("messages")
    private String messages;

    /// 创建时间
    @TableField("created_at")
    private Instant createdAt;

    /// 更新时间
    @TableField("updated_at")
    private Instant updatedAt;
}
