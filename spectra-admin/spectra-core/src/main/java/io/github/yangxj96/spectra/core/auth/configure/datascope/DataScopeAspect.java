package io.github.yangxj96.spectra.core.auth.configure.datascope;

import io.github.yangxj96.spectra.common.enums.AuthScope;
import io.github.yangxj96.spectra.core.auth.service.SecurityService;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据范围注解切面
 */
@Aspect
@Component
@Order(1)
public class DataScopeAspect {

    @Resource
    private SecurityService securityService;

    @Around("@annotation(dataScope)")
    public Object intercept(ProceedingJoinPoint pjp, DataScope dataScope) throws Throwable {
        try {
            var info = new DataScopeInfo();
            if (dataScope.filter()) {
                info.setFilter(true);
                // 获取用户最大权限范围
                info.setScope(securityService.getCurrentMaxScope());
            }
            DataScopeContext.set(info);
            return pjp.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }
}
