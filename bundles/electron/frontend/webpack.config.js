// Browser bundle for the electron UI process. webpack-cli 6's -o flag only
// sets the output directory (no --output-filename flag), so the frontend.js
// name index.html expects is fixed here.
module.exports = {
  mode: 'production',
  output: {
    filename: 'frontend.js',
  },
  module: {
    rules: [
      {
        // chartjs-adapter-date-fns resolves to a UMD file under its "require"
        // export condition, but its package.json sets "type": "module", so
        // webpack would treat that file as ESM: the UMD wrapper then takes its
        // browser-global branch and registers the date adapter on an undefined
        // window.Chart instead of the chart.js instance the app requires.
        // javascript/auto keeps it CommonJS, so both end up on the same
        // instance.
        test: /node_modules[\\/]chartjs-adapter-date-fns[\\/]/,
        type: 'javascript/auto',
      },
    ],
  },
};
