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

package com.devops00.spectra.security.base.audit;

/**
 * 安全审计事件结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public enum AuditResult {

    /** 变更尚未执行，仅用于高风险事务的审计预写入。 */
    STARTED,

    /** 操作成功。 */
    SUCCEEDED,

    /** 操作失败。 */
    FAILED,

    /** 操作被安全边界拒绝。 */
    DENIED
}
