import type { FormItemRule } from "element-plus";
import { useDictStore } from "@/plugin/store/modules/use-dict-store.ts";

// 手机号码验证规则
export const mobile: FormItemRule["validator"] = (rule, value, callback) => {
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
export const email = (_rule: any, value: string) => {
    return new Promise<void>((resolve, reject) => {
        if (!value) {
            reject(new Error("请输入邮箱地址"));
            return;
        }
        const reg = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
        if (!reg.test(value)) {
            reject(new Error("请输入有效的邮箱地址"));
            return;
        }

        const domain = value.split("@")[1];
        useDictStore()
            .getDictData("sys_email_suffix")
            .catch(() => reject(new Error("逻辑执行错误")))
            .then(res => {
                let allowedSuffixes = (res ?? []).map(i => i.value);
                if (!domain || !allowedSuffixes.includes(domain)) {
                    console.log(`不支持的邮箱类型`);
                    reject(new Error("不支持的邮箱类型"));
                } else {
                    resolve();
                }
            });
    });
};
