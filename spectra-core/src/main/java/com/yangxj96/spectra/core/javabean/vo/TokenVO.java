package com.yangxj96.spectra.core.javabean.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录认证token响应
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO implements Serializable {

    @Serial
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String username;

    private String accessToken;

    private List<String> authorities;

    private List<String> roles;

}
