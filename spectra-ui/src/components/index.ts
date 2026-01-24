import type { App } from "vue";
import Icons from "@/components/Icons/index.vue";

export function registerComponent(Vue: App) {
    Vue.component("components-icons", Icons);
}
