import { http, HttpResponse } from "msw";

const prefix = import.meta.env.VITE_API_URL;

export default [
    http.get(prefix + "/api/system/menus", () => {
        return HttpResponse.json<IResult<Menu[]>>({
            code: 0,
            msg: "success",
            data: [
                {
                    id: "1909865049464242126",
                    pid: "0",
                    icon: "icon-setting",
                    name: "系统管理",
                    path: "/system",
                    component: "layout",
                    layout: "layout",
                    sort: 0,
                    children: [
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-user",
                            name: "用户管理",
                            path: "user",
                            component: "/System/User/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-setting-role",
                            name: "角色管理",
                            path: "role",
                            component: "/System/Role/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-setting-role",
                            name: "权限管理",
                            path: "power",
                            component: "/System/Power/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-setting-role",
                            name: "字典管理",
                            path: "dict",
                            component: "/System/Dict/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-setting-role",
                            name: "定时任务",
                            path: "task",
                            component: "/System/Task/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-setting-role",
                            name: "菜单管理",
                            path: "menu",
                            component: "/System/Menu/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-setting-role",
                            name: "文件存储",
                            path: "storage",
                            component: "/System/Storage/index",
                            sort: 0
                        }
                    ]
                },
                {
                    id: "1909865049464242126",
                    pid: "0",
                    icon: "icon-setting",
                    name: "组件示例",
                    path: "/example",
                    component: "layout",
                    layout: "layout",
                    sort: 0,
                    children: [
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-qq",
                            name: "图表示例",
                            path: "echarts",
                            component: "/Example/Echarts/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-qq",
                            name: "列表示例",
                            path: "table",
                            component: "/Example/Table/index",
                            sort: 0
                        },
                        {
                            id: "1909865049464242177",
                            pid: "1909865049464242176",
                            icon: "icon-qq",
                            name: "表单示例",
                            path: "form",
                            component: "/Example/Form/index",
                            sort: 0
                        }
                    ]
                }
            ]
        });
    })
];
