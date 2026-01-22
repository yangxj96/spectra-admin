import type { App } from "vue";
import { owner } from "./owner";

export function registerDirectives(Vue: App) {
    Vue.directive("owner", owner);
}
