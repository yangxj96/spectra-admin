/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应
 *
 * @param <T> 具体类型
 * @author 杨新杰
 * @since 2025/5/26 17:09
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {

    @Serial
    @JsonIgnore
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
