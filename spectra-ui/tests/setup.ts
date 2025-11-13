// 自动导入常用方法，避免每个测试文件都 import
import { config } from "@vue/test-utils";

// 全局配置（如 stub 全局组件）
config.global.stubs = {
    // 例如：忽略 <router-link> 报错
    RouterLink: true
};
