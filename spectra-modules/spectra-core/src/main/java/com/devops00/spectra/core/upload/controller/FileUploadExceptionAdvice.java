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

package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.framework.web.response.R;
import com.devops00.spectra.core.upload.api.FileUploadException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 提供文件上传异常相关的 HTTP 接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FileUploadExceptionAdvice {

    /**
     * 处理文件上传异常。
     *
     * @param exception 异常参数。
     * @return 处理后的结果。
     */
    @ExceptionHandler(FileUploadException.class)
    public R<Object> handle(FileUploadException exception) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case FILE_UPLOAD_NOT_FOUND, FILE_ASSET_NOT_READY, FILE_TYPE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FILE_UPLOAD_PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case FILE_UPLOAD_EXPIRED -> HttpStatus.GONE;
            case FILE_UPLOAD_CONCURRENCY_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case FILE_PART_HASH_MISMATCH, FILE_UPLOAD_HASH_MISMATCH -> HttpStatus.UNPROCESSABLE_ENTITY;
            case FILE_ASSET_IN_USE, FILE_UPLOAD_CONFLICT -> HttpStatus.CONFLICT;
            case FILE_STORAGE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return R.failure(status, exception.getErrorCode().name() + ": " + exception.getMessage());
    }
}
