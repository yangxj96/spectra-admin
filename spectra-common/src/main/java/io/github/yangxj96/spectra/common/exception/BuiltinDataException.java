package io.github.yangxj96.spectra.common.exception;

/**
 * 系统内置数据
 */
public class BuiltinDataException extends DataException {

    public BuiltinDataException() {
        super("系统内置数据");
    }

    public BuiltinDataException(String message) {
        super(message);
    }

}
