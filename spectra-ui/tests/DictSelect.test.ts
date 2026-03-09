import { createTestingPinia } from "@pinia/testing";
import { mount } from "@vue/test-utils";
import { ElOption, ElSelect } from "element-plus";
import { describe, expect, it, vi } from "vitest";

import DictSelect from "../src/components/DictSelect/index.vue";

// 在文件顶部（vitest 会自动 hoist vi.mock）
vi.mock("../src/plugin/store/modules/useDictStore", () => ({
    default: () => ({
        dicts: {
            sys_common_state: [
                { label: "正常", value: "0" },
                { label: "冻结", value: "1" },
                { label: "封禁", value: "2" }
            ]
        }
    })
}));

describe("DictSelect 组件", () => {
    it("应该正确接收并传递dict_code和model值", async () => {
        const wrapper = mount(DictSelect, {
            props: {
                modelValue: "0",
                dict_code: "sys_common_state",
                "append-to": undefined
            },
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: false
                    })
                ],
                components: {
                    ElSelect,
                    ElOption
                }
            }
        });

        // 检查props是否正确接收
        expect(wrapper.props("modelValue")).toBe("0");
        expect(wrapper.props("dict_code")).toBe("sys_common_state");
    });
});
