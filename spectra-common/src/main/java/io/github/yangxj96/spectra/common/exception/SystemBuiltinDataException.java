package io.github.yangxj96.spectra.common.exception;

/**
 * 系统内置数据
 */
public class SystemBuiltinDataException extends RuntimeException {

    public SystemBuiltinDataException() {
        super("系统内置数据");
    }


    public SystemBuiltinDataException(String message) {
        super(message);
    }

}
