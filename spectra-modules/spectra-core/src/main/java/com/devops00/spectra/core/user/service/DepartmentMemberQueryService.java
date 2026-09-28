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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.core.user.javabean.from.DepartmentMemberPageFrom;
import com.devops00.spectra.core.user.javabean.vo.DepartmentMemberCandidateVO;

/**
 * 查询指定活动部门的直属用户成员。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
public interface DepartmentMemberQueryService {

    /**
     * 分页查询直接使用主部门或关联部门关系的用户。
     *
     * @param from 部门及分页筛选条件
     * @return 部门直属成员页
     */
    IPage<DepartmentMemberCandidateVO> page(DepartmentMemberPageFrom from);
}
