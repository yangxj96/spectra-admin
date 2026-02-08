package io.github.yangxj96.spectra.core.configure.datascope;


/// 数据范围上下文持有器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/23 11:55
public class DataScopeHolder {

    /// 本地线程对象
    private static final ThreadLocal<DataScopeContext> CONTEXT = new ThreadLocal<>();

    /// 设置
    public static void set(DataScopeContext context) {
        CONTEXT.set(context);
    }

    /// 获取
    public static DataScopeContext get() {
        return CONTEXT.get();
    }

    /// 清理
    public static void clear() {
        CONTEXT.remove();
    }

}
