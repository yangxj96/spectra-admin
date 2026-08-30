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

package com.devops00.spectra.upload.api;

/** 文件上传服务错误码。 */
public enum FileErrorCode {
    FILE_UPLOAD_NOT_FOUND,
    FILE_UPLOAD_EXPIRED,
    FILE_UPLOAD_PERMISSION_DENIED,
    FILE_UPLOAD_CONFLICT,
    FILE_PART_INVALID,
    FILE_PART_HASH_MISMATCH,
    FILE_UPLOAD_HASH_MISMATCH,
    FILE_ASSET_NOT_READY,
    FILE_ASSET_IN_USE,
    FILE_TYPE_NOT_FOUND,
    FILE_TYPE_INVALID,
    FILE_STORAGE_UNAVAILABLE,
    FILE_UPLOAD_CONCURRENCY_LIMIT
}
