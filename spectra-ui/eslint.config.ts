import { globalIgnores } from "eslint/config";
import { defineConfigWithVueTs, vueTsConfigs } from "@vue/eslint-config-typescript";
import pluginVue from "eslint-plugin-vue";
import skipFormatting from "eslint-config-prettier/flat";
export default defineConfigWithVueTs(
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
    vueTsConfigs.recommended,
    ...pluginVue.configs["flat/essential"],
    skipFormatting,
    {
        name: "app/files-to-lint",
        files: ["**/*.{vue,ts,mts,tsx}"],
        settings: {
            "import/resolver": {
                typescript: {
                    project: ["./tsconfig.json", "./tsconfig.app.json", "./tsconfig.node.json"]
                }
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
    {
        name: "Vue Views",
        files: ["src/views/**/*.vue"],
        rules: {
            "vue/multi-word-component-names": "off"
        }
    }
);

import importPlugin from "eslint-plugin-import";
