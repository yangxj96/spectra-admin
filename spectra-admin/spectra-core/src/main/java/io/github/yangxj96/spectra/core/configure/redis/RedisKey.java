package io.github.yangxj96.spectra.core.configure.redis;

/// redis的key的规范
public interface RedisKey {

    String getPattern();

    default String format(Object... args) {
        return String.format(getPattern(), args);
    }
}
