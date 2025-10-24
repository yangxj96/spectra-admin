declare module "bpmn-js-properties-panel";

declare module "bpmn-js-i18n/translations/zn" {
    const translations: { [key: string]: string };
    export default translations;
}

declare module "camunda-bpmn-js-behaviors/lib/camunda-cloud" {
    // camunda-cloud behavior 导出的是一个模块对象（用于 DI）
    const camundaCloudBehavior: never;
    export default camundaCloudBehavior;
}

// i18n翻译方法签名
type TranslateFn = (template: string, replacements?: { [key: string]: string }) => string;

declare module "@bpmn-io/properties-panel" {
    // properties-panel 导出的是一个模块对象（包含多个服务）
    const PropertiesPanelModule: never; // 实际上是依赖注入模块

    export default PropertiesPanelModule;
}
