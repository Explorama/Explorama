// Production bundling of the :simple-optimized ClojureScript output.
// Invoked by the :bundle-cmd in prod-opts.edn (via `clojure -M:prod ...`).
const path = require('path');
const shared = require('./webpack.config');

module.exports = {
  mode: 'production',
  entry: './resources/public/js/out/main.js',
  output: {
    path: path.resolve(__dirname, 'resources/public/js/out'),
    filename: 'main_bundle.js',
  },
  module: {
    rules: [
      ...shared.module.rules,
      {
        // The Closure-compiled output binds goog.global via `this || self`,
        // but inside webpack's CommonJS wrapper `this` is the exports object.
        // Run the module with `this` = window so goog.global is the real
        // global object (exported namespaces, timers, XMLHttpRequest).
        test: path.resolve(__dirname, 'resources/public/js/out/main.js'),
        use: [{ loader: 'imports-loader', options: { wrapper: 'window' } }],
      },
    ],
  },
  resolve: {
    fallback: {
      // Node-only fallback branch in cljs-ajax (ajax.xml-http-request);
      // unreachable in the browser, but webpack must resolve the require.
      xmlhttprequest: false,
    },
  },
};
