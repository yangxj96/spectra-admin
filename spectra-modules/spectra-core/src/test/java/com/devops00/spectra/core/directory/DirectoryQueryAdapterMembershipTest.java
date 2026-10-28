package com.devops00.spectra.core.directory;

import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.service.DepartmentDirectoryReadService;
import com.devops00.spectra.core.user.service.UserDirectoryReadService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DirectoryQueryAdapterMembershipTest {

    @Test
    void findsAssociatedUsersAndReturnsDepartmentSummaries() {
        var userId = UUID.randomUUID();
        var primaryId = UUID.randomUUID();
        var associatedId = UUID.randomUUID();
        var user = new UserDirectoryReadService.UserDirectoryRow(
                userId, "E001", "测试用户", "tester", null, "ACTIVE", primaryId);
        var department = new DepartmentDirectoryReadService.DepartmentDirectoryRow(
                associatedId, null, "关联部门", "集团/关联部门");
        var userDirectory = mock(UserDirectoryReadService.class);
        var departmentDirectory = mock(DepartmentDirectoryReadService.class);
        var adapter = new DirectoryQueryAdapter(userDirectory, departmentDirectory, mock(UserContactService.class));
        when(userDirectory.findUserIdsByDepartmentId(associatedId)).thenReturn(List.of(userId));
        when(userDirectory.findByIds(List.of(userId))).thenReturn(List.of(user));
        when(userDirectory.findDepartmentIdsByUserIds(List.of(userId)))
                .thenReturn(Map.of(userId, Set.of(primaryId, associatedId)));
        when(departmentDirectory.findActiveByIds(any())).thenReturn(List.of(department));

        var result = adapter.findUsersByDepartmentId(associatedId);

        assertEquals(1, result.size());
        assertEquals(primaryId, result.getFirst().primaryDepartmentId());
        assertEquals(List.of(new DirectoryDepartmentSnapshot(associatedId, null, "关联部门", "集团/关联部门")),
                result.getFirst().associatedDepartments());
    }
}
