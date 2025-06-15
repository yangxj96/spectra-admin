package com.yangxj96.spectra.common.exception;


/**
 * 数据不存在异常
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public class DataNotExistException extends RuntimeException {

    public DataNotExistException(String message) {
        super(message);
    }
}
