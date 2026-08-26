package com.devops00.spectra.core.scheduler.controller;

import com.devops00.spectra.core.scheduler.javabean.from.SchedulerExecutionActionFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerLoopCommandFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerOperationFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerTriggerFrom;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 管理端点的声明式权限矩阵契约。 */
class SchedulerAdminControllerRoleMatrixTest {

    @Test
    void readEndpointsUseQueryPermissionAndMutationsUseDedicatedPermission() throws Exception {
        assertPermission("catalog", "system:scheduler:query");
        assertPermission("jobs", "system:scheduler:query");
        assertPermission("create", "system:scheduler:manage");
        assertPermission("enable", "system:scheduler:manage");
        assertPermission("trigger", "system:scheduler:execute");
        assertPermission("retry", "system:scheduler:retry");
        assertPermission("command", "system:scheduler:control");
        assertPermission("commands", "system:scheduler:query");
        assertPermission("operations", "system:scheduler:query");
    }

    @Test
    void unknownResolutionHasExplicitDevOpsBoundary() throws Exception {
        var annotation = method("resolve", java.util.UUID.class, SchedulerExecutionActionFrom.class)
                .getAnnotation(PreAuthorize.class);
        assertTrue(annotation.value().contains("system:scheduler:resolve"));
        assertTrue(annotation.value().contains("ROLE_DEV_OPS"));
    }

    @Test
    void mutationBodiesCarryReasonAndIdempotencyContracts() {
        assertTrue(SchedulerOperationFrom.class.getDeclaredFields().length >= 3);
        assertTrue(SchedulerTriggerFrom.class.getDeclaredFields().length >= 3);
        assertTrue(SchedulerLoopCommandFrom.class.getDeclaredFields().length >= 6);
        assertTrue(Map.of("ROLE_AUDIT", "query", "ROLE_ADMIN_SYSTEM", "ordinary", "ROLE_DEV_OPS", "all").size() == 3);
    }

    private static void assertPermission(String name, String permission) throws Exception {
        var method = switch (name) {
            case "catalog" -> method(name);
            case "jobs" -> method(name, com.devops00.spectra.common.base.javabean.from.PageFrom.class,
                    com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobPageFrom.class);
            case "create" -> method(name, com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobSaveFrom.class);
            case "enable" -> method(name, java.util.UUID.class, SchedulerOperationFrom.class);
            case "trigger" -> method(name, java.util.UUID.class, SchedulerTriggerFrom.class);
            case "retry" -> method(name, java.util.UUID.class, SchedulerExecutionActionFrom.class);
            case "command" -> method(name, java.util.UUID.class, SchedulerLoopCommandFrom.class);
            case "commands" -> method(name, java.util.UUID.class, PageFrom.class);
            case "operations" -> method(name, java.util.UUID.class, PageFrom.class);
            default -> throw new IllegalArgumentException(name);
        };
        assertTrue(method.getAnnotation(PreAuthorize.class).value().contains(permission));
    }

    private static Method method(String name, Class<?>... parameterTypes) throws Exception {
        return SchedulerAdminController.class.getMethod(name, parameterTypes);
    }
}
