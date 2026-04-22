import { useDictStore } from "@/plugin/store/modules/use-dict-store.ts";

import type { FormItemRule } from "element-plus";

// 手机号码验证规则
export const mobile: FormItemRule["validator"] = (_rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入手机号"));
    }
    // 简单的中国大陆手机号正则表达式
    const reg = /^(13\d|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18\d|19[0-35-9])\d{8}$/;
    if (reg.test(value)) {
        callback();
    } else {
        callback(new Error("请输入有效的手机号"));
    }
};

// 邮箱验证规则
export const email: FormItemRule["validator"] = (_rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入邮箱地址"));
    }

    const reg = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    if (!reg.test(String(value))) {
        return callback(new Error("请输入有效的邮箱地址"));
    }

    const domain = String(value).split("@")[1];
    if (!domain) {
        return callback(new Error("请输入有效的邮箱地址"));
    }

    useDictStore()
        .getDictData("sys_email_suffix")
        .then(res => {
            const allowedSuffixes = (res ?? []).map(i => i.value);
            if (!allowedSuffixes.includes(domain)) {
                callback(new Error("不支持的邮箱类型"));
            } else {
                callback();
            }
        })
        .catch(() => {
            callback(new Error("逻辑执行错误"));
        });
};
