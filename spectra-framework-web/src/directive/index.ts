import type { App } from "vue";
// 自定义指令
import Owner from "@/directive/Owner";

export default function loadDirective(app: App) {
    app.directive("owner", Owner);
}
