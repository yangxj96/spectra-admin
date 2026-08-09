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

package com.devops00.spectra.framework.configure.mvc.advice.response;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
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

    private static final Pattern PATTERN = Pattern.compile("com\\.devops00\\.spectra\\..*\\.controller.*");

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        log.debug(LogPrefix.WEB.f("进入修改"));

        // 忽略流式
        if (returnType.getParameterType().isAssignableFrom(Flux.class)) {
            return false;
        }

        // 忽略 ByteArrayHttpMessageConverter（避免干扰文件下载等二进制响应）
        if (converterType.isAssignableFrom(ByteArrayHttpMessageConverter.class)) {
            return false;
        }

        var declaringClass = returnType.getContainingClass();
        // 判断是否是 BaseController 的子类 或者 属于 com.yangxj96.spectra.xxx.controller 包下
        return PATTERN.matcher(declaringClass.getPackageName()).matches();
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {

        // 第一优先级：流式直接放行
        if (MediaType.TEXT_EVENT_STREAM.includes(contentType) || body instanceof Flux || Flux.class.isAssignableFrom(returnType.getParameterType())) {
            log.debug(LogPrefix.WEB.f("跳过流式响应包装"));
            return body;
        }

        // 第二：String / byte[]
        if (body instanceof String || body instanceof byte[]) {
            log.debug(LogPrefix.WEB.f("跳过 String 和 byte[]"));
            return body;
        }

        // 第三：null 处理（必须放后面）
        if (body == null) {
            log.debug(LogPrefix.WEB.f("body为null处理"));
            return handleNullBody(request, response);
        }

        // 正常包装
        log.debug(LogPrefix.WEB.f("包装返回"));
        return R.success(body);
    }

    /**
     * 空body处理
     *
     * @param request
     *            请求
     * @param response
     *            响应
     * @return 结果
     */
    private R<Object> handleNullBody(ServerHttpRequest request, ServerHttpResponse response) {
        R<Object> r;
        // 如果能获取到响应则直接响应
        if (response instanceof ServletServerHttpResponse resp) {
            int status = resp.getServletResponse().getStatus();
            HttpStatus resolve = HttpStatus.resolve(status);
            if (resolve == null) {
                resolve = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            r = new R<>(resolve);
        } else {
            // 否则根据方法的RESTFull API设计规范进行响应
            String httpMethod = request.getMethod().name();
            if ("POST".equalsIgnoreCase(httpMethod)) {
                // 可以返回特定格式的创建响应
                response.setStatusCode(HttpStatus.CREATED);
                r = new R<>(HttpStatus.CREATED);
            } else if ("PUT".equalsIgnoreCase(httpMethod)) {
                // 可以返回特定格式的更新响应
                response.setStatusCode(HttpStatus.NO_CONTENT);
                r = new R<>(HttpStatus.NO_CONTENT);
            } else {
                r = R.success();
            }
        }
        return r;
    }
}
