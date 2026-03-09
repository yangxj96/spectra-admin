import { flushPromises, mount } from "@vue/test-utils";
import { ElTag } from "element-plus";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it } from "vitest";

import { useDictStore } from "@/plugin/store/modules/use-dict-store.ts";

import DictTag from "../src/components/DictTag/index.vue";

describe("DictTag.vue", () => {
    let pinia: ReturnType<typeof createPinia>;

    beforeEach(() => {
        pinia = createPinia();
        setActivePinia(pinia);

        // 预设字典数据，避免异步加载
        const store = useDictStore();
        store.dicts = {
            sys_organization_type: [
                { id: "", gid: "", value: "0", label: "总部", sort: 0, state: 0, remark: "", default_flag: false },
                { id: "", gid: "", value: "1", label: "子公司", sort: 0, state: 0, remark: "", default_flag: false }
            ]
        };
    });

    it("当 modelValue 等于 primary_value 时，显示 primary 样式", async () => {
        const wrapper = mount(DictTag, {
            props: {
                modelValue: "0",
                primary_value: "0",
                dict_code: "sys_organization_type"
            },
            global: { plugins: [pinia], components: { ElTag } }
        });

        await flushPromises();

        const tag = wrapper.findComponent(ElTag);
        expect(tag.exists()).toBe(true);
        expect(tag.text()).toBe("总部");
        expect(tag.props("type")).toBe("primary"); // 或检查 class: .toContain('el-tag--primary')
    });

    it("当 modelValue 不等于 primary_value 时，显示 danger 样式", async () => {
        const wrapper = mount(DictTag, {
            props: {
                modelValue: "1",
                primary_value: "0", // 注意：不相等
                dict_code: "sys_organization_type"
            },
            global: { plugins: [pinia], components: { ElTag } }
        });

        await flushPromises();

        const tag = wrapper.findComponent(ElTag);
        expect(tag.exists()).toBe(true);
        expect(tag.text()).toBe("子公司");
        expect(tag.props("type")).toBe("danger");
    });

    it("未传 primary_value 时，默认使用 primary 样式", async () => {
        const wrapper = mount(DictTag, {
            props: {
                modelValue: "1",
                dict_code: "sys_organization_type"
                // 未传 primary_value
            },
            global: { plugins: [pinia], components: { ElTag } }
        });

        await flushPromises();

        const tag = wrapper.findComponent(ElTag);
        expect(tag.exists()).toBe(true);
        expect(tag.text()).toBe("子公司");
        expect(tag.props("type")).toBe("primary");
    });
});
