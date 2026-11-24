// Picked up automatically by the `npx webpack` invocations that figwheel's
// :auto-bundle runs from this directory; webpack.prod.config.js pulls the
// same rules in for the production build.
//
// chartjs-adapter-date-fns resolves to a UMD file under its "require" export
// condition, but its package.json sets "type": "module", so webpack would
// treat that file as ESM: the UMD wrapper then takes its browser-global
// branch and registers the date adapter on an undefined window.Chart instead
// of the chart.js instance the app requires. javascript/auto keeps it
// CommonJS, so both end up on the same instance.
module.exports = {
  module: {
    rules: [
      {
        test: /node_modules[\\/]chartjs-adapter-date-fns[\\/]/,
        type: 'javascript/auto',
      },
    ],
  },
};
