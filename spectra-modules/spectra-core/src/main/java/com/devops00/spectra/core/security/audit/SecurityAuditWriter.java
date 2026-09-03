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

package com.devops00.spectra.core.security.audit;

/**
 * 安全审计写入端口。
 * <p>
 * 高风险安全写操作必须先调用 {@link #assertAvailable()}，再执行数据库变更；写入失败必须向上抛出，
 * 不能降级成普通业务日志或静默丢弃。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface SecurityAuditWriter {

    /**
     * 检查审计存储是否可用。
     *
     * @throws SecurityAuditUnavailableException 审计存储不可用
     */
    void assertAvailable();

    /**
     * 追加一条不可变安全事实。
     *
     * @param event 审计事件
     * @throws SecurityAuditUnavailableException 写入失败
     */
    void append(SecurityAuditEvent event);
}
