// ====================
// external
// ====================
import { useDark, useToggle } from "@vueuse/core";
import ElementPlus from "element-plus";
import { createApp } from "vue";

// ====================
// internal (@/)
// ====================
import { registerComponent } from "@/components";
import { registerDirectives } from "@/directive";
import router from "@/plugin/router";
import createStore from "@/plugin/store";
import { CommonUtils } from "@/utils/common-utils";

import App from "./App.vue";

// ====================
// sibling / relative
// ====================
// ====================
// styles
// ====================
import "@/plugin/element/index.scss";

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
