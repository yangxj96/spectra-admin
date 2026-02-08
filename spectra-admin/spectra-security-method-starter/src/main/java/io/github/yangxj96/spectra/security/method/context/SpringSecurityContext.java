package io.github.yangxj96.spectra.security.method.context;

import io.github.yangxj96.spectra.security.api.context.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 提供业务使用的统一上下文访问方法
 */
public class SpringSecurityContext {

    public static SecurityContext getContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null; // 或返回一个匿名/默认实现
        }
        return new SpringSecurityContextAdapter(auth);
    }
}
