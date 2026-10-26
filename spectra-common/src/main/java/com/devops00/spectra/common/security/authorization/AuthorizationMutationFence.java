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

package com.devops00.spectra.common.security.authorization;

import java.util.UUID;

/**
 * 在业务写事务中锁住操作者的授权变更串行化行，直到事务提交。
 */
public interface AuthorizationMutationFence {

    /**
     * 锁定当前有效用户。必须在调用方写事务中执行；用户不存在或锁失败时抛出异常。
     */
    void lockUser(UUID userId);
}
