package io.github.yangxj96.spectra.core.configure.datascope;


/**
 * 数据范围上下文持有器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/23 11:55
 */
public class DataScopeHolder {

    private static final ThreadLocal<DataScopeContext> CONTEXT = new ThreadLocal<>();

    public static void set(DataScopeContext context) {
        CONTEXT.set(context);
    }

    public static DataScopeContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

}
