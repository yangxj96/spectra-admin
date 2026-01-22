/// <reference types="vite/client" />
import "vue";
import type { Directive } from "vue";

interface ImportMetaEnvironment {
    readonly VITE_API_URL: string;
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

declare module "vue" {
    export interface GlobalDirectives {
        owner: Directive<HTMLElement, string | string[]>;
    }
}

declare module "vue-router" {
    interface RouteMeta {
        title?: string | (() => string);
    }
}

export {};
