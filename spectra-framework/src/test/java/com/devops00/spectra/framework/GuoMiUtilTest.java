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

package com.devops00.spectra.framework;


import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.framework.configure.mvc.properties.SMProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

/// 国密加解密测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/2 17:05
@Slf4j
@SpringBootTest
public class GuoMiUtilTest {

    @Autowired
    private SMProperties properties;

    @Autowired
    private ObjectMapper om;

    public static void main(String[] args) throws Exception {
        // ===== AES 测试 =====
        SecretKey aesKey = AESUtils.generateKey();
        byte[] aesIv = AESUtils.generateIv();
        String aesEncrypted = AESUtils.encrypt("Hello AES", aesKey, aesIv);
        String aesDecrypted = AESUtils.decrypt(aesEncrypted, aesKey, aesIv);
        System.out.println("AES 解密(GCM): " + aesDecrypted);
        System.out.println("AES 密钥(Base64): " + Base64.getEncoder().encodeToString(aesKey.getEncoded()));
        System.out.println("AES IV(Hex): " + AESUtils.getIvHex(aesIv));

        // ===== RSA 签名验签 =====
        KeyPair keyPair = RSAUtils.generateKeyPair();
        String sign = RSAUtils.sign("Hello RSA", keyPair.getPrivate());
        boolean isValid = RSAUtils.verify("Hello RSA", sign, keyPair.getPublic());
        System.out.println("RSA 验签: " + isValid);
        System.out.println("公钥(Base64): " + RSAUtils.getPublicKeyBase64(keyPair.getPublic()));
        System.out.println("私钥(Base64): " + RSAUtils.getPrivateKeyBase64(keyPair.getPrivate()));

        // ===== RSA 加解密测试（加密AES密钥的场景）=====
        byte[] fakeAesKey = aesKey.getEncoded();
        String encryptedKey = RSAUtils.encrypt(fakeAesKey, keyPair.getPublic());
        byte[] decryptedKey = RSAUtils.decrypt(encryptedKey, keyPair.getPrivate());
        System.out.println("RSA 加解密AES密钥: " + Arrays.equals(fakeAesKey, decryptedKey));

        // ===== SHA-256 测试 =====
        String digest = SHA256Utils.hash("Hello SHA256");
        System.out.println("SHA-256 摘要: " + digest);

        // ===== HMAC-SHA256 测试 =====
        String hmac = SHA256Utils.hmac("Hello HMAC", "secret-key");
        System.out.println("HMAC-SHA256: " + hmac);

        // ===== Nonce 生成测试 =====
        System.out.println("Nonce: " + SHA256Utils.generateNonce());
    }

    /// 解密测试
    @Test
    void decrypt() throws Exception {
        var str = """
                {
                    "data": "7EUgu8Ae1VfyFNIIfRWuyeXSdSs8SrsVPyqg4DfWy1oaWe18idMYfhONRZjLwO4oVo7gL708cY0JdMFAsp8pnZOtEdN/bbJ0XSKm5CQt3LmBfpVwrCGmZ3OtCiF5ctTrljh3Ue37vAZb9t65WCm0C+2nJDGMDEKW9KARvZXbOpSUn/zhfvB6B0E5ACbd17JafpbIUq8F0+1KWr+xo1B9Z7PhxTfclWsjM/cEjjKEn1EMFJsQFSnmTke8sykwnJ+xpKE3JQgBx1Tnrwzp04rNDaxUVyZS3Yqa6hfmtwmYJPxWPUrOyg==",
                    "signature": "oAUO3aNs6SXO9amWOs/uPxAv14XAm3JDAy0jW3Vg1kYrpYzO4UbgrKa5Kg/oqRBulkg/9+MqgXLAqFw/7+GnNwmUficMrINWtBxDMIFC5FMzIILOEf3qQRP38m4N/PwBc8U15vubXZKOzKkZK5uW4LG3eT3Xdankaog5T4m7K5jkynoQns7nLtGnf+VSx5HU+eJzfKjAhsphzm1nhOo3H/Xjzy2/JNWhrd0S6XhROzEOy/DqpgTtZNmTBqZauLC5nyqC0twbFzl3SA42ZSk7TZKoRcQn9aHHC82av82C6Ggp9TRoy3ZmkNRAV//hVE3Kvo/pVz8XULIhp/P8eqe5EA==",
                    "iv": "e8aed64d31b0fd8277527a2d",
                    "nonce": "+q3m8Z0KDHZdJ7bFkTi2iA==",
                    "key": "bKz6jOqpzXfEncCiFd37PSqvijtf9WUgxuXFoVaS215M4BjtlfNth1wUGkamaI+ySIn3fxrAPPLqp50kKSsdVXBEYa+e8Dhpyz3Futs5/jaNbgcH5zcYqbDK7UjxhEH5ydQnJqFSrFEziZqKdB4/KUeT8KYHNw8jV2yLvXBSNmUUJt2fy7W7BjKaguLn2jCkvN4kdkU41F0txK1beN7kMhrAaIECJEJ+s4YdftqVy0FPq4Y9iYcDG759s9FELfN8d4dy4Yuyr1HXh8xGTCprI7bO+yVxVkgGbMgK0xGtA3+ZrqAO1uSRdUXr0iNlZYK2IwBCttp14TS10y9+lte8zA==",
                    "timestamp": 1780561407
                }
                """;

        PublicKey publicKey = RSAUtils.restorePublicKey(properties.getPublicKey());
        PrivateKey privateKey = RSAUtils.restorePrivateKey(properties.getPrivateKey());

        // 解析 JSON
        JsonNode json = om.readTree(str);
        String encryptedData = json.get("data").asString();
        String encryptedKey = json.get("key").asString();
        String ivHex = json.get("iv").asString();
        String nonce = json.get("nonce").asString();
        long timestamp = json.get("timestamp").asLong();
        String signature = json.get("signature").asString();

        // 1. 验签
        String signContent = String.format("data=%s&nonce=%s&timestamp=%d", encryptedData, nonce, timestamp);
        boolean isValid = RSAUtils.verify(signContent, signature, publicKey);
        System.out.println("验签结果: " + isValid);
        if (!isValid) {
            throw new RuntimeException("签名验证失败");
        }

        // 2. RSA-OAEP(SHA-256) 私钥解密 AES 密钥
        byte[] decryptedAesKey = RSAUtils.decrypt(encryptedKey, privateKey);
        System.out.println("解密后的 AES 密钥(Base64): " + Base64.getEncoder().encodeToString(decryptedAesKey));

        // 3. AES-GCM 解密业务数据
        byte[] iv = AESUtils.hexToIv(ivHex);
        String decryptedData = AESUtils.decrypt(encryptedData, decryptedAesKey, iv);
        System.out.println("解密后的业务数据: " + decryptedData);

    }

}
