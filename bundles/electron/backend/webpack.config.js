// The backend (worker-window) code runs in a Node-enabled context: node
// builtins (fs, path) and the native better-sqlite3 must resolve via
// require() at runtime instead of being bundled for the web. figwheel's
// :auto-bundle :webpack invokes `npx webpack` with CLI flags only; webpack
// picks this file up from the cwd and merges it. Without it the bundle step
// targets the web and fails on node builtins — the missing piece of the
// 3ac8251 restructure (see issue #6).
module.exports = {
  target: 'node',
  externals: {
    'better-sqlite3': 'commonjs better-sqlite3',
  },
};
