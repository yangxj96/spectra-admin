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

package com.devops00.spectra.core.security.authorization.service;

import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;

import java.util.List;
import java.util.UUID;

public interface AuthorizationAssignmentQueryService {

    /**
     * 查询用户的 RoleAssignment 历史，保留 Access/Grant Boundary 的 Assignment 绑定；
     * 引用已删除角色的历史记录不参与运行时视图。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合查询条件的用户授权分配列表；无匹配时返回空列表，不返回 null。
     */
    List<AuthorizationAssignmentView> findByUserId(UUID userId);
}
