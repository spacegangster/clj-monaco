const postcss = require("rollup-plugin-postcss");
const {terser} = require("rollup-plugin-terser");
const resolve = require("@rollup/plugin-node-resolve");

const production = !process.env.ROLLUP_WATCH;
const assetsPath = "public/js";

export default [
  {
    input: "src/dev/javascript/index.js",
    output: {
      dir: assetsPath,
      format: "es"
    },
    plugins: [
      resolve(),
      postcss({
        extract: true,
        minimize: production
      }),
      production && terser()
    ]
  },
  {
    input: "src/dev/javascript/monaco.js",
    output: {
      dir: assetsPath,
      format: "es",
      chunkFileNames: "[name].js"
    },
    plugins: [
      resolve(),
      postcss({
        extract: true,
        minimize: production
      }),
      production && terser()
    ]
  },
  {
    input: "node_modules/monaco-editor/esm/vs/language/json/json.worker.js",
    output: {
      file: `${assetsPath}/json.worker.js`,
      format: "umd",
      name: "json.worker"
    },
    plugins: [resolve(), production && terser()]
  },
  {
    input: "node_modules/monaco-editor/esm/vs/language/css/css.worker.js",
    output: {
      file: `${assetsPath}/css.worker.js`,
      format: "umd",
      name: "css.worker"
    },
    plugins: [resolve(), production && terser()]
  },
  {
    input: "node_modules/monaco-editor/esm/vs/language/html/html.worker.js",
    output: {
      file: `${assetsPath}/html.worker.js`,
      format: "umd",
      name: "html.worker"
    },
    plugins: [resolve(), production && terser()]
  },
  {
    input: "node_modules/monaco-editor/esm/vs/language/typescript/ts.worker",
    output: {
      file: `${assetsPath}/ts.worker.js`,
      format: "umd",
      name: "ts.worker"
    },
    plugins: [resolve(), production && terser()]
  },
  {
    input: "node_modules/monaco-editor/esm/vs/editor/editor.worker.js",
    output: {
      file: `${assetsPath}/editor.worker.js`,
      format: "umd",
      name: "editor.worker"
    },
    plugins: [resolve(), production && terser()]
  }
];
