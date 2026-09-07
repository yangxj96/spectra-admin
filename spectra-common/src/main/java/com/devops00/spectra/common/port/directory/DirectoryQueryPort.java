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

package com.devops00.spectra.common.port.directory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 为业务模块提供只读目录快照的稳定端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
public interface DirectoryQueryPort {

    /**
     * 查询目录中的全部用户。
     *
     * @return 用户快照列表，永不返回 {@code null}
     */
    List<DirectoryUserSnapshot> listUsers();

    /**
     * 查询指定主部门下的全部用户。
     *
     * @param departmentId 主部门 ID
     * @return 用户快照列表，永不返回 {@code null}
     */
    List<DirectoryUserSnapshot> findUsersByDepartmentId(UUID departmentId);

    /**
     * 批量查询用户。
     *
     * @param userIds 用户 ID 集合
     * @return 用户快照列表，永不返回 {@code null}
     */
    List<DirectoryUserSnapshot> findUsersByIds(Collection<UUID> userIds);

    /**
     * 批量查询部门。
     *
     * @param departmentIds 部门 ID 集合
     * @return 部门快照列表，永不返回 {@code null}
     */
    List<DirectoryDepartmentSnapshot> findDepartmentsByIds(Collection<UUID> departmentIds);

    /**
     * 查询全部部门。
     *
     * @return 部门快照列表，永不返回 {@code null}
     */
    List<DirectoryDepartmentSnapshot> listDepartments();

    /**
     * 批量查询用户当前有效联系方式。
     *
     * @param userIds 用户 ID 集合
     * @return 按用户 ID 分组的联系方式快照，空结果返回空 Map
     */
    Map<UUID, List<DirectoryContactSnapshot>> findActiveContactsByUserIds(Collection<UUID> userIds);
}
