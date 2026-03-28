<script setup lang="ts">
import { Bell, Calendar, List } from "@element-plus/icons-vue";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// ===== 问候 =====
const greet = ref("");
const today = ref("");

const initTime = () => {
    const d = new Date();
    const h = d.getHours();

    if (h < 6) greet.value = "夜深了，注意休息";
    else if (h < 12) greet.value = "早上好";
    else if (h < 18) greet.value = "下午好";
    else greet.value = "晚上好";

    today.value = d.toLocaleDateString();
};

// ===== 快捷入口 =====
const shortcuts = [
    { name: "用户管理", path: "/system/user" },
    { name: "角色管理", path: "/system/role" },
    { name: "菜单管理", path: "/system/menu" },
    { name: "流程设计", path: "/flow/design" },
    { name: "审批中心", path: "/flow/task" },
    { name: "系统配置", path: "/system/config" }
];

const go = (path: string) => router.push(path);

// ===== 日历 / 待办 =====
const selectedDate = ref(new Date());

const todos = ref([
    { id: 1, title: "审核新用户注册", date: "2026-03-28", done: false },
    { id: 2, title: "更新权限配置", date: "2026-03-28", done: false },
    { id: 3, title: "发布系统公告", date: "2026-03-27", done: true },
    { id: 4, title: "流程审批处理", date: "2026-03-29", done: false }
]);

const selectedDateStr = computed(() => selectedDate.value.toISOString().slice(0, 10));

const filteredTodos = computed(() => todos.value.filter(t => t.date === selectedDateStr.value));

// ===== 公告 =====
const notices = ref([
    { id: 1, title: "系统升级通知" },
    { id: 2, title: "权限模块上线" },
    { id: 3, title: "流程优化完成" },
    { id: 4, title: "请完善资料" }
]);

onMounted(initTime);
</script>

<template>
    <div class="home">
        <!-- 欢迎 -->
        <el-card>
            <div>{{ greet }} 👋</div>
            <div class="sub">{{ today }}</div>
        </el-card>

        <!-- 快捷入口 -->
        <el-card>
            <div class="block-title">快捷入口</div>
            <div class="shortcut-grid">
                <div v-for="item in shortcuts" :key="item.path" class="shortcut-item" @click="go(item.path)">
                    {{ item.name }}
                </div>
            </div>
        </el-card>

        <!-- 三列布局 -->
        <div class="row">
            <!-- 日历 -->
            <el-card class="calendar-card">
                <div class="block-title">
                    <el-icon><Calendar /></el-icon>
                    日历
                </div>

                <el-calendar v-model="selectedDate" controller-type="select">
                    <template #date-cell="{ data }">
                        <div class="date-cell">
                            <span>{{ data.day.split("-")[2] }}</span>
                            <div v-if="todos.some(t => t.date === data.day)" class="dot" />
                        </div>
                    </template>
                </el-calendar>
            </el-card>

            <!-- 待办 -->
            <el-card class="todo-card">
                <div class="block-title">
                    <el-icon><List /></el-icon>
                    待办（{{ selectedDateStr }}）
                </div>

                <div class="scroll">
                    <div v-for="item in filteredTodos" :key="item.id" class="list-item">
                        <span>{{ item.title }}</span>
                        <el-tag size="small" :type="item.done ? 'success' : 'danger'">
                            {{ item.done ? "已完成" : "待处理" }}
                        </el-tag>
                    </div>

                    <div v-if="filteredTodos.length === 0" class="empty">暂无待办</div>
                </div>
            </el-card>

            <!-- 公告 -->
            <el-card class="notice-card">
                <div class="block-title">
                    <el-icon><Bell /></el-icon>
                    公告
                </div>

                <div class="scroll">
                    <div v-for="item in notices" :key="item.id" class="list-item">
                        {{ item.title }}
                    </div>
                </div>
            </el-card>
        </div>
    </div>
</template>

<style scoped lang="scss">
.home {
    height: 88vh;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 20px;
    overflow: hidden;
}

/* 标题 */
.block-title {
    font-weight: 500;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.sub {
    color: #999;
}

/* 快捷入口 */
.shortcut-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
}

.shortcut-item {
    padding: 10px;
    background: #f5f7fa;
    border-radius: 6px;
    text-align: center;
    cursor: pointer;
}

/* 三列布局 */
.row {
    display: grid;
    grid-template-columns: 1.3fr 1fr 1fr;
    gap: 12px;
    flex: 1;
}

/* 卡片 */
.calendar-card,
.todo-card,
.notice-card {
    display: flex;
    flex-direction: column;
    min-height: 0;
}

:deep(.el-calendar__body) {
    padding: 0;
}

:deep(.el-calendar-table td) {
    padding: 2px 0;
}

:deep(.el-calendar-day) {
    height: 34px;
}

/* 滚动区域 */
.scroll {
    flex: 1;
    overflow-y: auto;
}

/* 列表 */
.list-item {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    border-bottom: 1px solid #eee;
}

.empty {
    text-align: center;
    color: #999;
    padding: 20px;
}

/* 日历标记 */
.date-cell {
    position: relative;
}
.dot {
    width: 6px;
    height: 6px;
    background: red;
    border-radius: 50%;
    position: absolute;
    bottom: 2px;
    left: 50%;
    transform: translateX(-50%);
}
</style>
