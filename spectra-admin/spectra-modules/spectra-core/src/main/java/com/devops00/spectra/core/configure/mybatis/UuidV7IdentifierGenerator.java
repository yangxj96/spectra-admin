package com.devops00.spectra.core.configure.mybatis;


import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.github.f4b6a3.uuid.UuidCreator;

/// UUIDv7版本ID生成器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/21 14:21
public class UuidV7IdentifierGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        // MP 要求返回 Number，但 UUID 不适合
        throw new UnsupportedOperationException("Use nextUUID()");
    }

    @Override
    public String nextUUID(Object entity) {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
}
