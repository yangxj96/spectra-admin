/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.service;

import java.util.Collection;
import java.util.UUID;

/**
 * 预览并重写部门合并涉及的授权部门引用。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
public interface DepartmentAuthorizationReferenceService {

    /**
     * 计算部门合并对授权 Assignment 和方案的影响。
     *
     * @param sourceDepartmentIds   源部门 ID
     * @param sourceDepartmentCodes 源部门编码
     * @param targetDepartmentId    新部门 ID
     * @param targetDepartmentCode  新部门编码
     * @return 授权影响摘要
     */
    DepartmentAuthorizationReferenceImpact previewMerge(Collection<UUID> sourceDepartmentIds,
                                                        Collection<String> sourceDepartmentCodes,
                                                        UUID targetDepartmentId,
                                                        String targetDepartmentCode);

    /**
     * 将活动 Assignment 与未删除授权方案中的来源部门引用改写到合并部门。
     *
     * @param sourceDepartmentIds   源部门 ID
     * @param sourceDepartmentCodes 源部门编码
     * @param targetDepartmentId    新部门 ID
     * @param targetDepartmentCode  新部门编码
     * @param operatorId            当前操作者
     * @return 实际重写结果
     */
    DepartmentAuthorizationReferenceImpact applyMerge(Collection<UUID> sourceDepartmentIds,
                                                      Collection<String> sourceDepartmentCodes,
                                                      UUID targetDepartmentId,
                                                      String targetDepartmentCode,
                                                      UUID operatorId);
}
