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

package com.devops00.spectra.core.directory;

import com.devops00.spectra.common.port.directory.DirectoryContactSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.common.port.directory.DirectoryUserSnapshot;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.service.DepartmentDirectoryReadService;
import com.devops00.spectra.core.user.service.UserDirectoryReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core 对目录端口的实现，将核心实体转换为不可变快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
@Component
@RequiredArgsConstructor
public class DirectoryQueryAdapter implements DirectoryQueryPort {

    private final UserDirectoryReadService userDirectoryReadService;

    private final DepartmentDirectoryReadService departmentDirectoryReadService;

    private final UserContactService userContactService;

    @Override
    public List<DirectoryUserSnapshot> listUsers() {
        return toUserSnapshots(userDirectoryReadService.listAll());
    }

    @Override
    public List<DirectoryUserSnapshot> findUsersByDepartmentId(UUID departmentId) {
        var userIds = userDirectoryReadService.findUserIdsByDepartmentId(departmentId);
        return toUserSnapshots(userDirectoryReadService.findByIds(userIds));
    }

    @Override
    public List<UUID> findDepartmentIdsByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return userDirectoryReadService.findDepartmentIdsByUserIds(List.of(userId))
                .getOrDefault(userId, Set.of())
                .stream()
                .toList();
    }

    @Override
    public List<DirectoryUserSnapshot> findUsersByIds(Collection<UUID> userIds) {
        return toUserSnapshots(userDirectoryReadService.findByIds(userIds));
    }

    @Override
    public List<DirectoryDepartmentSnapshot> findDepartmentsByIds(Collection<UUID> departmentIds) {
        return departmentDirectoryReadService.findActiveByIds(departmentIds)
                .stream()
                .map(DirectoryQueryAdapter::toDepartmentSnapshot)
                .toList();
    }

    @Override
    public List<DirectoryDepartmentSnapshot> listDepartments() {
        return departmentDirectoryReadService.listAll()
                .stream()
                .map(DirectoryQueryAdapter::toDepartmentSnapshot)
                .toList();
    }

    @Override
    public Map<UUID, List<DirectoryContactSnapshot>> findActiveContactsByUserIds(Collection<UUID> userIds) {
        var distinctIds = distinctIds(userIds);
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        var contactsByUserId = userContactService.listActiveByUserIds(distinctIds);
        if (contactsByUserId == null || contactsByUserId.isEmpty()) {
            return Map.of();
        }
        var result = new LinkedHashMap<UUID, List<DirectoryContactSnapshot>>();
        contactsByUserId.forEach((userId, contacts) -> {
            if (userId == null || contacts == null || contacts.isEmpty()) {
                return;
            }
            var snapshots = contacts.stream()
                    .filter(Objects::nonNull)
                    .map(DirectoryQueryAdapter::toContactSnapshot)
                    .toList();
            if (!snapshots.isEmpty()) {
                result.put(userId, snapshots);
            }
        });
        return result.isEmpty() ? Map.of() : Map.copyOf(result);
    }

    private List<DirectoryUserSnapshot> toUserSnapshots(List<UserDirectoryReadService.UserDirectoryRow> users) {
        var records = users == null ? List.<UserDirectoryReadService.UserDirectoryRow>of() : users;
        if (records.isEmpty()) {
            return List.of();
        }
        var userIds = records.stream()
                .map(UserDirectoryReadService.UserDirectoryRow::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        var departmentIdsByUser = userDirectoryReadService.findDepartmentIdsByUserIds(userIds);
        var allDepartmentIds = departmentIdsByUser.values()
                .stream()
                .flatMap(Set::stream)
                .distinct()
                .toList();
        var departmentsById = departmentDirectoryReadService.findActiveByIds(allDepartmentIds)
                .stream()
                .collect(Collectors.toMap(DepartmentDirectoryReadService.DepartmentDirectoryRow::id,
                        DirectoryQueryAdapter::toDepartmentSnapshot, (left, right) -> left));
        return records.stream()
                .map(user -> new DirectoryUserSnapshot(
                        user.id(),
                        user.employeeNo(),
                        user.realName(),
                        user.username(),
                        user.avatar(),
                        user.status(),
                        user.primaryDepartmentId(),
                        departmentIdsByUser.getOrDefault(user.id(), Set.of())
                                .stream()
                                .map(departmentsById::get)
                                .filter(Objects::nonNull)
                                .toList()))
                .toList();
    }

    private static List<UUID> distinctIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static DirectoryDepartmentSnapshot toDepartmentSnapshot(
                                                                    DepartmentDirectoryReadService.DepartmentDirectoryRow department) {
        return new DirectoryDepartmentSnapshot(
                department.id(), department.pid(), department.name(), department.path());
    }

    private static DirectoryContactSnapshot toContactSnapshot(UserContact contact) {
        return new DirectoryContactSnapshot(contact.getContactType(), contact.getContactValue());
    }
}
