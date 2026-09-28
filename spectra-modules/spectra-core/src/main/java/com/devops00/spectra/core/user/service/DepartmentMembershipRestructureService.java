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

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * 计算并改写部门重组涉及的用户部门关系。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
public interface DepartmentMembershipRestructureService {

    /**
     * 计算多个部门合并后的成员关系变化。
     *
     * @param sourceDepartmentIds 源部门 ID
     * @return 成员关系影响摘要
     */
    DepartmentMembershipRestructureImpact previewMerge(Collection<UUID> sourceDepartmentIds);

    /**
     * 将源部门的成员关系改写到新部门。
     *
     * @param sourceDepartmentIds 源部门 ID
     * @param targetDepartmentId  新部门 ID
     * @param operatorId          操作者 ID
     * @return 受影响用户 ID
     */
    Set<UUID> applyMerge(Collection<UUID> sourceDepartmentIds, UUID targetDepartmentId, UUID operatorId);

    /**
     * 计算部分拆分成员关系变化。
     *
     * @param sourceDepartmentId 源部门 ID
     * @param userIds            用户 ID
     * @return 成员关系影响摘要
     */
    DepartmentMembershipRestructureImpact previewSplit(UUID sourceDepartmentId, Collection<UUID> userIds);

    /**
     * 将选中用户直接属于源部门的关系改写到新部门。
     *
     * @param sourceDepartmentId 源部门 ID
     * @param userIds            用户 ID
     * @param targetDepartmentId 新部门 ID
     * @param operatorId         操作者 ID
     * @return 受影响用户 ID
     */
    Set<UUID> applySplit(UUID sourceDepartmentId, Collection<UUID> userIds,
                         UUID targetDepartmentId, UUID operatorId);
}
