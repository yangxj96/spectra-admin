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

package com.devops00.spectra.launch.security;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 仅在 {@code spectra.test.session-http=true} 时装载的合成会话签发入口。
 *
 * <p>用于在两个真实应用进程上驱动一次新的会话签发，以验证共享 Redis 的并发配额。该控制器只存在于测试类路径，
 * 生产构建不包含，且需要有效认证身份才能到达。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@RestController
@ConditionalOnProperty(name = "spectra.test.session-http", havingValue = "true")
class SyntheticSessionController {

    private final SecuritySessionIssuer issuer;

    private final SecurityUserLoader securityUserLoader;

    private final UUID userId;

    SyntheticSessionController(SecuritySessionIssuer issuer, SecurityUserLoader securityUserLoader,
                               @Value("${spectra.test.session-probe-user-id}") UUID userId) {
        this.issuer = issuer;
        this.securityUserLoader = securityUserLoader;
        this.userId = userId;
    }

    @PostMapping("/security/test/session/issue")
    void issue() {
        SecurityPrincipal principal = securityUserLoader.load(userId);
        if (principal == null) {
            throw new BadCredentialsException("合成用户不可用");
        }
        issuer.createToken(principal, ClientType.WEB);
    }
}
