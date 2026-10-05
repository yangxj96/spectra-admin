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

package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.service.UserDepartmentMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * 用户关联部门关系服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/27
 */
@Service
@RequiredArgsConstructor
public class UserDepartmentMembershipServiceImpl implements UserDepartmentMembershipService {

    private final DepartmentMapper departmentMapper;
    private final UserDepartmentMembershipMapper membershipMapper;

    @Override
    @Transactional
    public void replace(UUID userId, UUID primaryDepartmentId, List<UUID> associatedDepartmentIds, UUID operatorId) {
        if (userId == null || primaryDepartmentId == null) {
            throw new DataException("用户和主部门不能为空");
        }

        List<UUID> requested = associatedDepartmentIds == null ? List.of() : associatedDepartmentIds;
        validateRequested(primaryDepartmentId, requested);
        validateDepartmentsExist(primaryDepartmentId, requested);

        membershipMapper.softDeleteActiveByUserId(userId, operatorId);
        if (!requested.isEmpty()
                && membershipMapper.batchInsertAssociated(userId, requested, operatorId) != requested.size()) {
            throw new DataException("替换用户关联部门失败");
        }
    }

    private void validateRequested(UUID primaryDepartmentId, List<UUID> requested) {
        var uniqueIds = new HashSet<UUID>();
        for (var departmentId : requested) {
            if (departmentId == null)
                throw new DataException("关联部门不能为空");
            if (!uniqueIds.add(departmentId))
                throw new DataException("关联部门不能重复");
            if (primaryDepartmentId.equals(departmentId))
                throw new DataException("主部门不能重复添加为关联部门");
        }
    }

    private void validateDepartmentsExist(UUID primaryDepartmentId, List<UUID> requested) {
        var allDepartmentIds = new ArrayList<UUID>(requested.size() + 1);
        allDepartmentIds.add(primaryDepartmentId);
        allDepartmentIds.addAll(requested);
        if (departmentMapper.selectActiveIdsByIds(allDepartmentIds).size() != allDepartmentIds.size()) {
            throw new DataException("主部门或关联部门不存在或已删除");
        }
    }

    @Override
    public List<UUID> findAssociatedDepartmentIds(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return List.copyOf(membershipMapper.selectActiveDepartmentIdsByUserId(userId));
    }
}
