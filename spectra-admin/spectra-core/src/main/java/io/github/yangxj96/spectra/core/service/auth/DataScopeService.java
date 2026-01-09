package io.github.yangxj96.spectra.core.service.auth;

import io.github.yangxj96.spectra.core.configure.datascope.DataScopeContext;

/// 数据范围获取
public interface DataScopeService {

    /// 根据用户ID获取数据范围
    /// >用户直授 > 角色 > 默认
    ///
    /// @param userId 用户ID
    /// @return 数据范围
    DataScopeContext resolve(Long userId);

    /// 根据用户ID获取用户权限范围
    ///
    /// @param userId 用户ID
    /// @return 数据范围
    DataScopeContext resolveUser(Long userId);

    /// 根据用户ID获取用户角色,后根据角色获取权限范围
    ///
    /// @param userId 用户ID
    /// @return 数据范围
    DataScopeContext resolveRole(Long userId);
}
