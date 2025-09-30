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

public class RSAUtils {

    private RSAUtils() {
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static void savePrivateKey(PrivateKey key, String path) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            PemObject pem = new PemObject("PRIVATE KEY", key.getEncoded());
            PemWriter pw = new PemWriter(fw);
            pw.writeObject(pem);
            pw.close();
        }
    }

    public static void savePublicKey(PublicKey key, String path) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            PemObject pem = new PemObject("PUBLIC KEY", key.getEncoded());
            PemWriter pw = new PemWriter(fw);
            pw.writeObject(pem);
            pw.close();
        }
    }

    public static PrivateKey loadPrivateKey(String path) throws Exception {
        try (FileReader fr = new FileReader(path);
             PemReader pr = new PemReader(fr)) {
            PemObject pem = pr.readPemObject();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pem.getContent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
    }

    public static PublicKey loadPublicKey(String path) throws Exception {
        try (FileReader fr = new FileReader(path);
             PemReader pr = new PemReader(fr)) {
            PemObject pem = pr.readPemObject();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(pem.getContent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
    }

    /**
     * 加载私钥 (PEM 格式)
     */
    public static PrivateKey loadPrivateKey(InputStream in) throws Exception {
        String content = new String(in.readAllBytes());
        content = content.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * 加载公钥 (PEM 格式)
     */
    public static PublicKey loadPublicKey(InputStream in) throws Exception {
        String content = new String(in.readAllBytes());
        content = content.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static String sign(String content, PrivateKey privateKey) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA", "BC");
        sign.initSign(privateKey);
        sign.update(content.getBytes());
        byte[] signature = sign.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String content, String signature, PublicKey publicKey) throws Exception {
        Signature verify = Signature.getInstance("SHA256withRSA", "BC");
        verify.initVerify(publicKey);
        verify.update(content.getBytes());
        byte[] sig = Base64.getDecoder().decode(signature);
        return verify.verify(sig);
    }
}