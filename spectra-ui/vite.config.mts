import { resolve } from "path";

import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";
import { defineConfig, loadEnv } from "vite";
import viteCompression from "vite-plugin-compression2";

export default defineConfig(({ mode }) => {
    const root = process.cwd();
    const env = loadEnv(mode, root);
    if (mode === "development") {
        console.log("环境变量:", env);
    }
    const srcPath = resolve(__dirname, "src");
    return {
        base: "/",
        plugins: [
            vue(),
            vueJsx(),
            // 生产环境压缩
            mode === "production" &&
                viteCompression({
                    threshold: 10240,
                    algorithms: ["gzip", "brotliCompress"]
                })
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
            target: "esnext",
            outDir: "build",
            sourcemap: false,
            minify: "esbuild",
            cssCodeSplit: true,
            chunkSizeWarningLimit: 1500,
            esbuild: {
                drop: ["console", "debugger"]
            },
            rollupOptions: {
                output: {
                    manualChunks(id) {
                        if (!id.includes("node_modules")) return;
                        if (id.includes("vue")) return "vue";
                        if (id.includes("element-plus")) return "element";
                        if (id.includes("@form-create")) return "form-create";
                        if (id.includes("echarts")) return "echarts";
                        if (id.includes("@logicflow")) return "logicflow";
                        if (id.includes("jsoneditor")) return "jsoneditor";
                        return "vendor";
                    },
                    entryFileNames: "js/[name]-[hash].js",
                    chunkFileNames: "js/[name]-[hash].js",
                    assetFileNames(assetInfo) {
                        const ext = assetInfo.name?.split(".").pop()?.toLowerCase() ?? "";
                        const map: Record<string, string> = {
                            css: "css",

                            png: "img",
                            jpg: "img",
                            jpeg: "img",
                            gif: "img",
                            svg: "img",
                            webp: "img",
                            avif: "img",

                            woff: "fonts",
                            woff2: "fonts",
                            ttf: "fonts",
                            otf: "fonts",
                            eot: "fonts"
                        };
                        const dir = map[ext] || "other";
                        return `${dir}/[name]-[hash][extname]`;
                    }
                }
            }
        },

        test: {
            environment: "happy-dom",
            silent: false,
            reporters: "default",
            include: ["tests/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
            globals: true,
            setupFiles: "./tests/setup.ts",
            alias: {
                "@": srcPath
            },
            coverage: {
                provider: "v8",
                reporter: ["text", "json", "html"]
            }
        }
    };
});
