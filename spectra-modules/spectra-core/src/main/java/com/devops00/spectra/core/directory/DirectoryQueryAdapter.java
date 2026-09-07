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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.port.directory.DirectoryContactSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.common.port.directory.DirectoryUserSnapshot;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Core 对目录端口的实现，将核心实体转换为不可变快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
@Component
public class DirectoryQueryAdapter implements DirectoryQueryPort {

    private final UserMapper userMapper;

    private final DepartmentMapper departmentMapper;

    private final UserContactService userContactService;

    /**
     * 创建目录查询适配器。
     *
     * @param userMapper         用户 Mapper
     * @param departmentMapper   部门 Mapper
     * @param userContactService 联系方式服务
     */
    public DirectoryQueryAdapter(UserMapper userMapper,
                                 DepartmentMapper departmentMapper,
                                 UserContactService userContactService) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.userContactService = userContactService;
    }

    @Override
    public List<DirectoryUserSnapshot> listUsers() {
        return userMapper.selectList(null)
                .stream()
                .filter(Objects::nonNull)
                .map(DirectoryQueryAdapter::toUserSnapshot)
                .toList();
    }

    @Override
    public List<DirectoryUserSnapshot> findUsersByDepartmentId(UUID departmentId) {
        if (departmentId == null) {
            return List.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getDepartmentId, departmentId))
                .stream()
                .filter(Objects::nonNull)
                .map(DirectoryQueryAdapter::toUserSnapshot)
                .toList();
    }

    @Override
    public List<DirectoryUserSnapshot> findUsersByIds(Collection<UUID> userIds) {
        var distinctIds = distinctIds(userIds);
        if (distinctIds.isEmpty()) {
            return List.of();
        }
        return userMapper.selectByIds(distinctIds)
                .stream()
                .filter(Objects::nonNull)
                .map(DirectoryQueryAdapter::toUserSnapshot)
                .toList();
    }

    @Override
    public List<DirectoryDepartmentSnapshot> findDepartmentsByIds(Collection<UUID> departmentIds) {
        var distinctIds = distinctIds(departmentIds);
        if (distinctIds.isEmpty()) {
            return List.of();
        }
        return departmentMapper.selectByIds(distinctIds)
                .stream()
                .filter(Objects::nonNull)
                .map(DirectoryQueryAdapter::toDepartmentSnapshot)
                .toList();
    }

    @Override
    public List<DirectoryDepartmentSnapshot> listDepartments() {
        return departmentMapper.selectList(null)
                .stream()
                .filter(Objects::nonNull)
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

    private static List<UUID> distinctIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static DirectoryUserSnapshot toUserSnapshot(User user) {
        return new DirectoryUserSnapshot(
                user.getId(),
                user.getEmployeeNo(),
                user.getRealName(),
                user.getUsername(),
                user.getAvatar(),
                user.getStatus() == null ? null : user.getStatus().name(),
                user.getDepartmentId());
    }

    private static DirectoryDepartmentSnapshot toDepartmentSnapshot(Department department) {
        return new DirectoryDepartmentSnapshot(
                department.getId(), department.getPid(), department.getName(), department.getPath());
    }

    private static DirectoryContactSnapshot toContactSnapshot(UserContact contact) {
        return new DirectoryContactSnapshot(contact.getContactType(), contact.getContactValue());
    }
}
