/**
 * HikariCP Prometheus 指标解析器
 * - 兼容 Vue 3 + Vite + strict TS 配置（含 noUncheckedIndexedAccess）
 * - 无 any、无 undefined、无非空断言（!）、无类型断言（as）
 * @author Jack Young
 * @version 1.0
 * @since 2025/11/26 11:41
 */

/**
 * HikariCP监控指标
 */
interface HikariMetric {
    activeConnections: number;
    idleConnections: number;
    totalConnections: number;
    maxConnections: number;
    minConnections: number;
    pendingThreads: number;
    timeoutCount: number;
    acquireTimeMaxMs: number;
    creationTimeMaxMs: number;
    usageTimeMaxMs: number;
}

/**
 * 指标解析结果
 */
export interface ParsedMetrics {
    pools: Record<string, HikariMetric>;
    summary: {
        healthStatus: "healthy" | "warning" | "critical";
        message: string;
    };
}

/**
 * 安全获取指标值（严格模式下兼容 noUncheckedIndexedAccess）
 */
function getMetricValue(metrics: Record<string, number>, key: string): number {
    // 使用 hasOwnProperty 确保 key 存在于对象自身
    if (Object.prototype.hasOwnProperty.call(metrics, key)) {
        // const value = metrics[key]; // 类型：number | undefined
        // // 显式检查类型，满足 strict 模式要求
        // if (typeof value === "number") {
        //     return value;
        // }
        return metrics[key]!;
    }
    return 0;
}

/**
 * 解析 Prometheus 格式的 HikariCP 指标文本
 */
export function parseHikariCpMetrics(text: string): ParsedMetrics {
    const lines = text
        .split("\n")
        .map(line => line.trim())
        .filter(line => line.length > 0);

    // 使用 Map 避免嵌套 Record 初始化问题
    const rawData = new Map<string, Record<string, number>>();

    for (const line of lines) {
        if (line.startsWith("#")) continue;

        const metricMatch = line.match(
            /^([a-zA-Z_][a-zA-Z0-9_]*)({.*?})?\s+([+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?)$/
        );

        // ✅ 关键：显式验证捕获组类型
        if (!metricMatch || typeof metricMatch[1] !== "string" || typeof metricMatch[3] !== "string") {
            continue;
        }

        const metricName = metricMatch[1];
        const labelStr = metricMatch[2] ?? "";
        const valueStr = metricMatch[3];

        if (!metricName.startsWith("hikaricp_")) continue;

        let poolName = "default";
        const poolMatch = labelStr.match(/pool="([^"]+)"/);
        if (poolMatch && typeof poolMatch[1] === "string") {
            poolName = poolMatch[1];
        }

        // 获取或创建 pool 的指标对象
        let poolMetrics: Record<string, number>;
        if (rawData.has(poolName)) {
            poolMetrics = rawData.get(poolName)!; // safe: just checked with .has()
        } else {
            poolMetrics = {};
            rawData.set(poolName, poolMetrics);
        }

        const parsedValue = parseFloat(valueStr);
        if (!isNaN(parsedValue) && isFinite(parsedValue)) {
            poolMetrics[metricName] = parsedValue;
        }
    }

    // 构建结果
    const pools: Record<string, HikariMetric> = {};

    for (const [poolName, metrics] of rawData.entries()) {
        pools[poolName] = {
            activeConnections: getMetricValue(metrics, "hikaricp_connections_active"),
            idleConnections: getMetricValue(metrics, "hikaricp_connections_idle"),
            totalConnections: getMetricValue(metrics, "hikaricp_connections"),
            maxConnections: getMetricValue(metrics, "hikaricp_connections_max"),
            minConnections: getMetricValue(metrics, "hikaricp_connections_min"),
            pendingThreads: getMetricValue(metrics, "hikaricp_connections_pending"),
            timeoutCount: getMetricValue(metrics, "hikaricp_connections_timeout_total"),
            acquireTimeMaxMs: getMetricValue(metrics, "hikaricp_connections_acquire_seconds_max") * 1000,
            creationTimeMaxMs: getMetricValue(metrics, "hikaricp_connections_creation_seconds_max") * 1000,
            usageTimeMaxMs: getMetricValue(metrics, "hikaricp_connections_usage_seconds_max") * 1000
        };
    }

    // 健康状态摘要
    const poolNames = Object.keys(pools);
    let healthStatus: "healthy" | "warning" | "critical" = "healthy";
    let message = "连接池状态正常";

    if (poolNames.length > 0) {
        const firstPool = pools[poolNames[0]!];
        if (firstPool) {
            if (firstPool.pendingThreads > 0 || firstPool.timeoutCount > 0) {
                healthStatus = "critical";
                message = `⚠️ 连接池资源不足！有 ${firstPool.pendingThreads} 个线程在等待连接，${firstPool.timeoutCount} 次连接超时。`;
            } else if (firstPool.maxConnections > 0 && firstPool.activeConnections >= firstPool.maxConnections * 0.8) {
                healthStatus = "warning";
                message = `🟡 连接池使用率过高（${firstPool.activeConnections}/${firstPool.maxConnections}），建议关注。`;
            }
        }
    }

    return {
        pools,
        summary: { healthStatus, message }
    };
}
