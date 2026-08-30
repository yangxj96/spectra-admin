/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.controller;

import com.devops00.spectra.common.response.R;
import com.devops00.spectra.upload.api.FileUploadException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FileUploadExceptionAdvice {

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
