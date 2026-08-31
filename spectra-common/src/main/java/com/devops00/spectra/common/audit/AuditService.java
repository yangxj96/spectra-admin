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

package com.devops00.spectra.common.audit;

/**
 * 统一审计写入端口。
 *
 * <p>调用方只提交 {@link AuditRecord}，不直接依赖 {@code sys_log}、
 * {@code sec_security_audit_event} 或其 Mapper。方法正常返回只表示事件已被当前 sink 接受；
 * 任何无法接受或持久化的错误都必须抛出 {@link AuditRecordingException} 或其子类，禁止静默丢弃。
 * SECURITY 事件由实现保持 fail-closed；OPERATION 事件由 Core sink 在当前事务内写入 PostgreSQL outbox。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@FunctionalInterface
public interface AuditService {

    /**
     * 记录一条统一审计事件。
     *
     * @param record 统一审计事件
     * @throws AuditRecordingException 事件未被接受或存储不可用
     */
    void record(AuditRecord record);

    /**
     * 审计事件无法被统一入口接受或持久化时抛出的运行时异常。
     */
    class AuditRecordingException extends RuntimeException {

        public AuditRecordingException(String message) {
            super(message);
        }

        public AuditRecordingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
