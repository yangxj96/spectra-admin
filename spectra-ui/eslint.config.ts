import { defineConfigWithVueTs, vueTsConfigs } from "@vue/eslint-config-typescript";
import { globalIgnores } from "eslint/config";
import skipFormatting from "eslint-config-prettier/flat";
import importPlugin from "eslint-plugin-import";
import pluginVue from "eslint-plugin-vue";

// import autoImport from "./.eslintrc-auto-import.json";

// 定义VueTs版本的配置, 靠后的规则覆盖靠前的规则
export default defineConfigWithVueTs(
    // 全局忽略
    globalIgnores([
        "**/node_modules/**",
        "**/build/**",
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
    // 让 ESLint 识别自动导入变量
    // {
    //     languageOptions: {
    //         globals: autoImport.globals
    //     }
    // },
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
            // 必须使用 === / !==
            eqeqeq: "warn",
            // 禁止空代码块
            "no-empty": "error",
            // 禁止使用 var
            "no-var": "error",
            // 优先使用 const（如果变量不会被重新赋值）
            "prefer-const": "error",
            // 禁止在代码中留下 debugger
            "no-debugger": "warn",
            // 必须使用 isNaN() 判断 NaN
            "use-isnan": "error",
            // 禁止在全局作用域声明变量
            "no-implicit-globals": "error",

            // 禁止未使用的变量
            "@typescript-eslint/no-unused-vars": "error",
            // 禁止使用 any 类型
            "@typescript-eslint/no-explicit-any": "error",
            // 禁止使用 require (必须使用 import)
            "@typescript-eslint/no-var-requires": "error",
            // 禁止空对象类型 {}
            "@typescript-eslint/no-empty-object-type": "error",
            // 强制类型导入使用 import type
            "@typescript-eslint/consistent-type-imports": [
                "error",
                {
                    prefer: "type-imports",
                    fixStyle: "inline-type-imports"
                }
            ],

            // import排序
            "import/order": [
                "error",
                {
                    // import分组顺序
                    groups: ["builtin", "external", "internal", "parent", "sibling", "index", "type"],
                    // 不同分组之间必须换行
                    "newlines-between": "always",
                    // 同组import按字母排序
                    alphabetize: {
                        order: "asc",
                        caseInsensitive: true
                    },
                    // 将 @/xxx 识别为 internal
                    pathGroups: [
                        {
                            pattern: "@/**",
                            group: "internal"
                        }
                    ],
                    // 内置模块不参与pathGroups
                    pathGroupsExcludedImportTypes: ["builtin"]
                }
            ],
            // 禁止循环依赖
            "import/no-cycle": "error",
            // 检查import路径是否能解析
            "import/no-unresolved": "error",
            // 禁止重复 import
            "import/no-duplicates": "error",
            // import 后必须空一行
            "import/newline-after-import": "error",
            // import 语句必须放在文件顶部
            "import/first": "error",
            // 禁止导入自身文件
            "import/no-self-import": "error",

            // 函数最大行数限制（防止函数过长）
            "max-lines-per-function": ["warn", 200],
            // 函数最大参数数量限制
            "max-params": ["warn", 4],
            // 禁止嵌套三元表达式（可读性差）
            "no-nested-ternary": "warn"
        }
    },
    // 针对所有VUE文件的规则
    {
        name: "vue/sfc",
        files: ["**/*.vue"],
        plugins: {
            vue: pluginVue
        },
        rules: {
            // 限制Vue SFC块顺序：script -> template -> style
            "vue/block-order": ["error", { order: ["script", "template", "style"] }],
            // Vue block标签前后必须换行
            "vue/block-tag-newline": ["error", { singleline: "always", multiline: "always" }],
            // 禁止注册但未使用的组件
            "vue/no-unused-components": "error",
            // 组件在template中必须使用 PascalCase
            "vue/component-name-in-template-casing": ["error", "PascalCase"],
            // 不建议使用 v-html（可能造成XSS）
            "vue/no-v-html": "warn",
            // Vue3通常不强制要求默认prop值
            "vue/require-default-prop": "off"
        }
    },
    // views下的页面文件的规则
    {
        name: "Vue Views",
        files: ["src/views/**/*.vue"],
        rules: {
            // 页面组件允许使用单词组件名（如 Login.vue / Home.vue）
            "vue/multi-word-component-names": "off"
        }
    }
);
