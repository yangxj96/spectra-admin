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

package com.devops00.spectra.framework.web.advice.crypto;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.framework.web.response.R;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.core.io.Resource;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import reactor.core.publisher.Flux;

import java.util.regex.Pattern;

/**
 * 响应结果统一修改
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@NullMarked
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ControllerAdvice
public class ResponseModifyAdvice implements ResponseBodyAdvice<Object> {

    /** 只对项目 Controller 包中的普通响应启用统一响应包装。 */
    private static final Pattern PATTERN = Pattern.compile("com\\.devops00\\.spectra\\..*\\.controller.*");

    /**
     * 判断当前响应是否需要统一结构包装。
     *
     * @param returnType    控制器方法返回类型，用于判断响应加密规则。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @return 返回当前控制器响应是否需要统一结构包装；流式、资源和二进制响应返回 false，其余匹配的控制器响应返回 true。
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        log.debug(LogPrefix.WEB.f("进入修改"));

        // 流式响应由响应写出层持续处理，不能提前收集并包装成单个 JSON 对象。
        if (Flux.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 统一响应和 ResponseEntity 已经表达了完整响应语义，不允许再次包装。
        if (R.class.isAssignableFrom(returnType.getParameterType())
                || ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 二进制和资源响应必须保持原始字节，否则下载内容会被 JSON 包装破坏。
        if (ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return false;
        }

        // 忽略 ResourceHttpMessageConverter（避免把文件下载响应包装成统一业务响应）
        if (ResourceHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return false;
        }

        if (Resource.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        var declaringClass = returnType.getContainingClass();
        // 判断是否是 BaseController 的子类 或者 属于 com.yangxj96.spectra.xxx.controller 包下
        return PATTERN.matcher(declaringClass.getPackageName()).matches();
    }

    /**
     * 在响应写出前执行响应加密或结构转换。
     *
     * @param body          待加密、解密或转换的请求/响应体。
     * @param returnType    控制器方法返回类型，用于判断响应加密规则。
     * @param contentType   响应媒体类型，用于选择序列化和加密策略。
     * @param converterType 当前 HTTP 消息转换器类型，用于判断 Advice 是否适用。
     * @param request       当前 HTTP 请求或待处理的安全业务请求。
     * @param response      当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 流式、资源、String、byte[]、{@code R} 和 {@code ResponseEntity} 原样返回；普通对象包装为 {@code R.success(body)}，204/304 空响应返回 null。
     */
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {

        // 第一优先级：流式、资源和已经统一过的响应直接放行，避免重复包装。
        if (MediaType.TEXT_EVENT_STREAM.includes(contentType)
                || body instanceof Flux
                || body instanceof Resource
                || body instanceof R<?>
                || body instanceof ResponseEntity<?>
                || Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过流式响应包装"));
            return body;
        }

        // 第二优先级：String 和 byte[] 由对应转换器直接写出，不改写其内容。
        if (body instanceof String || body instanceof byte[]) {
            log.debug(LogPrefix.WEB.f("跳过 String 和 byte[]"));
            return body;
        }

        // 第三优先级：只有确认不是特殊响应后才处理 null，避免丢失 204/304 语义。
        if (body == null) {
            log.debug(LogPrefix.WEB.f("body为null处理"));
            return handleNullBody(request, response);
        }

        // 正常包装
        log.debug(LogPrefix.WEB.f("包装返回"));
        return R.success(body);
    }

    /**
     * 为没有响应体的 REST 请求补充合适的状态响应。
     *
     * <p>Servlet 响应优先使用实际状态；无法取得 Servlet 响应时，再按 POST/PUT 的 REST 约定推导状态。</p>
     */
    private @Nullable Object handleNullBody(ServerHttpRequest request, ServerHttpResponse response) {
        // 如果能获取到响应则直接响应
        if (response instanceof ServletServerHttpResponse resp) {
            int status = resp.getServletResponse().getStatus();
            if (status == HttpStatus.NO_CONTENT.value() || status == HttpStatus.NOT_MODIFIED.value()) {
                return null;
            }
            HttpStatus resolve = HttpStatus.resolve(status);
            if (resolve == null) {
                resolve = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return new R<>(resolve);
        } else {
            // 否则根据方法的RESTFull API设计规范进行响应
            String httpMethod = request.getMethod().name();
            if ("POST".equalsIgnoreCase(httpMethod)) {
                // 可以返回特定格式的创建响应
                response.setStatusCode(HttpStatus.CREATED);
                return new R<>(HttpStatus.CREATED);
            } else if ("PUT".equalsIgnoreCase(httpMethod)) {
                // 可以返回特定格式的更新响应
                response.setStatusCode(HttpStatus.NO_CONTENT);
                return null;
            } else {
                return R.success();
            }
        }
    }
}
