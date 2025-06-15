package com.yangxj96.spectra.common.exception;

/**
 * 数据存在异常
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public class DataExistException extends RuntimeException {

    public DataExistException(String message) {
        super(message);
    }
}
