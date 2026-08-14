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

package com.devops00.spectra.core.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationIdentifierHashTest {

    @Test
    void shouldNormalizeIdentifierBeforeHashing() {
        assertEquals(AuthenticationIdentifierHash.digest(" Alice@example.com "),
                AuthenticationIdentifierHash.digest("alice@EXAMPLE.COM"));
        assertNotEquals(AuthenticationIdentifierHash.digest("alice@example.com"),
                AuthenticationIdentifierHash.digest("bob@example.com"));
    }

    @Test
    void shouldRejectBlankIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> AuthenticationIdentifierHash.digest(" "));
    }
}
