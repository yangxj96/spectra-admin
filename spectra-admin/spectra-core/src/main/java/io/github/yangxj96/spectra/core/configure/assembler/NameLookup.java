package io.github.yangxj96.spectra.core.configure.assembler;

import java.util.Map;
import java.util.Set;

public interface NameLookup<ID> {

    /// ID类型
    ///
    /// @return 返回ID的类型
    default Class<ID> idType() {
        @SuppressWarnings("unchecked")
        Class<ID> type = (Class<ID>) String.class;
        return type;
    }

    /// 批量查询 name
    ///
    /// @param ids ID 集合
    /// @return id -> name
    Map<ID, String> getNameMap(Set<ID> ids);
}
