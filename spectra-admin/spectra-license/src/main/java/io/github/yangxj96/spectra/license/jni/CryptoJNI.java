package io.github.yangxj96.spectra.license.jni;


/**
 * 加密的JNI调用
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/1/25 00:07
 */
public class CryptoJNI {

    public native byte[] encrypt(byte[] input);

    public native byte[] decrypt(byte[] input);

    public native void freeBuffer(byte[] buffer);

}
