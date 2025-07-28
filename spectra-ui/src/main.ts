import { createApp } from "vue";
import App from "./App.vue";
// 加载相关内容
import loadPlugins from "@/plugin";
import loadDirective from "@/directive";
// iconfont
import "//at.alicdn.com/t/c/font_3119163_m7gxyti0hks.js";

// 判断是否是刷新进来的
const navigationEntries = globalThis.performance?.getEntriesByType?.("navigation");
const navigationEntry = navigationEntries?.[0] as PerformanceNavigationTiming | undefined;
if (navigationEntry?.type === "reload") {
    sessionStorage.setItem("reloaded", "true");
}

// 创建APP
const app = createApp(App);
loadPlugins(app);
loadDirective(app);
app.mount("#app");
