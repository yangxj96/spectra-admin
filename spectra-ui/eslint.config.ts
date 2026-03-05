import { globalIgnores } from "eslint/config";
import { defineConfigWithVueTs, vueTsConfigs } from "@vue/eslint-config-typescript";
import pluginVue from "eslint-plugin-vue";
import skipFormatting from "eslint-config-prettier/flat";
import importPlugin from "eslint-plugin-import";

// 定义VueTs版本的配置, 靠后的规则覆盖靠前的规则
export default defineConfigWithVueTs(
    // 全局忽略
    globalIgnores([
        "**/node_modules/**",
        "**/dist/**",
        "**/dist-ssr/**",
        "**/coverage/**",
        "**/.output/**",
        "**/.vite/**",
        "**/public/**",
        "**/*.d.ts"
    ]),
    // ts的recommended
    vueTsConfigs.recommended,
    // flat的recommended
    ...pluginVue.configs["flat/essential"],
    // 跳过格式化,格式化交给prettier
    skipFormatting,
    // vue,ts,mts,tsx文件的规则
    {
        name: "app/files-to-lint",
        files: ["**/*.{vue,ts,mts,tsx}"],
        settings: {
            "import/resolver": {
                typescript: {
                    alwaysTryTypes: true,
                    project: ["./tsconfig.app.json"]
                },
                node: true
            }
        },
        plugins: {
            import: importPlugin
        },
        rules: {
            eqeqeq: "warn",
            "no-empty": "error",
            "no-var": "error",
            "use-isnan": "error",
            "no-implicit-globals": "error",

            "@typescript-eslint/no-unused-vars": "error",
            "@typescript-eslint/no-explicit-any": "error",
            "@typescript-eslint/no-var-requires": "error",
            "@typescript-eslint/no-empty-object-type": "error",

            "import/order": "error",
            "import/no-cycle": "error",
            "import/no-unresolved": "error"
        }
    },
    // views下的页面文件的规则
    {
        name: "Vue Views",
        files: ["src/views/**/*.vue"],
        rules: {
            "vue/multi-word-component-names": "off"
        }
    }
);
