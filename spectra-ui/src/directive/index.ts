import { owner } from "./owner";

import type { App } from "vue";

export function registerDirectives(Vue: App) {
    Vue.directive("owner", owner);
}
