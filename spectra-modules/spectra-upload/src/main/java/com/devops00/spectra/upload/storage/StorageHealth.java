/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage;

/** 文件存储 Provider 的内部探针结果；仅允许携带稳定错误码，不携带原始异常信息。 */
public record StorageHealth(boolean available, String errorCode) {

    /** 创建成功的内部探针结果。 */
    public static StorageHealth available(String safeCode) {
        return new StorageHealth(true, safeCode);
    }

    /** 创建失败的内部探针结果。 */
    public static StorageHealth unavailable(String errorCode) {
        return new StorageHealth(false, errorCode);
    }

    public StorageHealth {
        if (errorCode != null && errorCode.isBlank()) {
            errorCode = null;
        }
        if (!available && errorCode == null) {
            errorCode = "STORAGE_UNAVAILABLE";
        }
    }
}
