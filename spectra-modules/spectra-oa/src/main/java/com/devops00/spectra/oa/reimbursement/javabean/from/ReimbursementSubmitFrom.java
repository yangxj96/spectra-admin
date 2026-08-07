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

package com.devops00.spectra.oa.reimbursement.javabean.from;

import lombok.Data;

/// 报销提交参数。
///
/// approverUsername 为空时使用当前用户，便于本地演示；正式环境应由流程路由配置审批人。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class ReimbursementSubmitFrom {
    private String approverUsername;
}
