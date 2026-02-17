package io.github.yangxj96.spectra.core.configure.datascope;


import io.github.yangxj96.spectra.core.service.auth.DataScopeService;
import io.github.yangxj96.spectra.security.base.holder.SecUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/// 数据范围切面
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:56
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataScopeAspect {

    private final DataScopeService dataScopeService;

    public DataScopeAspect(DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    /// 数据权限范围环绕拦截
    ///
    /// @param pjp 进入点
    /// @return 响应数据
    /// @throws Throwable e
    @Around(value = "@annotation(dataScope) ")
    public Object around(ProceedingJoinPoint pjp, DataScope dataScope) throws Throwable {
        if (!dataScope.enabled()) {
            return pjp.proceed();
        }

        // 正常 DataScope
        String userId = SecUtil.getCurrentUserId();
        DataScopeContext context = dataScopeService.resolve(userId);

        try {
            DataScopeHolder.set(context);
            return pjp.proceed();
        } finally {
            DataScopeHolder.clear();
        }
    }

}
