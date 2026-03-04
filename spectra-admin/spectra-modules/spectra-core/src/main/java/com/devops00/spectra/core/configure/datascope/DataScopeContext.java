package com.devops00.spectra.core.configure.datascope;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/// 数据范围上下文
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:50
@Data
@Builder
public class DataScopeContext {

    /// 注解范围
    private DataScopeType scope;

    /// 当前用户
    private String userId;

    /// 可访问组织/部门 ID
    private List<String> targetIds;

    /// 表字段（可选，支持别名）
    private String scopeField;

    /// 用户字段
    private String userField;

    /// 是否忽略
    private boolean ignore;
}
