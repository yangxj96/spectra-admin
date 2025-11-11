/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.license.generator;

import io.github.yangxj96.spectra.license.utils.RSAUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 生成key
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
public class KeyGenerator {

    private KeyGenerator() {
    }

    static void main() throws Exception {
        // 1. 生成 RSA 密钥对（2048位）
        var keyPair = RSAUtils.generateKeyPair();

        // 2. 保存私钥到文件（你自己保留！）
        RSAUtils.savePrivateKey(keyPair.getPrivate(), "private-key.pem");
        log.info("✅ 私钥已生成：private-key.pem");

        // 3. 保存公钥到文件（分发给客户）
        RSAUtils.savePublicKey(keyPair.getPublic(), "public-key.pem");
        log.info("✅ 公钥已生成：public-key.pem");

        log.info("\n⚠️  安全提示：");
        log.info("  - private-key.pem 必须严格保密！");
        log.info("  - 不要提交到 Git、不要发给客户！");
        log.info("  - 建议加密备份。");
    }

}
