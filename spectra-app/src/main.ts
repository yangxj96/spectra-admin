import { createPinia } from "pinia";
import { createSSRApp } from "vue";
import App from "./App.vue";

import "@/static/iconfont/iconfont.css";
import "@tdesign/uniapp/theme.less";

// 注册拦截器
import "@/interceptor";

export function createApp() {
    const app = createSSRApp(App);

    const pinia = createPinia();
    app.use(pinia);

    return {
        app
    };
}
