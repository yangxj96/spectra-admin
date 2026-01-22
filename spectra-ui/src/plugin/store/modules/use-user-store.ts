import { defineStore } from "pinia";

export const useUserStore = defineStore("user", {
    state: (): StoreUser => {
        return {
            token: {} as Token,
            isLoggedIn: false
        };
    },
    getters: {
        /**
         * 获取权限列表
         */
        getAuthorities(): string[] {
            return this.token.authorities || [];
        },
        /**
         * 获取角色列表
         */
        getRoles(): string[] {
            return this.token.roles.map(item => item.code) || [];
        },
        /**
         * 统一权限检查方法
         * 支持格式：
         * - 'USER:INSERT'
         * - 'ROLE:ADMIN'
         * - 'DICT:*'
         * - '*'
         */
        hasPermission(): (perm: string) => boolean {
            return (perm: string): boolean => {
                const { getAuthorities, getRoles } = this;
                // 用户拥有的所有权限标识
                const allPerms: string[] = [
                    ...getAuthorities,
                    ...getRoles.map(role => `ROLE:${role}`) // 角色转为 ROLE:XXX 格式
                ];
                // 1. 全局通配符 *
                if (allPerms.includes("*")) return true;
                if (perm === "*") return true;
                return allPerms.some(userPerm => {
                    // 精确匹配
                    if (userPerm === perm) return true;
                    // 通配符模块匹配：DICT:* 匹配 DICT:INSERT
                    if (userPerm.endsWith(":*")) {
                        const module = userPerm.slice(0, -2);
                        return perm.startsWith(`${module}:`);
                    }
                    return false;
                });
            };
        },
        /**
         * 批量检查权限（用于 v-permission="[...]"）
         */
        hasAllPermissions(): (perms: string[]) => boolean {
            return (perms: string[]): boolean => {
                return perms.every(perm => this.hasPermission(perm));
            };
        }
    },
    persist: true
});
