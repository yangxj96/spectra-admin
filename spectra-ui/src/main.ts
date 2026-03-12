import formCreate from "@form-create/element-ui";
import { loadSlim } from "@tsparticles/slim";
import Particles from "@tsparticles/vue3";
import { useDark, useToggle } from "@vueuse/core";
import ElementPlus from "element-plus";
import { createApp } from "vue";

import { registerDirectives } from "@/directive";
import router from "@/plugin/router";
import createStore from "@/plugin/store";
import { CommonUtils } from "@/utils/common-utils";

import App from "./App.vue";

import "@/plugin/element/index.scss";

CommonUtils.hasReload();

// 启用暗色模式的响应式状态
const toggleDark = useToggle(useDark());
toggleDark(CommonUtils.shouldEnableDarkMode());

// 创建APP
const app = createApp(App);

app.use(formCreate);
app.use(ElementPlus);

app.use(Particles, {
    init: async engine => {
        await loadSlim(engine);
    }
});

// 注册自定义指令
registerDirectives(app);

// 注册 store + router
app.use(createStore());
app.use(router);

// 挂载
app.mount("#app");
