package com.devops00.spectra.core.directory;

import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.entity.UserDepartmentMembership;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DirectoryQueryAdapterMembershipTest {

    @Test
    void findsAssociatedUsersAndReturnsDepartmentSummaries() {
        var userId = UUID.randomUUID();
        var primaryId = UUID.randomUUID();
        var associatedId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setPrimaryDepartmentId(primaryId);
        var associated = new UserDepartmentMembership(userId, associatedId);
        var department = new Department();
        department.setId(associatedId);
        department.setName("关联部门");
        department.setPath("集团/关联部门");
        var userMapper = mock(UserMapper.class);
        var departmentMapper = mock(DepartmentMapper.class);
        var membershipMapper = mock(UserDepartmentMembershipMapper.class);
        var adapter = new DirectoryQueryAdapter(userMapper, departmentMapper, mock(UserContactService.class), membershipMapper);
        when(membershipMapper.selectUserIdsByDepartmentIds(List.of(associatedId), null)).thenReturn(List.of(userId));
        when(userMapper.selectByIds(List.of(userId))).thenReturn(List.of(user));
        when(membershipMapper.selectActiveByUserIds(List.of(userId))).thenReturn(List.of(associated));
        when(departmentMapper.selectActiveByIds(List.of(associatedId))).thenReturn(List.of(department));

        var result = adapter.findUsersByDepartmentId(associatedId);

        assertEquals(1, result.size());
        assertEquals(primaryId, result.getFirst().primaryDepartmentId());
        assertEquals(List.of(new DirectoryDepartmentSnapshot(associatedId, null, "关联部门", "集团/关联部门")),
                result.getFirst().associatedDepartments());
    }
}
