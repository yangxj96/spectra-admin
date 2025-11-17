import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import VueDevTools from "vite-plugin-vue-devtools";
import viteCompression from "vite-plugin-compression";
import { resolve } from "path";

export default defineConfig(({ mode }) => {
    const root = process.cwd();
    const env = loadEnv(mode, root);
    console.log("环境变量:", env);
    // src路径，用于配置别名
    const srcPath = resolve(__dirname, "src");
    return {
        base: "./",
        server: {
            open: false,
            watch: {
                usePolling: true
            }
        },
        plugins: [vue(), viteCompression({}), VueDevTools()],
        resolve: {
            alias: {
                "@": srcPath
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

                        if (/\.(png|jpe?g|gif|svg|webp|avif)$/i.test(fileName)) {
                            dir = "img";
                        } else if (/\.(ttf|otf|woff2?|eot)$/i.test(fileName)) {
                            dir = "fonts";
                        } else if (/\.(mp4|webm|ogg|mp3|wav|flac|aac)$/i.test(fileName)) {
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
        },
        test: {
            // 使用 happy-dom 或 jsdom
            environment: "happy-dom", // 或 'jsdom'
            // 打印日志输出
            reporters: ["verbose"],
            silent: false,
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
