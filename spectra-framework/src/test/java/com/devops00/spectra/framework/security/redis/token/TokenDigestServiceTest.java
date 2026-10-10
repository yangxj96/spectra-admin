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

package com.devops00.spectra.framework.security.redis.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安全 Redis 令牌摘要比较的相等、不同及缺失边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
class TokenDigestServiceTest {

    @Test
    void equalDigestValuesMatchRegardlessOfStringIdentity() {
        String digest = TokenDigestService.digest("synthetic-access-token");
        assertTrue(TokenDigestService.equalDigests(digest, new String(digest.toCharArray())));
        assertFalse(TokenDigestService.equalDigests(digest, TokenDigestService.digest("another-token")));
    }

    @Test
    void missingDigestsNeverMatch() {
        String digest = TokenDigestService.digest("synthetic-access-token");
        assertFalse(TokenDigestService.equalDigests(null, digest));
        assertFalse(TokenDigestService.equalDigests(digest, null));
        assertFalse(TokenDigestService.equalDigests(null, null));
    }
}
