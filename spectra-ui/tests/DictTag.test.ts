import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import DictTag from "../src/components/DictTag/index.vue";


describe("DictTag", () => {
    it("renders", () => {
        const wrapper = mount(DictTag, {
            props: {
                modelValue: "1",
                primary_value: "0",
                dict_code: "sys_organization_type"
            }
        });
        let text = wrapper.text();
        console.log(text);
        expect(wrapper.text()).toContain("Hello World");
    });
});
