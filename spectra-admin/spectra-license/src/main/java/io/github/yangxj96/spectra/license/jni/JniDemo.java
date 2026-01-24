package io.github.yangxj96.spectra.license.jni;


/**
 * JNI测试
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/1/25 00:27
 */
public class JniDemo {


    // 静态加载 Rust 编译出的共享库
    static {
        // 注意：库名应和 Rust 输出的库文件一致（在 Linux 上是 libcrypto.so）
        System.loadLibrary("crypto");
    }


    static void main(String[] args) {
        // 测试加密解密功能
        //var cryptoJNI = new CryptoJNI();
        //var text = "Hello, Rust!";
        //var encrypted = cryptoJNI.encrypt(text.getBytes());
        //System.out.println("Encrypted: " + new String(encrypted));
        //var decrypted = cryptoJNI.decrypt(encrypted);
        //System.out.println("Decrypted: " + new String(decrypted));
        //// 释放内存
        //cryptoJNI.freeBuffer(encrypted);
        //cryptoJNI.freeBuffer(decrypted);

        // 获取硬件ID
        var hardwareJNI = new HardwareJNI();
        System.out.println("生成的硬件ID:" + hardwareJNI.getId());
    }

}
