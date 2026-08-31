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

import java.util.Map;

/**
 * 统一审计快照脱敏端口。
 *
 * <p>所有模块必须使用同一个实现处理 before/after 快照。实现必须返回不修改输入的结构化副本，
 * 递归处理嵌套集合，并至少屏蔽密码、Token、验证码、私钥和其他凭据；业务模块不得再维护自己的
 * 敏感字段 deny-list。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@FunctionalInterface
public interface AuditSanitizer {

    /** 统一的敏感值替换标记。 */
    String REDACTED_VALUE = "***";

    /**
     * 生成可写入审计事件的脱敏快照。
     *
     * @param snapshot 原始结构化快照，可以为空
     * @return 脱敏后的不可变或不再受输入修改影响的快照
     */
    Map<String, Object> sanitize(Map<String, ?> snapshot);
}
