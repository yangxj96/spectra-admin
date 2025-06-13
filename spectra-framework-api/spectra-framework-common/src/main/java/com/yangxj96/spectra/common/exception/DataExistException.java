package com.yangxj96.spectra.common.exception;

/**
 * 数据存在异常
 *
 * @author Jack Young
 * @since 2025/6/12 17:03
 */
public class DataExistException extends RuntimeException {

    public DataExistException(String message) {
        super(message);
    }
}
