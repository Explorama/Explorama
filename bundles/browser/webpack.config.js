const webpack = require("webpack");
const path = require("path");

module.exports = {
  plugins: [
    new webpack.ProvidePlugin({
      ol: "ol",
    }),
  ],
  module: {
    rules: [
      {
        test: /node_modules\/ol-ext\/dist\/ol-ext\.js$/,
        use: {
          loader: "imports-loader",
          options: {
            imports: [
              {
                syntax: "default",
                moduleName: path.resolve(__dirname, "shim/ol-shim.js"),
                name: "ol",
              },
            ],
            wrapper: {
              thisArg: "window",
              args: {
                ol: "ol",
              },
            },
            additionalCode: "window.ol = ol;",
          },
        },
      },
    ],
  },
};
