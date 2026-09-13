/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.upload.storage;

/**
 * 承载存储健康状态相关的不可变数据。
 *
 * @param available 系统资源的可用容量
 * @param errorCode 错误编码
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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
