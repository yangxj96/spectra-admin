package io.github.yangxj96.spectra.core.configure.security.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 权限配置相关内容
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/4 10:39
 */
@Data
@ConfigurationProperties(prefix = "spectra.security")
public class SecurityProperties {

    /**
     * 放行白名单
     */
    private List<String> whitelists = new ArrayList<>(Arrays.asList(
            "/common/kaptcha",
            "/auth/login"
    ));


    /**
     * token超时时长
     */
    private Long tokenExpire = 7200L;

    /**
     * 超级管理员角色：拥有绝对权限，不需要匹配任何权限表达式
     */
    private String administrators = "ROLE_DEV_OPS";

}
