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

package com.devops00.spectra.ai.base;

/**
 * AI 对话复合记忆标识
 *
 * conversationId 用于 ChatMemory 缓存 key 和数据库存储 key；
 * token 用于工具执行时设置 SecurityContext。
 * equals/hashCode 仅比较 conversationId，确保同一会话在 token 变化后仍复用同一 Memory 实例。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/26
 */
public record AiMemoryId(String conversationId, String token) {

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AiMemoryId that))
            return false;
        return conversationId.equals(that.conversationId);
    }

    @Override
    public int hashCode() {
        return conversationId.hashCode();
    }

    @Override
    public String toString() {
        return conversationId;
    }
}
