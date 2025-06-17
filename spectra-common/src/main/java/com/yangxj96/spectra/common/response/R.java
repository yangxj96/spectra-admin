/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.response;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应
 *
 * @param <T> 具体类型
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String msg;

    private transient T data;

    /**
     * 自定义的构建方式
     *
     * @param status http状态码
     */
    public R(HttpStatus status) {
        this.code = status.value();
        this.msg = status.getReasonPhrase();
    }

    public static R<Object> success() {
        return R.builder()
                .code(HttpStatus.OK.value())
                .msg(HttpStatus.OK.getReasonPhrase())
                .build();
    }

    public static <T extends Serializable> @NotNull R<T> success(T data) {
        return R.<T>builder()
                .code(HttpStatus.OK.value())
                .msg(HttpStatus.OK.getReasonPhrase())
                .data(data)
                .build();
    }

    public static <T> @NotNull R<T> success(T data) {
        return R.<T>builder()
                .code(HttpStatus.OK.value())
                .msg(HttpStatus.OK.getReasonPhrase())
                .data(data)
                .build();
    }

    public static R<Object> failure() {
        return R.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .msg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();
    }

    public static R<Object> failure(HttpStatus status) {
        return R.builder().code(status.value()).msg(status.getReasonPhrase()).build();
    }

    public static R<Object> failure(String msg) {
        return R.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .msg(msg)
                .build();
    }

    public static R<Object> failure(HttpStatus status, String msg) {
        return R.builder().code(status.value()).msg(msg).build();
    }

}
