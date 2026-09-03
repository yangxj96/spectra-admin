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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.core.security.authorization.service.GrantBoundaryService;
import com.devops00.spectra.core.security.authorization.AuthorizationGrantRequest;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.core.security.authorization.GrantBoundaryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Grant Boundary 默认实现；不在业务 Controller 中复制权限组合逻辑。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Slf4j
@Service
public class GrantBoundaryServiceImpl implements GrantBoundaryService {

    @Override
    public void evaluate(AuthorizationSnapshot operatorSnapshot,
                         UUID operatorId,
                         UUID targetUserId,
                         List<AuthorizationGrantRequest> requests,
                         boolean root) {
        GrantBoundaryPolicy.assertAllowed(operatorSnapshot, operatorId, targetUserId, requests, root);
        log.debug("Grant Boundary 校验通过: operatorId={}, targetUserId={}, permissionCount={}, root={}",
                operatorId, targetUserId, requests == null ? 0 : requests.size(), root);
    }
}
