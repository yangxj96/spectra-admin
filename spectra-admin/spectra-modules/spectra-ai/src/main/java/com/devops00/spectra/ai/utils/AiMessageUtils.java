package com.devops00.spectra.ai.utils;


/**
 * AI消息工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/4/26 12:15
 */
public class AiMessageUtils {

    private AiMessageUtils() {
        // 工具类禁止实例化
    }

    /**
     * 构建首条SSE消息
     *
     * @param chatId  对话ID
     * @param created 创建日期
     * @return 构建好的消息
     */
    public static String buildFirstSSE(String chatId, long created) {
        String json = String.format("""
                {"id":"%s","object":"chat.completion.chunk","created":%d,"model":"deepseek-chat","choices":[{"delta":{"role":"assistant","content":""},"index":0,"finish_reason":null}]}
                """, chatId, created);
        return json + "\n\n";
    }

    /**
     * 构建内容部分SSE消息
     *
     * @param chatId  对话ID
     * @param created 创建日期
     * @return 构建好的消息
     */
    public static String buildContentSSE(String chatId, long created, String content) {
        String escaped = escapeJson(content);
        String json = String.format("""
                {"id":"%s","object":"chat.completion.chunk","created":%d,"model":"deepseek-chat","choices":[{"delta":{"content":"%s"},"index":0,"finish_reason":null}]}
                """, chatId, created, escaped);
        return json + "\n\n";
    }

    /**
     * 构建完成部分(结尾)SSE消息
     *
     * @param chatId  对话ID
     * @param created 创建日期
     * @return 构建好的消息
     */
    public static String buildDoneSSE(String chatId, long created) {
        String json = String.format("""
                {"id":"%s","object":"chat.completion.chunk","created":%d,"model":"deepseek-chat","choices":[{"delta":{},"index":0,"finish_reason":"stop"}]}
                """, chatId, created);
        return json + "\n\n" + "data: [DONE]\n\n";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


}
