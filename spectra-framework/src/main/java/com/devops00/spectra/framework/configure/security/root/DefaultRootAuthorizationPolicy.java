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

package com.devops00.spectra.framework.configure.security.root;

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import org.jspecify.annotations.Nullable;

/**
 * 默认 Root 判定实现。
 * <p>
 * Root 角色代码只允许由安全配置提供；该组件不负责跳过审计或数据范围校验。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public class DefaultRootAuthorizationPolicy implements RootAuthorizationPolicy {

    @Override
    public boolean isRoot(@Nullable SecurityPrincipal principal) {
        if (principal == null
                || !principal.isEnabled()
                || !principal.isAccountNonExpired()
                || !principal.isAccountNonLocked()
                || !principal.isCredentialsNonExpired()) {
            return false;
        }
        return principal.getAuthorityNames()
                .stream()
                .anyMatch(RootAuthorizationPolicy.ROOT_ROLE::equals);
    }
}
