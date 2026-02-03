use std::{env, fs, path::PathBuf};

fn main() {
    // 只在 release 构建时执行
    if env::var("PROFILE").as_deref() != Ok("release") {
        return;
    }

    // debug / release
    let profile = env::var("PROFILE").unwrap();

    let manifest_dir = PathBuf::from(env::var("CARGO_MANIFEST_DIR").unwrap());

    let target_dir = manifest_dir.join("target").join(&profile);

    // === 原始产物名（Rust 编译生成的） ===
    let (from_name, to_name) = if cfg!(target_os = "windows") {
        ("spectra_tools.dll", "spectra-tools.dll")
    } else if cfg!(target_os = "macos") {
        ("libspectra_tools.dylib", "libspectra-tools.dylib")
    } else {
        ("libspectra_tools.so", "libspectra-tools.so")
    };

    let from = target_dir.join(from_name);

    // === 目标目录（相对于 Cargo.toml） ===
    let to_dir = manifest_dir.join("../spectra-admin/spectra-license/src/main/resources/libs");
    fs::create_dir_all(&to_dir).unwrap();

    match to_dir.canonicalize() {
        Ok(abs) => {
            println!("cargo:info=to_dir absolute path: {:?}", abs);
        }
        Err(_) => {
            // 目录还不存在时 canonicalize 会失败
            println!("cargo:info=to_dir (not exist yet): {:?}", to_dir);
        }
    }

    let dest = to_dir.join(to_name);

    // === 复制 + 重命名 ===
    if from.exists() {
        fs::copy(&from, &dest).unwrap();
        println!("cargo:info=Copied & Renamed {:?} -> {:?}", from, dest);
    } else {
        println!("cargo:info=Library not found, skip copy: {:?}", from);
    }
}
