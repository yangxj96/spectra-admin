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

package com.devops00.spectra.ai.javabean.vo;

import lombok.Data;

import java.util.List;

/**
 * OpenAI标准响应VO
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/5/18 09:44
 */
@Data
public class OpenAIStreamVO {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 对象类型，流式固定为 chat.completion.chunk
     */
    private String object = "chat.completion.chunk";

    /**
     * 创建时间戳
     */
    private long created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 会话 ID（前端据此保存当前会话）
     */
    private String conversationId;

    /**
     * 返回的内容选项
     */
    private List<Choice> choices;

    /**
     * 流式响应选项。
     */
    @Data
    public static class Choice {

        /**
         * 选项索引
         */
        private int index;

        /**
         * 增量内容
         */
        private Delta delta;

        /**
         * 结束原因，流式传输中通常为 null，最后一条为 "stop"
         */
        private String finish_reason;
    }

    /**
     * 流式响应增量内容。
     */
    @Data
    public static class Delta {

        /**
         * 角色 (assistant)
         */
        private String role;

        /**
         * 增量文本内容
         */
        private String content;
    }
}
