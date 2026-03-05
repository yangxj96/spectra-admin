import type { App } from "vue";
import Particles from "@tsparticles/vue3";
import { loadSlim } from "@tsparticles/slim";
import Icons from "@/components/Icons/index.vue";

export function registerComponent(Vue: App) {
    Vue.component("components-icons", Icons);

    Vue.use(Particles, {
        init: async engine => {
            await loadSlim(engine);
        }
    });
}
