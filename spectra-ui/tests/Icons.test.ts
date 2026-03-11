import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import Icons from "@/components/ComponentsIcons/index.vue";

describe("component icons test", () => {
    // 测试渲染图标
    it("render icon", async () => {
        const wrapper = mount(Icons, {
            props: {
                name: "home"
            }
        });

        await flushPromises();

        console.log(wrapper.html());

        expect(wrapper.exists()).toBe(true);
        expect(wrapper.html()).toContain("home");
    });

    it("name 不存在时不应报错", () => {
        expect(() => {
            mount(Icons, {
                props: {
                    name: "not-exist-icon"
                }
            });
        }).not.toThrow();
    });
});
