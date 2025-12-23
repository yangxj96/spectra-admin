package io.github.yangxj96.spectra.core.configure.datascope;


import io.github.yangxj96.spectra.core.configure.security.holder.SecUtil;
import io.github.yangxj96.spectra.core.service.auth.DataScopeService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 数据范围切面
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/23 11:56
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataScopeAspect {

    private final DataScopeService dataScopeService;

    public DataScopeAspect(DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    @Around(value = "@annotation(dataScope) || @annotation(dataScopeIgnore)", argNames = "pjp,dataScope,dataScopeIgnore")
    public Object around(ProceedingJoinPoint pjp, DataScope dataScope, DataScopeIgnore dataScopeIgnore) throws Throwable {
        // 忽略注解.等级最高
        if (dataScopeIgnore != null) {
            try {
                DataScopeHolder.set(DataScopeContext.builder()
                        .ignore(true)
                        .build());
                return pjp.proceed();
            } finally {
                DataScopeHolder.clear();
            }
        }

        // 没有 DataScope，直接放行
        if (!dataScope.enabled()) {
            return pjp.proceed();
        }

        // 正常 DataScope
        Long userId = SecUtil.getCurrentUserId();
        DataScopeContext context = dataScopeService.resolve(userId);

        try {
            DataScopeHolder.set(context);
            return pjp.proceed();
        } finally {
            DataScopeHolder.clear();
        }
    }

}
