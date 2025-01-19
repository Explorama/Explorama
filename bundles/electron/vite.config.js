import { defineConfig } from "vite";
import react from '@vitejs/plugin-react'
import fixReactVirtualized from 'esbuild-plugin-react-virtualized'

export default defineConfig({
  optimizeDeps: {
    esbuildOptions: {
      // this should fix react virtualized https://github.com/bvaughn/react-virtualized/issues/1722
      plugins: [fixReactVirtualized],
    },
  },
  plugins: [react()],
  build: {
    minify: false,
    target: "es2020",
    terserOptions: {
      compress: false,
      mangle: false,
    },
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