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

package com.devops00.spectra.workflow.service;

import java.util.Map;

/// 审批回调接口
///
/// 业务模块实现此接口处理审批结果
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
public interface ApprovalCallback {

    /// 审批通过回调
    ///
    /// @param businessKey 业务KEY
    /// @param variables   流程变量
    void onApproved(String businessKey, Map<String, Object> variables);

    /// 审批驳回回调
    ///
    /// @param businessKey 业务KEY
    /// @param reason      驳回原因
    void onRejected(String businessKey, String reason);

    /// 流程终止回调
    ///
    /// @param businessKey 业务KEY
    /// @param reason      终止原因
    void onTerminated(String businessKey, String reason);
}
