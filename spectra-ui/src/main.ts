// ====================
// external
// ====================
import { createApp } from "vue";
import ElementPlus from "element-plus";
import { useDark, useToggle } from "@vueuse/core";

// ====================
// internal (@/)
// ====================
import App from "./App.vue";
import createStore from "@/plugin/store";
import router from "@/plugin/router";
import { registerDirectives } from "@/directive";
import { registerComponent } from "@/components";
import { CommonUtils } from "@/utils/common-utils";

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
