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

package com.devops00.spectra.framework.health.readiness;

/**
 * 运行期依赖恢复后的一致性校验。
 *
 * <p>依赖（数据库、安全 Redis）重新可用后，仅凭连通性不足以判定可以服务；实现方在此校验自身状态
 * （如接口加解密密钥、缓存一致性）。任一实现抛出异常时，探针保持未就绪并继续退避重试。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@FunctionalInterface
public interface ReadinessVerifier {

    /**
     * 校验恢复后的一致性；不一致时抛出异常。
     *
     * @throws Exception 状态不一致，服务不得重新就绪
     */
    void verifyRecovery() throws Exception;
}
