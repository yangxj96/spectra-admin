package com.devops00.spectra.common.utils;


import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RAS非对称加密
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/4 16:07
 */
public class RSAUtils {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int KEY_SIZE = 2048;

    /**
     * 生成RSA密钥对
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 从Base64字符串恢复公钥
     */
    public static PublicKey restorePublicKey(String base64PublicKey) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 从Base64字符串恢复私钥
     */
    public static PrivateKey restorePrivateKey(String base64PrivateKey) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * RSA-OAEP公钥加密（用于加密AES密钥）
     */
    public static String encrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * RSA-OAEP私钥解密
     */
    public static byte[] decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepSpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        return cipher.doFinal(decoded);
    }

    /**
     * RSA私钥签名（SHA-256 with RSA）
     */
    public static String sign(String content, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signBytes);
    }

    /**
     * RSA公钥验签
     */
    public static boolean verify(String content, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signBytes);
    }

    /**
     * 获取Base64编码的公钥字符串
     */
    public static String getPublicKeyBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 获取Base64编码的私钥字符串
     */
    public static String getPrivateKeyBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

}
