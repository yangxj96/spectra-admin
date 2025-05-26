package com.yangxj96.spectra.security.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录认证token响应
 *
 * @author 杨新杰
 * @since 2025/5/26 19:02
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO implements Serializable {

    @Serial
    @JsonIgnore
    private static final long serialVersionUID = 1L;

}
