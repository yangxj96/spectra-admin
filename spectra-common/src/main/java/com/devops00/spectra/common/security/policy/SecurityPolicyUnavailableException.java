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

package com.devops00.spectra.common.security.policy;

import com.devops00.spectra.common.exception.SpectraException;

/**
 * 安全策略存储不可用时的 fail-closed 异常。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public class SecurityPolicyUnavailableException extends SpectraException {

    /** 创建策略不可用异常。 */
    public SecurityPolicyUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
