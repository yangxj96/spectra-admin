package com.devops00.spectra.common.assembler.converter;

/// ID转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/20 11:41
public interface IdConverter<ID> {

    String toString(ID id);

    ID fromString(String value);
}