import type { Directive, DirectiveBinding } from "vue";
import useUserStore from "@/plugin/store/modules/useUserStore";
import CommonUtils from "@/utils/CommonUtils";

let del: boolean = false;

export default {
    created(element: HTMLElement, binging: DirectiveBinding<string>) {
        const authority = useUserStore().token.authorities;
        if (!authority.includes(binging.value)) {
            element.hidden = true;
            del = true;
        }
    },
    mounted(element: HTMLElement) {
        if (del) {
            element.id = CommonUtils.UUID();
            document.querySelector("#" + element.id)?.remove();
        }
    }
} as Directive;
