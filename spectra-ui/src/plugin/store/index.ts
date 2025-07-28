import { createPinia } from "pinia";
import { createPersistedState } from "pinia-plugin-persistedstate";

export default function createStore() {
    const pinia = createPinia();
    pinia.use(createPersistedState({ storage: localStorage }));
    return pinia;
}
