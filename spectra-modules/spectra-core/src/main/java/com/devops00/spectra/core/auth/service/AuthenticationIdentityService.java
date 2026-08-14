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

import com.devops00.spectra.core.auth.javabean.entity.AuthenticationIdentity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface AuthenticationIdentityService {

    @Nullable
    AuthenticationIdentity findPasswordIdentity(String identifier);

    AuthenticationIdentity createPasswordIdentity(UUID userId, String identifier);

    void updatePasswordIdentifier(UUID userId, String identifier);

    void revokeByUserId(UUID userId);
}
