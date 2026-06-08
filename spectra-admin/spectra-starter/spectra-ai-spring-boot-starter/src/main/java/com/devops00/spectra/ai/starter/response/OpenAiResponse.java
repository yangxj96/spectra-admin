package com.devops00.spectra.ai.starter.response;


import com.devops00.spectra.ai.starter.dto.OpenAIStreamVO;
import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.event.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * DeepSeek的响应格式化
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 10:59
 */
@Slf4j
public class OpenAiResponse {

    /// 封装基础 VO 构建逻辑，消除重复代码
    ///
    /// @param modelName   模型名称
    /// @param responseId  响应ID
    /// @param createdTime 创建时间
    /// @return 封装的OpenAI流VO对象
    public static OpenAIStreamVO buildBaseVO(String modelName, String responseId, long createdTime) {
        var response = new OpenAIStreamVO();
        response.setId(responseId);
        response.setCreated(createdTime);
        response.setModel(modelName);
        return response;
    }

    /// 核心转换逻辑：将 AgentEvent 转换为 OpenAI 协议格式
    ///
    /// @param modelName   模型名称
    /// @param event       事件对象
    /// @param responseId  响应ID
    /// @param createdTime 创建时间
    /// @return 封装的OpenAI流VO对象
    public static OpenAIStreamVO convertToStreamVO(String modelName, Object event, String responseId, long createdTime) {
        var response = buildBaseVO(modelName, responseId, createdTime);
        var choice = new OpenAIStreamVO.Choice();
        choice.setIndex(0);
        var delta = new OpenAIStreamVO.Delta();
        delta.setRole("assistant");

        if (event instanceof AgentEvent) {
            switch (event) {
                // Agent任务流的开始
                case AgentStartEvent e:
                    log.trace("{}AgentStartEvent:{}", LogPrefix.AI.p(), e.getReplyId());
                    break;
                // Agent任务流的结束
                case AgentEndEvent e:
                    log.trace("{}AgentEndEvent:{}", LogPrefix.AI.p(), e.getReplyId());
                    choice.setFinish_reason("stop");
                    break;
                // 外部请求主动终止了 Agent 的执行
                case RequestStopEvent e:
                    log.trace("{}RequestStopEvent:{}", LogPrefix.AI.p(), e.getReason());
                    break;
                // Agent最终面向用户的自然语言回复的流式输出 开始
                case TextBlockStartEvent e:
                    log.trace("{}TextBlockStartEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // Agent最终面向用户的自然语言回复的流式输出 结束
                case TextBlockEndEvent e:
                    log.trace("{}TextBlockEndEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // Agent最终面向用户的自然语言回复的流式输出 增量内容
                case TextBlockDeltaEvent e:
                    log.trace("{}TextBlockDeltaEvent:{}", LogPrefix.AI.p(), e.getDelta());
                    delta.setContent(e.getDelta());
                    break;
                // 非文本的结构化数据输出 开始
                case DataBlockStartEvent e:
                    log.trace("{}DataBlockStartEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // 非文本的结构化数据输出 结束
                case DataBlockEndEvent e:
                    log.trace("{}DataBlockEndEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // 非文本的结构化数据输出 增量内容
                case DataBlockDeltaEvent e:
                    log.trace("{}DataBlockDeltaEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // Agent 陷入了死循环或思考过久，达到了最大迭代次数限制被强制停止。
                case ExceedMaxItersEvent e:
                    log.trace("{}ExceedMaxItersEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // Agent 开始向大模型发送请求
                case ModelCallStartEvent e:
                    log.trace("{}ModelCallStartEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // Agent 开始向大模型发送请求收到完整响应
                case ModelCallEndEvent e:
                    log.trace("{}ModelCallEndEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // 推理 开始
                case ThinkingBlockStartEvent e:
                    log.trace("{}ThinkingBlockStartEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // 推理 结束
                case ThinkingBlockEndEvent e:
                    log.trace("{}ThinkingBlockEndEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // 推理 增量内容
                case ThinkingBlockDeltaEvent e:
                    log.trace("{}ThinkingBlockDeltaEvent:{}", LogPrefix.AI.p(), e.getBlockId());
                    break;
                // 工具调用流式 开始
                case ToolCallStartEvent e:
                    log.trace("{}ToolCallStartEvent:{}", LogPrefix.AI.p(), e.getToolCallId());
                    break;
                // 工具调用流式 结束
                case ToolCallEndEvent e:
                    log.trace("{}ToolCallEndEvent:{}", LogPrefix.AI.p(), e.getToolCallId());
                    break;
                // 工具调用流式 增量内容
                case ToolCallDeltaEvent e:
                    log.trace("{}ToolCallDeltaEvent:{}", LogPrefix.AI.p(), e.getToolCallId());
                    break;
                // 工具结果流式 开始
                case ToolResultStartEvent e:
                    log.trace("{}ToolResultStartEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // 工具结果流式 结束
                case ToolResultEndEvent e:
                    log.trace("{}ToolResultEndEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // 工具结果流式 数据增量内容
                case ToolResultDataDeltaEvent e:
                    log.trace("{}ToolResultDataDeltaEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // 工具结果流式 文本增量内容
                case ToolResultTextDeltaEvent e:
                    log.trace("{}ToolResultTextDeltaEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // Agent 需要执行外部系统操作，暂停等待。
                case RequireExternalExecutionEvent e:
                    log.trace("{}RequireExternalExecutionEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // Agent 在执行高危操作（如删除数据、发送邮件）前，暂停等待用户确认。
                case RequireUserConfirmEvent e:
                    log.trace("{}RequireUserConfirmEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // 用户点击了“同意”或“拒绝”后产生的事件。
                case UserConfirmResultEvent e:
                    log.trace("{}UserConfirmResultEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                // 外部系统执行完毕，将结果回传给 Agent。
                case ExternalExecutionResultEvent e:
                    log.trace("{}ExternalExecutionResultEvent:{}", LogPrefix.AI.p(), e.getId());
                    break;
                default:
                    throw new RuntimeException(LogPrefix.AI.p() + "未识别的响应类型:" + event);
            }
        }
        choice.setDelta(delta);
        response.setChoices(List.of(choice));
        return response;
    }

    /// 统一的错误降级处理
    ///
    /// @param modelName   模型名称
    /// @param error       错误信息
    /// @param responseId  响应ID
    /// @param createdTime 创建时间
    /// @return 封装的OpenAI流VO对象
    public static Flux<OpenAIStreamVO> handleStreamError(String modelName, Throwable error, String responseId, long createdTime) {
        log.error("Agent streamEvents error", error);
        var errorResponse = buildBaseVO(modelName, responseId, createdTime);
        var choice = new OpenAIStreamVO.Choice();
        choice.setIndex(0);
        choice.setFinish_reason("error");
        choice.setDelta(new OpenAIStreamVO.Delta());
        errorResponse.setChoices(List.of(choice));
        return Flux.just(errorResponse);
    }

}
