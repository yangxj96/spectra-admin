package io.github.yangxj96.spectra.license.jni;


import io.netty.util.internal.NativeLibraryLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * JNI测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/1/25 00:27
 */
public class JniDemo {

    private static final String LIB_DIR = "libs";

    // 静态加载 Rust 编译出的共享库
    static {
        load("spectra-tools");
    }

    public static void load(String libName) {
        try {
            String mapped = System.mapLibraryName(libName);
            InputStream in = NativeLibraryLoader.class
                    .getClassLoader()
                    .getResourceAsStream(LIB_DIR + "/" + mapped);

            if (in == null) {
                throw new RuntimeException("Native library not found: " + mapped);
            }

            Path tempDir = Files.createTempDirectory("jni-libs");
            tempDir.toFile().deleteOnExit();

            Path tempLib = tempDir.resolve(mapped);

            Files.copy(in, tempLib, StandardCopyOption.REPLACE_EXISTING);
            tempLib.toFile().deleteOnExit();

            System.load(tempLib.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library", e);
        }
    }

    static void main(String[] args) {
        // 测试加密解密功能
        var cryptoJNI = new CryptoJNI();
        var text = "Hello, Rust!";
        System.out.println("元数据:" + Arrays.toString(text.getBytes()));
        var encrypted = cryptoJNI.encrypt(text.getBytes());
        System.out.println("加密后: " + Arrays.toString(encrypted));
        var decrypted = cryptoJNI.decrypt(encrypted);
        System.out.println("解密后: " + Arrays.toString(decrypted));

        // 获取硬件ID
        var hardwareJNI = new HardwareJNI();
        System.out.println("生成的硬件ID:" + hardwareJNI.getId());
    }

}
