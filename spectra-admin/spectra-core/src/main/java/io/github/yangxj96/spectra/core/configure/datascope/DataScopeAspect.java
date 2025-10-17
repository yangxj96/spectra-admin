/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.yangxj96.spectra.core.configure.datascope;

import io.github.yangxj96.spectra.core.service.auth.SecurityService;
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
