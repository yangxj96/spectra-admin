/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.framework.security.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证绑定码发送与核验均命中外层限流策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
class BindingRateLimitPolicyTest {
    @Test
    void bothBindingChannelsHaveUserAndIpScopedSendAndVerifyPolicies() {
        for (String endpoint : new String[]{"/security/authentication/bind/sms",
                "/security/authentication/bind/email"}) {
            var policy = RateLimitPolicy.resolve("POST", endpoint).orElseThrow();
            assertEquals("binding-code-send", policy.name());
            assertEquals(5, policy.maxRequests());
            assertEquals(RateLimitPolicy.SubjectDimension.IP_AND_USER, policy.subjectDimension());
        }
        for (String endpoint : new String[]{"/security/identities/phone", "/security/identities/email"}) {
            var policy = RateLimitPolicy.resolve("POST", endpoint).orElseThrow();
            assertEquals("binding-code-verify", policy.name());
            assertEquals(15, policy.maxRequests());
            assertEquals(RateLimitPolicy.SubjectDimension.IP_AND_USER, policy.subjectDimension());
            assertTrue(RateLimitPolicy.resolve("GET", endpoint).isEmpty());
        }
    }
}
