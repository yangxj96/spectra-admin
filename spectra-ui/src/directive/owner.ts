import { useUserStore } from "@/plugin/store/modules/use-user-store.ts";

import type { Directive, DirectiveBinding } from "vue";

/**
 * v-owner 指令
 * 支持：
 * - v-owner="'USER:INSERT'"
 * - v-owner="['USER:INSERT', 'ROLE:ADMIN']"        → AND（默认）
 * - v-owner.or="['USER:INSERT', 'USER:UPDATE']"    → OR
 */
export const owner: Directive<HTMLElement, string | string[]> = {
    mounted(el, binding) {
        checkPermission(el, binding);
    },
    updated(el, binding) {
        checkPermission(el, binding);
    }
};

function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const { value, modifiers } = binding;
    if (!value) {
        console.warn("[v-owner] 缺少绑定值");
        el.style.display = "none";
        el.remove();
        return;
    }

    const requiredPerms: string[] = Array.isArray(value) ? value : [value];
    const userStore = useUserStore();
    const hasAccess = modifiers.or
        ? requiredPerms.some(perm => userStore.hasPermission(perm))
        : requiredPerms.every(perm => userStore.hasPermission(perm));
    if (hasAccess) {
        // 恢复显示
        el.style.display = "";
    } else {
        // 彻底移除
        el.remove();
    }
}
