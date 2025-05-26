import type { App } from "vue";
// 状态
import { createStore } from "@/plugin/store";
// 路由
import router from "@/plugin/router";
// element自定义的样式文件
import "@/plugin/element/index.scss";
// element全局指令
import ElementPlus from "element-plus";

export default function loadPlugins(app: App) {
    app.use(createStore()).use(router).use(ElementPlus);
}
