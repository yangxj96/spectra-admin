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

import java.util.List;
import java.util.UUID;

/**
 * 维护用户的关联部门关系。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/27
 */
public interface UserDepartmentMembershipService {

    /**
     * 原子替换用户的关联部门；主部门只用于冲突校验，仍写入用户记录。
     *
     * @param userId                  用户 ID
     * @param primaryDepartmentId     用户主部门 ID
     * @param associatedDepartmentIds 关联部门 ID 列表；null 按空列表处理
     * @param operatorId              执行本次变更的操作人 ID
     */
    void replace(UUID userId, UUID primaryDepartmentId, List<UUID> associatedDepartmentIds, UUID operatorId);

    /**
     * 查询用户当前有效的关联部门 ID。
     *
     * @param userId 用户 ID
     * @return 当前有效的关联部门 ID
     */
    List<UUID> findAssociatedDepartmentIds(UUID userId);
}
