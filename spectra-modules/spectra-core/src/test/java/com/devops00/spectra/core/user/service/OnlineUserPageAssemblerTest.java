package com.devops00.spectra.core.user.service;

import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.OnlineUserPageFrom;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                new PageFrom(), filter, sessions, List.of(user), Set.of(associatedDepartmentId),
                Map.of(userId, Set.of(primaryDepartmentId, associatedDepartmentId))));

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(2, result.getRecords().getFirst().getSessionCount());
    }

    private static UserOnlineVO session(String sessionId, UUID userId) {
        return UserOnlineVO.builder().sessionId(sessionId).userId(userId.toString()).username("person@example.test").build();
    }
}
