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
import com.devops00.spectra.core.user.javabean.entity.RelUserRole;
import com.devops00.spectra.core.user.javabean.event.RoleDeletedEvent;
import com.devops00.spectra.core.user.service.RelUserRoleService;
import com.devops00.spectra.core.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.UUID;

/**
 * 角色删除事件
 * <p>
 * 用户-角色关联处理
 * </p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDeletionOnUserRoleCleanupListener {

    private final RoleService roleService;

    private final RelUserRoleService relUserRoleService;

    /**
     * 角色删除事件监听器
     * <p>
     * 撤销关联的用户
     * </p>
     *
     * @param event 角色删除事件实体
     */
    @TransactionalEventListener(fallbackExecution = true)
    public void handleRoleDeleted(RoleDeletedEvent event) {
        log.debug("{}角色删除事件监听-用户角色关联关系:{}", LogPrefix.CORE.p(), event.roleId());
        // 获取保底角色
        var defaultRole = roleService.getSystemDefaultUserRole();
        // 查询所有有这个角色的用户,
        var relUserRoles = relUserRoleService.getRelByRoleId(event.roleId());
        if (relUserRoles.isEmpty()) {
            return;
        }
        // 如果只有这一个角色的,则移除这个角色关联关系,新增一个保底角色的关联关系,保证正常登录
        // 如果有多个角色,则删除这个角色的关联关系即可
        // 获取哪些用户有这个角色
        var userIds = relUserRoles.stream().map(RelUserRole::getUserId).distinct().toList();
        // 循环查询这个用户的角色进行处理
        for (UUID userId : userIds) {
            var roles = relUserRoleService.getRoles(userId);
            // 他只有一个角色的情况,取消了关联就要给他一个默认保底
            if (roles.size() <= 1) {
                relUserRoleService.grant(userId, Collections.singletonList(defaultRole.getId()));
            }
            // 取消关联
            relUserRoleService.revoke(userId, Collections.singletonList(event.roleId()));
        }
    }
}
