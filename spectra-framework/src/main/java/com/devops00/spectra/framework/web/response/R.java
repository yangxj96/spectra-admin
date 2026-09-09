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

package com.devops00.spectra.framework.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一 HTTP 响应包装。
 *
 * <p>{@code code} 表示对外响应状态，{@code msg} 提供可展示的处理结果说明，{@code data} 携带成功响应的业务结果；
 * 无数据成功或失败响应的 {@code data} 保持为 null，避免把“没有响应体”误解为业务空集合。</p>
 *
 * @param <T> 成功响应中 {@code data} 字段承载的业务结果类型
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 对外返回的 HTTP 状态码数值。 */
    private Integer code;

    /** 面向调用方的状态说明；由工厂方法默认取 HTTP 状态原因，也可由调用方覆盖。 */
    private String msg;

    /** 成功响应携带的业务数据；无数据响应时为 null。 */
    private transient T data;

    /**
     * 使用给定 HTTP 状态创建响应包装。
     *
     * @param status 用于填充 {@code code} 和默认 {@code msg} 的 HTTP 状态；不能为 null
     */
    public R(HttpStatus status) {
        this.code = status.value();
        this.msg = status.getReasonPhrase();
    }

    /**
     * 创建不携带业务数据的成功响应。
     *
     * @return code 为 200、msg 为 OK 且 data 为 null 的响应；不会返回 null
     */
    public static R<Object> success() {
        return R.builder().code(HttpStatus.OK.value()).msg(HttpStatus.OK.getReasonPhrase()).build();
    }

    /**
     * 创建携带业务结果的成功响应。
     *
     * @param data 本次成功操作产生的业务结果；允许为 null，表示成功但没有响应体
     * @param <T>  业务结果类型
     * @return code 为 200、msg 为 OK 且 data 保留传入值的响应；不会返回 null
     */
    public static <T> R<T> success(T data) {
        return R.<T>builder().code(HttpStatus.OK.value()).msg(HttpStatus.OK.getReasonPhrase()).data(data).build();
    }

    /**
     * 创建不携带错误详情的服务器失败响应。
     *
     * @return code 为 500、msg 为 Internal Server Error 且 data 为 null 的响应；不会返回 null
     */
    public static R<Object> failure() {
        return R.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).build();
    }

    /**
     * 创建指定 HTTP 状态、但不携带响应数据的失败响应。
     *
     * @param status 表示失败类别的 HTTP 状态；不能为 null
     * @return code 和默认 msg 来自 status、data 为 null 的响应；不会返回 null
     */
    public static R<Object> failure(HttpStatus status) {
        return R.builder().code(status.value()).msg(status.getReasonPhrase()).build();
    }

    /**
     * 创建使用默认服务器错误状态和自定义说明的失败响应。
     *
     * @param msg 面向调用方的失败原因或处理建议；允许为 null，表示不额外提供说明
     * @return code 为 500、msg 为传入值且 data 为 null 的响应；不会返回 null
     */
    public static R<Object> failure(String msg) {
        return R.builder().code(HttpStatus.INTERNAL_SERVER_ERROR.value()).msg(msg).build();
    }

    /**
     * 创建同时指定 HTTP 状态和说明的失败响应。
     *
     * @param status 表示失败类别的 HTTP 状态；不能为 null
     * @param msg    面向调用方的失败原因或处理建议；允许为 null
     * @return code 和 msg 来自传入值、data 为 null 的响应；不会返回 null
     */
    public static R<Object> failure(HttpStatus status, String msg) {
        return R.builder().code(status.value()).msg(msg).build();
    }
}
