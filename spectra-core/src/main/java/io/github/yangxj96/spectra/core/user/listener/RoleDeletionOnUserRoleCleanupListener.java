package io.github.yangxj96.spectra.core.user.listener;

import io.github.yangxj96.spectra.core.user.javabean.entity.RelUserRole;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import io.github.yangxj96.spectra.core.user.javabean.event.RoleDeletedEvent;
import io.github.yangxj96.spectra.core.user.service.RelUserRoleService;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import io.github.yangxj96.spectra.core.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色删除事件
 * <p>用户-角色关联处理</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDeletionOnUserRoleCleanupListener {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private RelUserRoleService relUserRoleService;

    /**
     * 角色删除事件监听器
     * <p>撤销关联的用户</p>
     *
     * @param event 角色删除事件实体
     */
    @TransactionalEventListener(fallbackExecution = true)
    public void handleRoleDeleted(RoleDeletedEvent event) {
        log.atInfo().log("角色删除事件监听-用户角色关联关系:{}", event.roleId());
        // 获取保底角色
        Role defaultRole = roleService.getSystemDefaultUserRole();
        // 查询所有有这个角色的用户,
        List<RelUserRole> relUserRoles = relUserRoleService.getRelByRoleId(event.roleId());
        if (relUserRoles.isEmpty()) {
            return;
        }
        // 如果只有这一个角色的,则移除这个角色关联关系,新增一个保底角色的关联关系,保证正常登录
        // 如果有多个角色,则删除这个角色的关联关系即可
        // 获取哪些用户有这个角色
        List<Long> userIds = relUserRoles.stream().map(RelUserRole::getUserId).distinct().toList();
        // 循环查询这个用户的角色进行处理
        for (Long userId : userIds) {
            List<Role> roles = relUserRoleService.getRoles(userId);
            // 他只有一个角色的情况,取消了关联就要给他一个默认保底
            if (roles.size() <= 1) {
                relUserRoleService.grant(userId, Collections.singletonList(defaultRole.getId()));
            }
            // 取消关联
            relUserRoleService.revoke(userId, Collections.singletonList(event.roleId()));
        }
    }

}
