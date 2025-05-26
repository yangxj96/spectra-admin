// 手机号码验证规则
import type { FormItemRule } from "element-plus";

export const mobile: FormItemRule["validator"] = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入手机号"));
    }
    const reg = /^(13[0-9]|14[01456879]|15[0-3,5-9]|16[2567]|17[0-8]|18[0-9]|19[0-3,5-9])\d{8}$/; // 简单的中国大陆手机号正则表达式
    if (reg.test(value)) {
        callback();
    } else {
        callback(new Error("请输入有效的手机号"));
    }
};

export const email: FormItemRule["validator"] = (rule, value, callback) => {
    if (!value) {
        return callback(new Error("请输入邮箱地址"));
    }
    const reg = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
    if (reg.test(value)) {
        callback();
    } else {
        callback(new Error("请输入有效的邮箱地址"));
    }
};
