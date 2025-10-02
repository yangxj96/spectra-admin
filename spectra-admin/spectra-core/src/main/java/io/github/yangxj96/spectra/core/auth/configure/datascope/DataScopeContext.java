package io.github.yangxj96.spectra.core.auth.configure.datascope;

/**
 * 数据范围上下文工具类
 */
public class DataScopeContext {

    private DataScopeContext() {
    }

    private static final ThreadLocal<DataScopeInfo> DATA_SCOPE_HOLDER = new ThreadLocal<>();

    public static void set(DataScopeInfo info) {
        DATA_SCOPE_HOLDER.set(info);
    }

    public static DataScopeInfo get() {
        return DATA_SCOPE_HOLDER.get();
    }

    public static void clear() {
        DATA_SCOPE_HOLDER.remove();
    }

}