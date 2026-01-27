extern crate hex;
extern crate jni;
extern crate md5;
extern crate wmi;

use crate::platform;
use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jstring;
use md5::compute;

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "C" fn Java_io_github_yangxj96_spectra_license_jni_HardwareJNI_getId(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    // 根据目标平台选择调用相应的平台特定函数
    let platform = platform::platform_specific();
    let hardware_id = platform.platform_specific_function();

    // 将 Rust 字符串转换为 Java 字符串
    let output = env.new_string(hash_string(&*hardware_id)).unwrap();
    output.into_inner() // 返回 Java 字符串对象
}

// 哈希化硬件标识符
fn hash_string(input: &str) -> String {
    // 使用 md5::compute 计算 MD5
    let result = compute(input);
    // 将计算结果转为十六进制字符串
    hex::encode(result.to_vec())
}
