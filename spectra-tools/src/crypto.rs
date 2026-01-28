extern crate jni;

use jni::JNIEnv;
use jni::objects::{JByteArray, JClass};
use jni::sys::jbyteArray;

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_github_yangxj96_spectra_license_jni_CryptoJNI_encrypt(
    env: JNIEnv,
    _class: JClass,
    input: jbyteArray,
) -> jbyteArray {
    // raw → wrapper（Rust 接管 local ref）
    let input = unsafe { JByteArray::from_raw(input) };

    // 获取传入的字节数组
    let input_bytes: Vec<u8> = env.convert_byte_array(&input).unwrap();

    // 加密（示例：XOR 加密）
    let encrypted: Vec<u8> = input_bytes.iter().map(|&b| b ^ 0xAA).collect();

    println!("输出日志");

    // 将加密后的结果转化为 JByteArray
    let output = env.byte_array_from_slice(&encrypted).unwrap();

    output.into_raw()
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_github_yangxj96_spectra_license_jni_CryptoJNI_decrypt(
    env: JNIEnv,
    _class: JClass,
    input: jbyteArray,
) -> jbyteArray {
    // raw → wrapper（Rust 接管 local ref）
    let input = unsafe { JByteArray::from_raw(input) };

    // 获取传入的字节数组
    let input_bytes: Vec<u8> = env.convert_byte_array(&input).unwrap();

    // 解密（示例：XOR 解密）
    let decrypted: Vec<u8> = input_bytes.iter().map(|&b| b ^ 0xAA).collect();

    // 将解密后的结果转化为 JByteArray
    let output =  env.byte_array_from_slice(&decrypted).unwrap();

    output.into_raw()
}
