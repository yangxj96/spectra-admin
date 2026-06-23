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

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/// Assistant
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/9 17:53
public interface DeepSeekAssistant {

    /// 标准的同步阻塞对话
    ///
    /// @param token   传递token,用来设置Security上下文
    /// @param message 问题内容
    @SystemMessage("你是一个全能的开发助手。")
    String chat(@MemoryId String token, @UserMessage String message);

    /// 高级流式输出（打字机效果）
    /// 返回 TokenStream 是 LangChain4j 流式响应的标准抽象
    ///
    /// @param token   传递token,用来设置Security上下文
    /// @param message 问题内容
    @SystemMessage("你是一个全能的开发助手。")
    TokenStream stream(@MemoryId String token, @UserMessage String message);

}
