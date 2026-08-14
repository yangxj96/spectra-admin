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

package com.devops00.spectra.core.security.change;

import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.holder.SecUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Phase 5 v2 Session 接入前的撤销适配器；任何 Redis 失败均继续由 SecUtil fail-closed 处理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Service
public class LegacySecuritySessionRevocationPort implements SecuritySessionRevocationPort {

    @Override
    public void revokeUserSessions(UUID userId) {
        SecUtil.kick(userId);
    }
}
