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

import { createApp } from "vue";
import App from "./App.vue";
// 加载相关内容
import loadPlugins from "@/plugin";
// iconfont
import "//at.alicdn.com/t/c/font_3119163_m7gxyti0hks.js";
import Owner from "@/directive/Owner.ts";

// 判断是否是刷新进来的
const navigationEntries = globalThis.performance?.getEntriesByType?.("navigation");
const navigationEntry = navigationEntries?.[0] as PerformanceNavigationTiming | undefined;
if (navigationEntry?.type === "reload") {
    sessionStorage.setItem("reloaded", "true");
}

// 创建APP
const app = createApp(App);
loadPlugins(app);
app.directive("owner", Owner).mount("#app");
