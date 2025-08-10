/// <reference types="vite/client" />

interface ImportMetaEnvironment {
    /** api请求地址 **/
    readonly VITE_API_URL: string;
    /**
     * 网站名称
     */
    readonly VITE_WEB_TITLE: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnvironment;
}

declare module "*.vue" {
    import type { DefineComponent } from "vue";
    const component: DefineComponent<{}, {}, any>;
    export default component;
}
