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
 * <p>调用方只提交 {@link AuditRecord}，不直接依赖审计表或持久化实现。方法正常返回表示事件已在
 * 当前事务中写入统一审计表；任何无法接受或持久化的错误都必须抛出 {@link AuditRecordingException}，
 * 禁止静默丢弃。</p>
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
     * 在执行不可逆或高风险安全变更前确认统一审计存储可用。
     *
     * <p>具体持久化实现应验证连接和审计表写入权限；Core 的生产实现必须 fail-closed。</p>
     */
    default void assertAvailable() {
        // Lightweight test or alternate implementations may not need a preflight check.
    }

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
