import { defineConfig } from "vite";

export default defineConfig({
    plugins: [],
    build: {
        outDir: "./vite-target/"
    },
    server: {
        watch: {
            // Exclude .cljs files
            // so changes dont trigger multiple reloads
            ignored: "**/*.cljs",
        },
    },
    /*  resolve: {
        alias: [
          {
            find: "@",
            replacement: fileURLToPath(new URL("./src/client", import.meta.url)),
          },
          {
            find: "@@",
            replacement: fileURLToPath(
              new URL("./src/client/components/ui", import.meta.url),
            ),
          },
        ],
      },*/
});