/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.listener.user;

import io.github.yangxj96.spectra.core.javabean.user.event.RoleDeletedEvent;
import io.github.yangxj96.spectra.core.service.user.RelRoleAuthorityService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 角色删除事件
 * <p>角色-权限关联处理</p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDeletionOnRoleAuthorityCleanupListener {

    @Resource
    private RelRoleAuthorityService relRoleAuthorityService;

    @TransactionalEventListener(fallbackExecution = true)
    public void handleRoleDeleted(RoleDeletedEvent event) {
        log.debug("角色删除事件监听-角色权限关联关系:{}", event.roleId());
        relRoleAuthorityService.revoke(event.roleId());
    }

}
