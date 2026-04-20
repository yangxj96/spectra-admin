package com.devops00.spectra.common.assembler.converter;


import java.util.UUID;

/// UUID作为主键的转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/20 11:41
public class UuidConverter implements IdConverter<UUID> {

    @Override
    public String toString(UUID id) {
        return id == null ? null : id.toString();
    }

    @Override
    public UUID fromString(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}