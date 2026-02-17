package io.github.yangxj96.spectra.common.constant;

/// redis的key的规范
public interface RedisKey {

    String getPattern();

    default String format(Object... args) {
        return String.format(getPattern(), args);
    }
}
