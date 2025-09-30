package io.github.yangxj96.spectra.core.user.listener;

import io.github.yangxj96.spectra.core.user.javabean.event.RoleDeletedEvent;
import io.github.yangxj96.spectra.core.user.service.RelRoleAuthorityService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 角色删除事件
 * <p>角色-权限关联处理</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDeletionOnRoleAuthorityCleanupListener {

    @Resource
    private RelRoleAuthorityService relRoleAuthorityService;

    @TransactionalEventListener(fallbackExecution = true)
    public void handleRoleDeleted(RoleDeletedEvent event) {
        log.atDebug().log("角色删除事件监听-角色权限关联关系:{}", event.roleId());
        relRoleAuthorityService.revoke(event.roleId());
    }

}
