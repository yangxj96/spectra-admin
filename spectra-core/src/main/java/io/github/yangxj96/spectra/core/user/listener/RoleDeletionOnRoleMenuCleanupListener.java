package io.github.yangxj96.spectra.core.user.listener;

import io.github.yangxj96.spectra.core.user.javabean.event.RoleDeletedEvent;
import io.github.yangxj96.spectra.core.user.service.RelRoleMenuService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 角色删除事件
 * <p>角色-菜单关联处理</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDeletionOnRoleMenuCleanupListener {

    @Resource
    private RelRoleMenuService relRoleMenuService;

    @TransactionalEventListener(fallbackExecution = true)
    public void handleRoleDeleted(RoleDeletedEvent event) {
        log.atInfo().log("角色删除事件监听-角色菜单关联关系:{}", event.roleId());
        relRoleMenuService.revoke(event.roleId());
    }

}
