// ElLoading       : loading组件
// ILoadingInstance: loading对象类型接口
import _ from "lodash";
import { ElLoading } from "element-plus";
import type { LoadingInstance } from "element-plus/lib/components/loading/src/loading";

// 计数器
let count: number = 0;

// 开始执行时间
let startTime: number = 0;

// 最小loading请求事件
const minLoadingTime: number = 300;

// loading对象
let loading: LoadingInstance;

function open(): void {
    startTime = Date.now();
    const els = document.querySelectorAll(".loading-box");
    const arr = [...els] as HTMLElement[];
    loading = ElLoading.service({
        target: arr.at(-1),
        lock: true,
        text: "数据加载中...",
        background: "rgba(0,0,0,0.5)"
    });
}

async function close(): void {
    // 计算是否需要补足最小 loading 时间
    const elapsed = Date.now() - startTime;
    const remaining = Math.max(0, minLoadingTime - elapsed);
    // 使用 lodash delay 或原生 setTimeout 补足时间
    _.delay(() => {
        if (loading) {
            loading.close();
        }
        loading = undefined;
    }, remaining);
}

/**
 * 显示loading层
 */
export function showLoading(): void {
    if (count == 0 && loading == undefined) {
        open();
    }
    count++;
}

/**
 * 关闭loading层
 */
export function hideLoading(): void {
    if (count <= 0) {
        return;
    }
    count--;
    if (count == 0) {
        close();
    }
}
