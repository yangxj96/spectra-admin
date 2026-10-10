package com.devops00.spectra.core.user.service;

import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.OnlineUserPageFrom;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OnlineUserPageAssemblerTest {

    @Test
    void filtersOnlineUsersByAssociatedDepartmentWithoutDuplicatingSessions() {
        var userId = UUID.randomUUID();
        var primaryDepartmentId = UUID.randomUUID();
        var associatedDepartmentId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setUsername("person@example.test");
        user.setPrimaryDepartmentId(primaryDepartmentId);
        var filter = new OnlineUserPageFrom();
        filter.setDepartmentId(associatedDepartmentId);
        var sessions = List.of(session("session-1", userId), session("session-2", userId));

        var result = new OnlineUserPageAssembler().page(new OnlineUserPageAssembler.PageRequest(
                new OnlineUserPageAssembler.PageOptions(new PageFrom(), filter), sessions, List.of(user),
                Set.of(associatedDepartmentId),
                Map.of(userId, Set.of(primaryDepartmentId, associatedDepartmentId))));

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(2, result.getRecords().getFirst().getSessionCount());
    }

    @Test
    void pageRequestKeepsPaginationFilterAndMembershipSnapshots() {
        var userId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var page = new PageFrom();
        page.setPageNum(2L);
        var filter = new OnlineUserPageFrom("original", null, departmentId);
        var sessions = new ArrayList<UserOnlineVO>();
        sessions.add(session("session-1", userId));
        var users = new ArrayList<User>();
        users.add(new User());
        var matching = new HashSet<>(Set.of(departmentId));
        var userDepartments = new HashSet<>(Set.of(departmentId));
        var membership = new HashMap<UUID, Set<UUID>>();
        membership.put(userId, userDepartments);

        var request = new OnlineUserPageAssembler.PageRequest(
                new OnlineUserPageAssembler.PageOptions(page, filter), sessions, users, matching, membership);
        page.setPageNum(3L);
        filter.setUsername("changed");
        sessions.clear();
        users.clear();
        matching.clear();
        userDepartments.clear();
        membership.clear();

        assertEquals(2L, request.options().pageNum());
        assertEquals("original", request.options().username());
        assertEquals(1, request.onlineSessions().size());
        assertEquals(1, request.onlineUsers().size());
        assertEquals(Set.of(departmentId), request.matchingDepartmentIds());
        assertEquals(Set.of(departmentId), request.departmentIdsByUser().get(userId));
        assertThrows(UnsupportedOperationException.class, () -> request.onlineSessions().clear());
    }

    private static UserOnlineVO session(String sessionId, UUID userId) {
        return UserOnlineVO.builder().sessionId(sessionId).userId(userId.toString()).username("person@example.test").build();
    }
}
