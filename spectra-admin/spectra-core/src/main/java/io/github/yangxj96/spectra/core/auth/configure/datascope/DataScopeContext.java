package io.github.yangxj96.spectra.core.auth.configure.datascope;

/**
 * 数据范围上下文工具类
 */
public class DataScopeContext {

    private DataScopeContext() {
    }

    private static final ThreadLocal<Boolean> DATA_SCOPE_HOLDER = new ThreadLocal<>();

    public static void set(Boolean filter) {
        DATA_SCOPE_HOLDER.set(filter);
    }

    public static Boolean get() {
        return DATA_SCOPE_HOLDER.get();
    }

    public static void clear() {
        DATA_SCOPE_HOLDER.remove();
    }

}