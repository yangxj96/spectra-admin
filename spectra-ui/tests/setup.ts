// 自动导入常用方法，避免每个测试文件都 import
import { beforeEach } from "vitest";
import { config } from "@vue/test-utils";
import createStore from "../src/plugin/store/index";
import { setActivePinia } from "pinia";

// 全局配置（如 stub 全局组件）
config.global.stubs = {
    // 例如：忽略 <router-link> 报错
    RouterLink: true
};

beforeEach(() => {
    const pinia = createStore();
    setActivePinia(pinia);
});