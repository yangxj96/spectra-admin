import { loadSlim } from "@tsparticles/slim";
import Particles from "@tsparticles/vue3";

import Icons from "@/components/Icons/index.vue";

import type { App } from "vue";

export function registerComponent(Vue: App) {
    Vue.component("components-icons", Icons);

    Vue.use(Particles, {
        init: async engine => {
            await loadSlim(engine);
        }
    });
}
