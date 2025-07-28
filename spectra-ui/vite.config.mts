import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import VueDevTools from "vite-plugin-vue-devtools";
import { fileURLToPath, URL } from "url";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import viteCompression from "vite-plugin-compression";

export default defineConfig(({ mode }) => {
    const root = process.cwd();
    const env = loadEnv(mode, root);
    console.log("环境变量:", env);
    return {
        base: "./",
        server: {
            open: false,
            watch: {
                usePolling: true
            }
        },
        plugins: [
            vue(),
            VueDevTools(),
            AutoImport({
                resolvers: [ElementPlusResolver()],
                imports: ["vue", "vue-router", "pinia"],
                dts: "./types/auto-imports.d.ts"
            }),
            Components({
                resolvers: [ElementPlusResolver()],
                dts: "./types/components.d.ts"
            }),
            viteCompression({})
        ],
        css: {
            preprocessorOptions: {
                scss: {
                    // 屏蔽sass提示高版本删除api的警告信息
                    silenceDeprecations: ["legacy-js-api"]
                }
            }
        },
        resolve: {
            alias: {
                "@": fileURLToPath(new URL("./src", import.meta.url))
            }
        },
        build: {
            minify: "terser",
            outDir: "build",
            rollupOptions: {
                output: {
                    assetFileNames: chunkInfo => {
                        // 使用 names[0] 获取文件名
                        const fileName = chunkInfo.names.length > 0 ? chunkInfo.names[0] : "";

                        let dir = "other";

                        if (/\.png|jpe?g|gif|svg|webp|avif$/i.test(fileName)) {
                            dir = "img";
                        } else if (/\.ttf|otf|woff2?|eot$/i.test(fileName)) {
                            dir = "fonts";
                        } else if (/\.mp4|webm|ogg|mp3|wav|flac|aac$/i.test(fileName)) {
                            dir = "media";
                        } else if (/\.css$/i.test(fileName)) {
                            return `css/[name]-[hash][extname]`;
                        }

                        return `assets/${dir}/[name]-[hash][extname]`;
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
                    comments: true
                }
            }
        }
    };
});
