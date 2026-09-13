/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.framework.persistence.scope.context;

import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 受控的数据隔离手动扩展点。
 * <p>
 * 只有系统运维角色或显式通配权限可以临时绕过隔离，调用范围限定在一个
 * lambda 内，避免出现全局开关未恢复的问题。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Component
@RequiredArgsConstructor
public class DataScopeExecutor {

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 在系统运维权限校验通过后执行有返回值的隔离绕过任务。
     *
     * @param action 待在临时关闭数据权限的上下文中执行的受信任内部操作。
     * @return 返回临时关闭数据隔离后由 {@code action} 产生的业务结果；{@code action} 返回 null 时原样返回 null，权限不足或 action 抛异常时不吞掉异常，并保证上下文恢复。
     */
    public <T> T withoutScope(Supplier<T> action) {
        requireSystemOperator();
        return DataScopeContextHolder.withBypass(action);
    }

    /**
     * 在系统运维权限校验通过后执行无返回值的隔离绕过任务。
     *
     * @param action 待在临时关闭数据权限的上下文中执行的受信任内部操作。
     */
    public void withoutScope(Runnable action) {
        requireSystemOperator();
        DataScopeContextHolder.withBypass(action);
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireSystemOperator}）。
     */
    private void requireSystemOperator() {
        var user = securityContextAccessor.currentUser();
        if (user == null
                || user.getAuthorityNames()
                        .stream()
                        .noneMatch(authority -> "ROLE_DEV_OPS".equals(authority) || "*".equals(authority))) {
            throw new DataScopeViolationException("当前用户无权临时绕过数据隔离");
        }
    }
}
