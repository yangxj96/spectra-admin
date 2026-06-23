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

package com.devops00.spectra.core.user.listener.user;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.user.javabean.event.RoleDeletedEvent;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/// 角色删除事件
/// >角色-菜单关联处理
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDeletionOnRoleMenuCleanupListener {

    private final RelRoleMenuService relRoleMenuService;


    @TransactionalEventListener(fallbackExecution = true)
    public void handleRoleDeleted(RoleDeletedEvent event) {
        log.debug("{}角色删除事件监听-角色菜单关联关系:{}", LogPrefix.CORE.p(), event.roleId());
        relRoleMenuService.revoke(event.roleId());
    }

}
