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

package io.github.yangxj96.spectra.license.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RAS加解密
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
public class RSAUtils {

    private RSAUtils() {
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws Exception {
        var gen = KeyPairGenerator.getInstance("RSA", "BC");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static void savePrivateKey(PrivateKey key, String path) throws IOException {
        try (var fw = new FileWriter(path)) {
            var pem = new PemObject("PRIVATE KEY", key.getEncoded());
            var pw = new PemWriter(fw);
            pw.writeObject(pem);
            pw.close();
        }
    }

    public static void savePublicKey(PublicKey key, String path) throws IOException {
        try (var fw = new FileWriter(path)) {
            var pem = new PemObject("PUBLIC KEY", key.getEncoded());
            var pw = new PemWriter(fw);
            pw.writeObject(pem);
            pw.close();
        }
    }

    public static PrivateKey loadPrivateKey(String path) throws Exception {
        try (var fr = new FileReader(path); var pr = new PemReader(fr)) {
            var pem = pr.readPemObject();
            var spec = new PKCS8EncodedKeySpec(pem.getContent());
            var kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }

    public static PublicKey loadPublicKey(String path) throws Exception {
        try (var fr = new FileReader(path); var pr = new PemReader(fr)) {
            var pem = pr.readPemObject();
            var spec = new X509EncodedKeySpec(pem.getContent());
            var kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
    }

    /**
     * 加载私钥 (PEM 格式)
     */
    public static PrivateKey loadPrivateKey(InputStream in) throws Exception {
        var content = new String(in.readAllBytes());
        content = content.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        var decoded = Base64.getDecoder().decode(content);
        var spec = new PKCS8EncodedKeySpec(decoded);
        var kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * 加载公钥 (PEM 格式)
     */
    public static PublicKey loadPublicKey(InputStream in) throws Exception {
        var content = new String(in.readAllBytes());
        content = content.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        var decoded = Base64.getDecoder().decode(content);
        var spec = new X509EncodedKeySpec(decoded);
        var kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static String sign(String content, PrivateKey privateKey) throws Exception {
        var sign = Signature.getInstance("SHA256withRSA", "BC");
        sign.initSign(privateKey);
        sign.update(content.getBytes());
        var signature = sign.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String content, String signature, PublicKey publicKey) throws Exception {
        var verify = Signature.getInstance("SHA256withRSA", "BC");
        verify.initVerify(publicKey);
        verify.update(content.getBytes());
        var sig = Base64.getDecoder().decode(signature);
        return verify.verify(sig);
    }
}