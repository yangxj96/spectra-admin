import uni from "@dcloudio/vite-plugin-uni";
import { resolve } from "path";
import { defineConfig } from "vite";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [uni()],
    server: {
        hmr: true
    },
    css: {
        preprocessorOptions: {
            scss: {
                silenceDeprecations: ["global-builtin", "legacy-js-api"],
                quietDeps: true
            }
        }
    },
    resolve: {
        alias: {
            "@": resolve(__dirname, "src")
        }
    }
});
