package io.github.yangxj96.spectra.core.javabean.system.from;

import io.github.yangxj96.spectra.common.base.Verify;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 系统配置入参
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguredFrom {

    /// 主键ID
    @NotNull(message = "ID不能为空", groups = Verify.Update.class)
    private Long id;

    /// 配置VALUE
    @NotNull(message = "配置值不能为空")
    private String value;

    /// 备注说明
    private String remarks;

}
