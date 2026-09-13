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

package com.devops00.spectra.core.user.service;

import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.OnlineUserPageFrom;
import com.devops00.spectra.core.user.javabean.vo.OnlineUserPageVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在线会话合并、按用户筛选和分页的回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class OnlineUserPageAssemblerTest {

    private static final UUID ALICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID MISSING_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ALICE_DEPARTMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ALICE_CHILD_DEPARTMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID BOB_DEPARTMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @Test
    void shouldGroupSessionsSortByLatestLoginAndPageByUserCount() {
        OnlineUserPageAssembler assembler = new OnlineUserPageAssembler();
        List<UserOnlineVO> sessions = List.of(
                session(ALICE_ID, "alice", "alice-old", 1_700_000_000_000L),
                session(BOB_ID, "bob", "bob-current", 1_700_000_003_000L),
                session(ALICE_ID, "alice", "alice-current", 1_700_000_002_000L),
                session(MISSING_USER_ID, "deleted", "orphan-handle", 1_700_000_004_000L));
        List<User> users = List.of(user(ALICE_ID, "alice", "Alice", ALICE_DEPARTMENT_ID),
                user(BOB_ID, "bob", "Bob", BOB_DEPARTMENT_ID));

        var firstPage = assembler.page(page(1, 1), new OnlineUserPageFrom(), sessions, users, Set.of());
        var secondPage = assembler.page(page(2, 1), new OnlineUserPageFrom(), sessions, users, Set.of());

        assertThat(firstPage.getTotal()).isEqualTo(2);
        assertThat(firstPage.getRecords()).extracting(OnlineUserPageVO::getUsername).containsExactly("bob");
        assertThat(firstPage.getRecords().getFirst().getSessions()).extracting(session -> session.getSessionId())
                .containsExactly("bob-current");
        assertThat(secondPage.getTotal()).isEqualTo(2);
        assertThat(secondPage.getRecords()).extracting(OnlineUserPageVO::getUsername).containsExactly("alice");
        assertThat(secondPage.getRecords().getFirst().getSessionCount()).isEqualTo(2);
        assertThat(secondPage.getRecords().getFirst().getLatestLoginTime())
                .isEqualTo(LocalDateTime.of(2023, 11, 14, 22, 13, 22));
        assertThat(secondPage.getRecords().getFirst().getSessions()).extracting(session -> session.getSessionId())
                .containsExactly("alice-current", "alice-old");
    }

    @Test
    void shouldFilterByUsernameAndDepartmentDescendantsBeforePaging() {
        OnlineUserPageFrom filter = new OnlineUserPageFrom();
        filter.setUsername("ali");
        filter.setDepartmentId(ALICE_DEPARTMENT_ID);
        OnlineUserPageAssembler assembler = new OnlineUserPageAssembler();
        List<UserOnlineVO> sessions = List.of(
                session(ALICE_ID, "alice", "alice-current", 1_700_000_002_000L),
                session(BOB_ID, "bob", "bob-current", 1_700_000_003_000L));
        List<User> users = List.of(user(ALICE_ID, "alice", "Alice Example", ALICE_CHILD_DEPARTMENT_ID),
                user(BOB_ID, "bob", "Bob Example", BOB_DEPARTMENT_ID));

        var result = assembler.page(page(1, 15), filter, sessions, users,
                Set.of(ALICE_DEPARTMENT_ID, ALICE_CHILD_DEPARTMENT_ID));

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).extracting(OnlineUserPageVO::getUserId).containsExactly(ALICE_ID);
    }

    private static PageFrom page(long pageNum, long pageSize) {
        PageFrom page = new PageFrom();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        return page;
    }

    private static User user(UUID id, String username, String realName, UUID departmentId) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setDepartmentId(departmentId);
        return user;
    }

    private static UserOnlineVO session(UUID userId, String username, String sessionId, long loginEpochMillis) {
        return UserOnlineVO.builder()
                .sessionId(sessionId)
                .userId(userId.toString())
                .username(username)
                .clientType("web")
                .ip("127.0.0.1")
                .loginTime(LocalDateTime.of(2023, 11, 14, 22, 13, 20)
                        .plusSeconds((loginEpochMillis - 1_700_000_000_000L) / 1000))
                .build();
    }
}
