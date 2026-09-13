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

package com.devops00.spectra.core.security.authorization.javabean.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 封装授权分配视图相关的响应数据。
 *
 * @param assignmentId        分配标识
 * @param userId              用户标识
 * @param roleId              角色标识
 * @param roleCode            角色编码
 * @param roleKind            角色所属的角色类型
 * @param roleName            被分配角色的展示名称
 * @param roleSystemManaged   角色系统
 * @param roleState           角色状态
 * @param roleVersion         角色版本
 * @param rolePermissionCount 分配角色包含的权限数量
 * @param version             当前对象或配置的版本号
 * @param state               当前对象所处的业务状态
 * @param validFrom           授权关系的生效时间
 * @param validUntil          授权关系的失效时间
 * @param accessBoundaries    访问边界
 * @param grantBoundaries     该授权关系允许授予的权限边界集合
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record AuthorizationAssignmentView(UUID assignmentId,
                                          UUID userId,
                                          UUID roleId,
                                          String roleCode,
                                          String roleKind,
                                          String roleName,
                                          Boolean roleSystemManaged,
                                          String roleState,
                                          Long roleVersion,
                                          Long rolePermissionCount,
                                          Long version,
                                          String state,
                                          LocalDateTime validFrom,
                                          LocalDateTime validUntil,
                                          List<AuthorizationBoundaryView> accessBoundaries,
                                          List<AuthorizationBoundaryView> grantBoundaries) {

    public AuthorizationAssignmentView {
        accessBoundaries = immutableList(accessBoundaries);
        grantBoundaries = immutableList(grantBoundaries);
    }

    @Override
    public List<AuthorizationBoundaryView> accessBoundaries() {
        return immutableList(accessBoundaries);
    }

    @Override
    public List<AuthorizationBoundaryView> grantBoundaries() {
        return immutableList(grantBoundaries);
    }

    /**
     * 转换、解析或规范化数据（{@code immutableList}）。
     */
    private static <T> List<T> immutableList(List<T> source) {
        return source == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(source));
    }
}
