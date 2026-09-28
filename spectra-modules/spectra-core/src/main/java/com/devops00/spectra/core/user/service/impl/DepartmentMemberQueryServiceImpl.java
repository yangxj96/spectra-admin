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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.core.security.authorization.AuthorizationDepartmentScope;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.javabean.from.DepartmentMemberPageFrom;
import com.devops00.spectra.core.user.javabean.vo.DepartmentMemberCandidateVO;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devops00.spectra.core.user.service.DepartmentMemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门直属成员查询服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Service
@RequiredArgsConstructor
public class DepartmentMemberQueryServiceImpl implements DepartmentMemberQueryService {

    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;
    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public IPage<DepartmentMemberCandidateVO> page(DepartmentMemberPageFrom from) {
        var activeIds = departmentMapper.selectActiveIdsByIds(List.of(from.getDepartmentId()));
        if (activeIds.size() != 1 || !activeIds.contains(from.getDepartmentId())) {
            throw new DataNotExistException("部门不存在或已删除");
        }
        var viewerId = securityContextAccessor.currentUserId();
        var authorization = viewerId == null ? null : authorizationSnapshotProvider.load(viewerId);
        var departments = departmentMapper.selectList(new QueryWrapper<Department>().select("id", "pid"));
        if (!AuthorizationDepartmentScope.canAccessDepartment(
                    authorization, "department:read", from.getDepartmentId(), departments)
                || !AuthorizationDepartmentScope.canAccessDepartment(
                    authorization, "user:read", from.getDepartmentId(), departments)) {
            throw new DataNotExistException("部门不存在或无权访问");
        }
        var keyword = from.getKeyword() == null || from.getKeyword().isBlank()
                ? null
                : from.getKeyword().trim();
        return userMapper.selectDepartmentMemberCandidates(from.toPage(), from.getDepartmentId(), keyword);
    }
}
