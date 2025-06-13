package com.yangxj96.spectra.common.exception;


/**
 * 数据不存在异常
 *
 * @author Jack Young
 * @since 2025/6/12 17:03
 */
public class DataNotExistException extends RuntimeException {

    public DataNotExistException(String message) {
        super(message);
    }
}
