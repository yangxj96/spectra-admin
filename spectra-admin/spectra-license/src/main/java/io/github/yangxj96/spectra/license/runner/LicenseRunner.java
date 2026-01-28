package io.github.yangxj96.spectra.license.runner;


import io.github.yangxj96.spectra.license.jni.CryptoJNI;
import io.github.yangxj96.spectra.license.jni.HardwareJNI;
import io.netty.util.internal.NativeLibraryLoader;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * 监听模块启动执行
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/1/27 11:46
 */
@Slf4j
@Component
public class LicenseRunner implements ApplicationRunner {

    private static final String LIB_DIR = "libs";

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


    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        // 测试加密解密功能
        var cryptoJNI = new CryptoJNI();
        var text = "Hello, Rust!";
        log.info("元数据:{}", Arrays.toString(text.getBytes()));
        var encrypted = cryptoJNI.encrypt(text.getBytes());
        log.info("加密后: {}", Arrays.toString(encrypted));
        var decrypted = cryptoJNI.decrypt(encrypted);
        log.info("解密后: {}", Arrays.toString(decrypted));
        // 获取硬件ID
        var hardwareJNI = new HardwareJNI();
        log.info("生成的硬件ID:{}", hardwareJNI.getId());
    }
}
