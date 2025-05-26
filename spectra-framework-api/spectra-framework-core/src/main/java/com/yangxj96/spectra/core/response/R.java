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

    public static R<Object> failure() {
        return R.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .msg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();
    }

    public static R<Object> failure(HttpStatus status) {
        return R.builder().code(status.value()).msg(status.getReasonPhrase()).build();
    }

}
