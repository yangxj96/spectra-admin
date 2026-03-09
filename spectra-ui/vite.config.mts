import { resolve } from "path";

import vue from "@vitejs/plugin-vue";
import VueJsx from "@vitejs/plugin-vue-jsx";
import { defineConfig, loadEnv } from "vite";
import viteCompression from "vite-plugin-compression";
import VueDevTools from "vite-plugin-vue-devtools";

export default defineConfig(({ mode }) => {
    if (mode === "development") {
        const root = process.cwd();
        const env = loadEnv(mode, root);
        console.log("环境变量:", env);
    }
    // src路径，用于配置别名
    const srcPath = resolve(__dirname, "src");
    return {
        base: "/",
        server: {
            open: false,
            watch: {
                usePolling: true
            }
        },
        plugins: [
            vue(),
            VueJsx(),
            mode === "production" && viteCompression({ threshold: 10240 }),
            mode === "development" && VueDevTools()
        ].filter(Boolean),
        resolve: {
            alias: {
                "@": srcPath
            }
        },
        css: {
            preprocessorOptions: {
                scss: {
                    quietDeps: true
                }
            }
        },
        build: {
            sourcemap: false,
            target: "es2018",
            minify: "terser",
            outDir: "build",
            rollupOptions: {
                output: {
                    manualChunks: {
                        vue: ["vue", "vue-router", "pinia"]
                    },
                    assetFileNames: assetInfo => {
                        const ext = assetInfo.name?.split(".").pop();

                        if (/png|jpe?g|gif|svg|webp|avif/i.test(ext ?? "")) {
                            return "assets/img/[name]-[hash][extname]";
                        }

                        if (/woff2?|ttf|otf|eot/i.test(ext ?? "")) {
                            return "assets/fonts/[name]-[hash][extname]";
                        }

                        if (ext === "css") {
                            return "css/[name]-[hash][extname]";
                        }

                        return "assets/other/[name]-[hash][extname]";
                    },

                    chunkFileNames: "assets/js/[name]-[hash].js",
                    entryFileNames: "assets/js/[name]-[hash].js"
                }
            },
            terserOptions: {
                compress: {
                    // 移除所有的 console.* 调用
                    drop_console: true,
                    // 移除 debugger 语句
                    drop_debugger: true,
                    // 更细粒度控制
                    pure_funcs: ["console.log", "console.info", "console.warn", "console.error"]
                },
                format: {
                    // 移除注释
                    comments: false
                }
            }
        },
        test: {
            // 使用 happy-dom 或 jsdom
            environment: "happy-dom", // 或 'jsdom'
            // 打印日志输出
            silent: false,
            reporters: "default",
            // 匹配测试文件
            include: ["tests/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
            // 全局注册 Vue Test Utils 的 API（可选）
            globals: true,
            // 支持 setup 文件
            setupFiles: "./tests/setup.ts",
            alias: {
                "@": srcPath
            },
            // 覆盖率（可选）
            coverage: {
                provider: "v8", // or 'istanbul'
                reporter: ["text", "json", "html"]
            }
        }
    };
});
