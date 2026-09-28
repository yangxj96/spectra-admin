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

import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentMergeApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentMergePreviewFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentSplitApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentSplitPreviewFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.DepartmentRestructureApplyVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.DepartmentRestructurePreviewVO;

/**
 * 部门合并与直属成员部分拆分的 Preview/Apply 编排。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
public interface DepartmentRestructureService {

    DepartmentRestructurePreviewVO previewMerge(DepartmentMergePreviewFrom from);

    DepartmentRestructureApplyVO applyMerge(DepartmentMergeApplyFrom from);

    DepartmentRestructurePreviewVO previewSplit(DepartmentSplitPreviewFrom from);

    DepartmentRestructureApplyVO applySplit(DepartmentSplitApplyFrom from);
}
