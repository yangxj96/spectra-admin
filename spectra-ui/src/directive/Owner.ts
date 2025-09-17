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

import type { Directive } from "vue";
import useUserStore from "@/plugin/store/modules/useUserStore.ts";

/**
 * v-owner 指令
 * 支持：
 * - v-owner="'USER:INSERT'"
 * - v-owner="['USER:INSERT', 'ROLE:ADMIN']"        → AND（默认）
 * - v-owner.or="['USER:INSERT', 'USER:UPDATE']"    → OR
 */
export default {
    mounted(el, binding) {
        checkPermission(el, binding);
    },
    // 可选
    updated(el, binding) {
        checkPermission(el, binding);
    }
} as Directive;

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const userStore = useUserStore();
    const { value, modifiers } = binding;
    if (!value) {
        console.warn("[v-owner] 缺少绑定值");
        el.remove();
        return;
    }
    const requiredPerms: string[] = Array.isArray(value) ? value : [value];
    let hasAccess: boolean;
    hasAccess = modifiers.or
        ? requiredPerms.some(perm => userStore.hasPermission(perm))
        : requiredPerms.every(perm => userStore.hasPermission(perm));
    if (hasAccess) {
        el.style.display = ""; // 恢复显示
    } else {
        // el.style.display = "none"; // 隐藏（保留 DOM 结构）
        el.remove(); // 或使用 el.remove() 彻底移除
    }
}
