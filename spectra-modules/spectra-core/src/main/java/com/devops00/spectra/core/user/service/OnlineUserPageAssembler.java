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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.from.OnlineUserPageFrom;
import com.devops00.spectra.core.user.javabean.vo.OnlineSessionVO;
import com.devops00.spectra.core.user.javabean.vo.OnlineUserPageVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 将 Redis 在线会话摘要与当前用户资料合并为按用户分页的页面结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
@NullMarked
public class OnlineUserPageAssembler {

    private static final Comparator<UserOnlineVO> SESSION_ORDER = Comparator
            .comparing(UserOnlineVO::getLoginTime, Comparator.nullsLast(Comparator.reverseOrder()));

    private static final Comparator<OnlineUserPageVO> USER_ORDER = Comparator
            .comparing(OnlineUserPageVO::getLatestLoginTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(OnlineUserPageVO::getUsername, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    /**
     * 按当前有效会话构造经过过滤、排序和分页的用户分组。
     *
     * @param page                  页码与页大小。
     * @param filter                账号、姓名和部门筛选条件。
     * @param onlineSessions        安全 Redis 返回的在线会话摘要。
     * @param onlineUsers           当前存在的用户实体资料。
     * @param matchingDepartmentIds 目标部门及其可匹配的下级部门 ID。
     * @return 返回 total 按用户数量计算的 MyBatis-Plus 分页结果；没有匹配在线用户时 records 为空、total 为 0。
     */
    public Page<OnlineUserPageVO> page(PageFrom page, OnlineUserPageFrom filter,
                                       List<UserOnlineVO> onlineSessions, List<User> onlineUsers,
                                       Set<UUID> matchingDepartmentIds) {
        long pageNum = page.getPageNum() == null ? 1L : page.getPageNum();
        long pageSize = page.getPageSize() == null ? 15L : page.getPageSize();
        if (pageNum < 1 || pageSize < 1) {
            throw new IllegalArgumentException("在线用户分页页码和页大小必须大于零");
        }

        Map<UUID, User> usersById = new HashMap<>();
        for (User user : onlineUsers) {
            usersById.put(user.getId(), user);
        }
        Map<UUID, List<UserOnlineVO>> sessionsByUser = new HashMap<>();
        for (UserOnlineVO session : onlineSessions) {
            UUID userId = UUID.fromString(session.getUserId());
            sessionsByUser.computeIfAbsent(userId, ignored -> new ArrayList<>()).add(session);
        }

        List<OnlineUserPageVO> records = new ArrayList<>();
        for (Map.Entry<UUID, List<UserOnlineVO>> entry : sessionsByUser.entrySet()) {
            User user = usersById.get(entry.getKey());
            if (user == null || !matches(user, filter, matchingDepartmentIds)) {
                continue;
            }
            List<UserOnlineVO> sortedSessions = entry.getValue().stream().sorted(SESSION_ORDER).toList();
            UserOnlineVO latestSession = sortedSessions.getFirst();
            List<OnlineSessionVO> sessionViews = sortedSessions.stream()
                    .map(session -> new OnlineSessionVO(session.getSessionId(), session.getClientType(), session.getIp(),
                            session.getLoginTime()))
                    .toList();
            records.add(new OnlineUserPageVO(user.getId(), user.getUsername(), user.getRealName(),
                    user.getDepartmentId(), null, sessionViews.size(), latestSession.getLoginTime(), sessionViews));
        }
        records.sort(USER_ORDER);

        long pageOffset = pageNum - 1;
        long offset = pageOffset > Long.MAX_VALUE / pageSize ? Long.MAX_VALUE : pageOffset * pageSize;
        int fromIndex = offset >= records.size() ? records.size() : Math.toIntExact(offset);
        int available = records.size() - fromIndex;
        int toIndex = pageSize >= available ? records.size() : fromIndex + Math.toIntExact(pageSize);
        Page<OnlineUserPageVO> result = new Page<>(pageNum, pageSize, records.size());
        result.setRecords(records.subList(fromIndex, toIndex));
        return result;
    }

    private static boolean matches(User user, OnlineUserPageFrom filter, Set<UUID> matchingDepartmentIds) {
        if (filter == null) {
            return true;
        }
        if (!containsIgnoreCase(user.getUsername(), filter.getUsername())
                || !containsIgnoreCase(user.getRealName(), filter.getRealName())) {
            return false;
        }
        return filter.getDepartmentId() == null
                || matchingDepartmentIds.contains(user.getDepartmentId());
    }

    private static boolean containsIgnoreCase(String value, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }
}
