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

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * AI 工具执行统一核心适配器。
 *
 * 本工具类旨在解决 LangChain4j 默认使用 Gson 序列化工具入参/出参，
 *
 * 导致与 Spring Boot 项目自带 Jackson 全局配置（如 Instant 时间格式、Long 转 String 等）冲突的问题。
 *
 * 采用“进门自己解，出门自己装”的防腐层模式:
 * <ol>
 * <li>输入端：接收大模型生成的原始 JSON 字符串，利用项目统一的 Jackson 反序列化为强类型 DTO。</li>
 * <li>输出端：捕获业务执行结果，利用项目统一的 Jackson 序列化为标准的 JSON 字符串吐给大模型。</li>
 * <li>异常端：统一拦截业务及解析异常，转为大模型可读的 JSON 错误文本，防止 WebFlux 响应流中断。</li>
 * </ol>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/11 15:18
 */
@Slf4j
@Component
public class ToolExecutor {

    private static ObjectMapper objectMapper;

    /**
     * 构造函数注入，用于在 Spring 启动时将容器中配好的 ObjectMapper 赋给静态变量。
     *
     * @param objectMapper
     *            项目全局 Jackson 映射器
     */
    public ToolExecutor(ObjectMapper objectMapper) {
        ToolExecutor.objectMapper = objectMapper;
    }

    /**
     * 适用于大模型既生成了业务参数，又需要当前智能体对话关联的用户 ID 的场景（如：查当前用户的订单、改当前用户的设置）。
     *
     * @param jsonParams
     *            大模型生成的原始工具入参 JSON 字符串（允许为 null 或空字符串）
     * @param token
     *            来自 LangChain4j {@code @ToolMemoryId} 的当前会话token
     * @param reqClass
     *            期望反序列化出来的业务 DTO 目标类 Class
     * @param businessLogic
     *            核心业务逻辑函数表达式，入参为 (解析后的DTO, 用户ID)，返回强类型业务结果
     * @param <R>
     *            请求参数的强类型泛型（Request DTO）
     * @param <T>
     *            业务返回结果的强类型泛型（Target/Result Object）
     * @return 序列化后的符合项目 Jackson 规范的 JSON 结果字符串（若发生异常则返回错误 JSON 文本）
     * @throws IllegalStateException
     *             如果工具类未被 Spring 成功注入初始化则抛出
     */
    public static <R, T> String execute(String token, String jsonParams, Class<R> reqClass, BiFunction<R, String, T> businessLogic) {
        return executeWithSecurity(token, () -> {
            // 1. 解析入参
            R request = parseParams(jsonParams, reqClass);
            // 2. 回调核心业务逻辑
            return businessLogic.apply(request, token);
        });
    }

    /**
     * 适用于大模型不需要生成任何业务参数，但必须获取当前用户上下文的场景（如：查看我的个人资产、查看我的个人信息）。
     *
     * @param token
     *            来自 LangChain4j {@code @ToolMemoryId} 的当前会话token
     * @param businessLogic
     *            核心业务逻辑函数表达式，入参为 (用户ID)，返回强类型业务结果
     * @param <T>
     *            业务返回结果的强类型泛型（Target/Result Object）
     * @return 序列化后的符合项目 Jackson 规范的 JSON 结果字符串（若发生异常则返回错误 JSON 文本）
     * @throws IllegalStateException
     *             如果工具类未被 Spring 成功注入初始化则抛出
     */
    public static <T> String execute(String token, Function<String, T> businessLogic) {
        return executeWithSecurity(token, () -> businessLogic.apply(token));
    }

    /**
     * 适用于纯公共业务工具，大模型生成了业务参数，但执行逻辑与当前对话的具体用户 ID 无关的场景（如：根据城市查天气、根据商品ID查公共库存）。
     *
     * @param jsonParams
     *            大模型生成的原始工具入参 JSON 字符串（允许为 null 或空字符串）
     * @param reqClass
     *            期望反序列化出来的业务 DTO 目标类 Class
     * @param businessLogic
     *            核心业务逻辑函数表达式，入参为 (解析后的DTO)，返回强类型业务结果
     * @param <R>
     *            请求参数的强类型泛型（Request DTO）
     * @param <T>
     *            业务返回结果的强类型泛型（Target/Result Object）
     * @return 序列化后的符合项目 Jackson 规范的 JSON 结果字符串（若发生异常则返回错误 JSON 文本）
     * @throws IllegalStateException
     *             如果工具类未被 Spring 成功注入初始化则抛出
     */
    public static <R, T> String execute(String jsonParams, Class<R> reqClass, Function<R, T> businessLogic) {
        try {
            checkInitialized();
            R request = parseParams(jsonParams, reqClass);
            T result = businessLogic.apply(request);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return buildErrorResponse(e);
        }
    }

    // --- 内部辅助方法 ---

    /**
     * 环绕控制：负责管理带有 Security 权限校验的工具方法执行全生命周期
     */
    private static <T> String executeWithSecurity(String token, Supplier<T> coreLogic) {
        boolean contextBound = false;
        try {
            checkInitialized();

            // 注入 Security 上下文
            if (StrUtils.isNotBlank(token)) {
                SecurityUser user = SecUtil.getCurrentUser(token);
                if (user == null) {
                    throw new BadCredentialsException("认证失败：Token 已过期或无效");
                }
                var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                contextBound = true;
            }

            // 执行外部传入的真实业务逻辑
            T result = coreLogic.get();
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return buildErrorResponse(e);
        } finally {
            // 无论业务成功或是抛错，只要绑定过线程，必须无条件擦除，防内存泄漏与身份交叉污染
            if (contextBound) {
                SecurityContextHolder.clearContext();
            }
        }
    }

    /**
     * 统一参数解析与兜底
     */
    private static <R> R parseParams(String jsonParams, Class<R> reqClass) {
        return (jsonParams == null || jsonParams.trim().isEmpty())
                ? objectMapper.readValue("{}", reqClass)
                : objectMapper.readValue(jsonParams, reqClass);
    }

    /**
     * 检查静态映射器是否已由 Spring 完成注入初始化。
     */
    private static void checkInitialized() {
        if (objectMapper == null) {
            throw new IllegalStateException("ToolExecutor 未初始化，ObjectMapper 为空！");
        }
    }

    /**
     * 统一捕获工具执行期异常，包装为符合标准的大模型可读 JSON 文本。
     *
     * @param e
     *            捕获的异常对象
     * @return 包装后的包含错误信息的 JSON 字符串
     */
    private static String buildErrorResponse(Exception e) {
        // 打印错误日志，方便本地排查
        log.error("{}构建出错:{}", LogPrefix.AI.p(), e.getMessage(), e);
        return String.format("{\"status\":\"ERROR\",\"message\":\"%s\"}", e.getMessage());
    }
}