/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.common.security;

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.port.security.UserOnlineVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 安全公共契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
class SecurityContractTest {

    @Test
    void principalMustRemainFrameworkAgnostic() {
        assertThat(Arrays.stream(SecurityPrincipal.class.getDeclaredMethods())
                .map(Method::getName)
                .toList())
                .contains("getId", "getUsername", "isEnabled", "getAuthorityNames")
                .doesNotContain("getPassword", "getAuthorities");
    }

    @Test
    void tokenAndOnlineViewMustBeCommonContracts() {
        assertThat(Arrays.stream(SecurityToken.class.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> !"serialVersionUID".equals(name))
                .toList())
                .containsExactlyInAnyOrder(
                        "loginType", "id", "username", "accessToken", "refreshToken",
                        "permissions", "passwordChangeRequired");

        assertThat(UserOnlineVO.class.getPackageName())
                .isEqualTo("com.devops00.spectra.common.port.security");
    }
}
