package com.devops00.spectra.ai.javabean.vo;

import lombok.Data;

import java.util.List;


@Data
public class OpenAIStreamVO {

    /// 响应ID
    private String id;
    /// 对象类型，流式固定为 chat.completion.chunk
    private String object = "chat.completion.chunk";
    /// 创建时间戳
    private long created;
    /// 模型名称
    private String model;
    /// 返回的内容选项
    private List<Choice> choices;

    @Data
    public static class Choice {
        /// 选项索引
        private int index;
        /// 增量内容
        private Delta delta;
        /// 结束原因，流式传输中通常为 null，最后一条为 "stop"
        private String finish_reason;
    }

    @Data
    public static class Delta {
        /// 角色 (assistant)
        private String role;
        /// 增量文本内容
        private String content;
    }
}