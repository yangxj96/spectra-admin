package io.github.yangxj96.spectra.core.auth.configure.datascope;

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


    @Around("@annotation(dataScope)")
    public Object intercept(ProceedingJoinPoint pjp, DataScope dataScope) throws Throwable {
        try {
            DataScopeContext.set(dataScope.filter());
            return pjp.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }
}
