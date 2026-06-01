package com.devops00.spectra.framework.configure.mapstruct;

import org.mapstruct.Builder;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/// mapstruct全局配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/9 14:47
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface GlobalMapperConfig {
}
