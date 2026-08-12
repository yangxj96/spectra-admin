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

package com.devops00.spectra.ai.configuration;

import com.devops00.spectra.ai.base.AiMemoryId;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * Spectra AI 智能体接口
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/9 17:53
 */
public interface SpectraAssistant {

    /**
     * 流式输出对话（打字机效果）
     *
     * @param memoryId 复合记忆标识（conversationId + token）
     * @param message  问题内容
     */
    TokenStream stream(@MemoryId AiMemoryId memoryId, @UserMessage String message);
}
