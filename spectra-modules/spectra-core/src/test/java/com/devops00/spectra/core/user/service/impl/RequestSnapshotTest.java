package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.service.NotificationTaskBatchPlanner;
import com.devops00.spectra.core.security.authorization.AuthorizationDepartmentScope;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.user.javabean.entity.UserImportRow;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestSnapshotTest {

    @Test
    void notificationPlanRetainsTargetsAndTemplatesWhenInputsChange() {
        var target = new NotificationTaskBatchPlanner.TaskTarget(null, NotificationChannel.EMAIL, "test@example.invalid");
        var targets = new ArrayList<>(List.of(target));
        var templates = new HashMap<NotificationChannel, NotificationTaskBatchPlanner.TemplateSnapshot>();
        templates.put(NotificationChannel.EMAIL,
                new NotificationTaskBatchPlanner.TemplateSnapshot(UUID.randomUUID(), 1, "digest", null, "title", "body"));
        var request = new NotificationTaskBatchPlanner.PlanRequest(null, UUID.randomUUID(), Instant.EPOCH, null,
                targets, templates);

        targets.clear();
        templates.clear();

        assertEquals(List.of(target), request.targets());
        assertEquals(1, request.templateSnapshots().size());
        assertThrows(UnsupportedOperationException.class, () -> request.targets().clear());
        assertThrows(NullPointerException.class, () -> new NotificationTaskBatchPlanner.PlanRequest(
                null, UUID.randomUUID(), Instant.EPOCH, null, null, Map.of()));
    }

    @Test
    void authorizationQueryRetainsDepartmentSelectionAndNullCompatibility() {
        var departmentId = UUID.randomUUID();
        var ids = new HashSet<>(Set.of(departmentId));
        var departments = new ArrayList<Department>();
        departments.add(null);
        var query = new AuthorizationDepartmentScope.UserAccessQuery(null, "user:read", null, null, ids, departments);

        ids.clear();
        departments.clear();

        assertEquals(Set.of(departmentId), query.targetDepartmentIds());
        assertEquals(1, query.departments().size());
        assertNull(query.departments().getFirst());
        assertThrows(UnsupportedOperationException.class, () -> query.departments().clear());
        var absent = new AuthorizationDepartmentScope.UserAccessQuery(null, "user:read", null, null, null, null);
        assertNull(absent.targetDepartmentIds());
        assertNull(absent.departments());
    }

    @Test
    void importRequestsKeepCollectionShapeAndRowIdentity() {
        var row = new UserImportRow();
        var rows = new ArrayList<>(List.of(row));
        var chunk = new UserImportExecutionWorker.ChunkRequest(UUID.randomUUID(), UUID.randomUUID(), rows,
                false, null, null);
        rows.clear();
        assertEquals(List.of(row), chunk.rows());
        assertSame(row, chunk.rows().getFirst());
        assertThrows(UnsupportedOperationException.class, () -> chunk.rows().clear());

        var departmentId = UUID.randomUUID();
        var ids = new HashMap<String, UUID>();
        ids.put("D1", departmentId);
        var profiles = new HashMap<String, com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO>();
        var departments = new ArrayList<Department>();
        departments.add(new Department());
        var process = new UserImportRowProcessor.ProcessRequest(UUID.randomUUID(), false, ids, profiles,
                departments, null);
        ids.clear();
        departments.clear();

        assertEquals(departmentId, process.departmentIds().get("D1"));
        assertEquals(1, process.departments().size());
        assertThrows(UnsupportedOperationException.class, () -> process.departmentIds().clear());
    }
}
