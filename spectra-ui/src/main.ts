import { createApp } from "vue";
import App from "./App.vue";
// 加载相关内容
// 状态
import createStore from "@/plugin/store";
// 路由
import router from "@/plugin/router";
// element自定义的样式文件
import ElementPlus from "element-plus";
import "@/plugin/element/index.scss";
// 自定义指令
import { registerDirectives } from "@/directive";
// 全局组件
import { registerComponent } from "@/components/index.ts";

// 使用 vueuse 控制深色模式
import { useDark, useToggle } from "@vueuse/core";
// 工具类
import { CommonUtils } from "@/utils/common-utils.ts";

CommonUtils.hasReload();

// 启用暗色模式的响应式状态
const toggleDark = useToggle(useDark());
toggleDark(CommonUtils.shouldEnableDarkMode());

// 创建APP
const app = createApp(App);

// 注册自定义指令
registerDirectives(app);

// 全局组件
registerComponent(app);

app.use(createStore()).use(router).use(ElementPlus).mount("#app");
