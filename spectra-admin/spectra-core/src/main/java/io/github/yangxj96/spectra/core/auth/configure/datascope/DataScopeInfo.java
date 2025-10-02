package io.github.yangxj96.spectra.core.auth.configure.datascope;

import io.github.yangxj96.spectra.common.enums.AuthScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据范围上下文信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataScopeInfo {

    /**
     * 是否开启数据过滤
     */
    private Boolean filter;

    /**
     * 数据范围
     */
    private AuthScope scope;

}
