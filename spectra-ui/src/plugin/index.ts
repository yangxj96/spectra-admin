/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import type { App } from "vue";
// 状态
import createStore from "@/plugin/store";
// 路由
import router from "@/plugin/router";
// element自定义的样式文件
import "@/plugin/element/index.scss";
// 使用 vueuse 控制深色模式
import { useDark, useToggle } from "@vueuse/core";
import CommonUtils from "@/utils/CommonUtils.ts";

// 启用暗色模式的响应式状态
const isDark = useDark();
const toggleDark = useToggle(isDark);
toggleDark(CommonUtils.shouldEnableDarkMode());

export default function loadPlugins(app: App) {
    app.use(createStore()).use(router);
}
