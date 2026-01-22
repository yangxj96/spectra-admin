import { ElMessage, ElMessageBox, ElNotification, type MessageOptions, type MessageType } from "element-plus";

/* ===================== 公用部分 ===================== */

// ElMessage默认配置
const DEFAULT_OPTIONS: Partial<MessageOptions> = {
    appendTo: ".box-content",
    showClose: true,
    duration: 3000
};

/**
 * 标准化错误信息
 *
 * @param message 消息
 */
function resolveMessage(message: unknown): string {
    if (message instanceof Error) return message.message;
    if (typeof message === "string") return message;
    return "未知错误";
}

/* ===================== ElMessage ===================== */

/**
 * 创建消息
 * @param type 消息类型
 */
function createMessage(type: MessageType) {
    return (message: unknown, onClose?: CloseCallback, options?: Partial<MessageOptions>) => {
        ElMessage({
            ...DEFAULT_OPTIONS,
            ...options,
            type,
            message: resolveMessage(message),
            onClose
        });
    };
}

/* ===================== ElNotification ===================== */

/**
 * 创建通知
 * @param type 消息类型
 */
function createNotification(type: MessageType) {
    return (message: unknown, title = "提示", options?: Partial<NotificationOptions>) => {
        ElNotification({
            duration: 4e3,
            ...options,
            type,
            title,
            message: resolveMessage(message)
        });
    };
}

/* ===================== ElMessageBox ===================== */

/**
 * ElMessageBox-confirm
 * @param message 消息
 * @param title 标题,默认确认
 * @param options 配置
 */
async function confirm(
    message: unknown,
    title = "确认",
    options?: Partial<Parameters<typeof ElMessageBox.confirm>[2]>
) {
    return ElMessageBox.confirm(resolveMessage(message), title, {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        appendTo: ".box-content",
        ...options
    });
}

/**
 * ElMessageBox-alert
 * @param message 消息
 * @param title 标题,默认提示
 * @param options 配置
 */
async function alert(message: unknown, title = "提示", options?: Partial<Parameters<typeof ElMessageBox.alert>[2]>) {
    return ElMessageBox.alert(resolveMessage(message), title, {
        confirmButtonText: "确定",
        ...options
    });
}

/**
 * ElMessageBox-prompt
 * @param message 消息
 * @param title 标题,默认输入
 * @param options 配置
 */
async function prompt(message: unknown, title = "输入", options?: Partial<Parameters<typeof ElMessageBox.prompt>[2]>) {
    return ElMessageBox.prompt(resolveMessage(message), title, {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        ...options
    });
}

/* ===================== 统一出口 ===================== */

// 具体方法
export const MessageUtils = {
    /* ElMessage */
    success: createMessage("success"),
    error: createMessage("error"),
    warning: createMessage("warning"),
    info: createMessage("info"),

    /* ElNotification */
    notify: {
        success: createNotification("success"),
        error: createNotification("error"),
        warning: createNotification("warning"),
        info: createNotification("info")
    },

    /* ElMessageBox */
    box: {
        confirm,
        alert,
        prompt
    }
};
