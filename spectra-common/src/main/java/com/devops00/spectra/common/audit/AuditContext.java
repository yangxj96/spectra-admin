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

import java.util.UUID;

/**
 * 审计事件共享的请求上下文。
 *
 * <p>上下文不包含租户字段。Spectra 是单租户系统；组织和数据范围属于授权语义，
 * 不属于审计上下文。</p>
 *
 * @param operatorId    操作者用户 ID；匿名事件可以为空
 * @param requestId     当前请求 ID
 * @param correlationId 关联请求、事务或业务动作的 ID
 * @param client        客户端类型
 * @param ip            客户端 IP
 * @param userAgent     客户端 User-Agent
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public record AuditContext(UUID operatorId,
                           String requestId,
                           String correlationId,
                           String client,
                           String ip,
                           String userAgent) {

    public AuditContext {
        requestId = normalize(requestId);
        correlationId = normalize(correlationId);
        client = normalize(client);
        ip = normalize(ip);
        userAgent = normalize(userAgent);
    }

    /**
     * 创建没有请求身份的系统上下文。
     *
     * @return 空审计上下文
     */
    public static AuditContext empty() {
        return new AuditContext(null, null, null, null, null, null);
    }

    /**
     * 将空白文本统一为缺省值。
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
