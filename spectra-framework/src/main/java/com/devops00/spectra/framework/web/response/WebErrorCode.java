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

package com.devops00.spectra.framework.web.response;

/**
 * 稳定的对外错误码。
 *
 * <p>统一响应失败时通过 {@code error_code} 携带枚举名，调用方据此稳定识别错误类别，
 * 不再依赖 {@code msg} 文本前缀。成功响应不设置该字段（序列化时缺省隐藏）。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
public enum WebErrorCode {

    /** 请求参数校验失败或格式无效。 */
    REQUEST_INVALID,
    /** 请求体不可解析。 */
    REQUEST_BODY_UNREADABLE,
    /** 请求加密数据无效。 */
    REQUEST_CRYPTO_INVALID,
    /** 资源不存在。 */
    RESOURCE_NOT_FOUND,
    /** 数据不存在。 */
    DATA_NOT_FOUND,
    /** 数据已存在。 */
    DATA_EXIST,
    /** 当前状态与请求冲突。 */
    BUSINESS_CONFLICT,
    /** 实体更新冲突（并发/版本）。 */
    ENTITY_UPDATE_CONFLICT,
    /** 业务规则校验失败。 */
    BUSINESS_RULE_VIOLATION,
    /** 数据范围不足。 */
    DATA_SCOPE_VIOLATION,
    /** 权限不足。 */
    ACCESS_DENIED,
    /** 用户未登录或登录已过期。 */
    UNAUTHENTICATED,
    /** 认证失败。 */
    AUTHENTICATION_FAILED,
    /** 需要先修改密码。 */
    PASSWORD_CHANGE_REQUIRED,
    /** 验证码不匹配。 */
    KAPTCHA_NOT_MATCH,
    /** 验证码过期。 */
    KAPTCHA_EXPIRED,
    /** 功能暂未实现。 */
    NOT_IMPLEMENTED,
    /** 调度数据库不可用。 */
    SCHEDULER_DATABASE_UNAVAILABLE,
    /** 安全会话 Redis 不可用。 */
    SECURITY_REDIS_UNAVAILABLE,
    /** 安全密钥服务不可用。 */
    SECURITY_SECRET_UNAVAILABLE,
    /** 安全策略服务不可用。 */
    SECURITY_POLICY_UNAVAILABLE,
    /** 审计服务不可用。 */
    AUDIT_SERVICE_UNAVAILABLE,
    /** 加密配置不可用。 */
    CRYPTO_CONFIGURATION_UNAVAILABLE,
    /** 必需依赖运行期未就绪。 */
    DEPENDENCY_NOT_READY,
    /** 数据完整性或持久化失败。 */
    PERSISTENCE_FAILED,
    /** 未分类的服务器内部错误。 */
    INTERNAL_ERROR
}
