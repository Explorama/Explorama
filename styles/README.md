# Styles Build System

Modern npm scripts build system for Explorama styles

## Quick Start

```bash
./build.sh dev      # Uses npm run build
./build.sh prod     # Uses npm run build:prod
```

## Main Commands

| Command              | Description                              |
| -------------------- | ---------------------------------------- |
| `npm run dev`        | Build + watch files + live reload server |
| `npm run build`      | SVG optimization → CSS bundle → copy assets → Tailwind utilities |
| `npm run build:prod` | Build + CSS minification                 |
| `npm run init`       | Initialize repo (all build tasks)        |

## Individual Tasks

**CSS:** `css:dist` (lightningcss bundles the `src/css/style.css` manifest to `dist/css/style.css`), `css:watch` / `watch:css` (chokidar watcher)
**Tailwind:** `tailwind:dist`, `tailwind:watch` (utility classes from `src/tailwind.css`, output `dist/css/5_utilities.css`)
**SVG:** `svgmin` (optimize), `svgcss` (generate iconmap)
**Copy:** `copy:fonts`, `copy:img`, `copy:img-mosaic`, `copy:img-svg`, `copy:dist-other`, `copy:browser-dev`
**Other:** `cssmin` (minify CSS)

## Project Structure

```
src/css/            → CSS source (native-nested, bundled by lightningcss)
  base/              → base partials + generated iconmap.css
  components/        → component sheets (incl. residual *_domain.css sheets)
  style.css          → manifest (plain @import list, in sass-compiled emission order)
src/img/svg/       → SVG icons (auto-converted to base/iconmap.css)
src/fonts/         → Fonts
other/             → Pre-compiled vendor CSS
dist/              → Build output
```

## Build Pipeline

1. Minify SVGs (`svgo`)
2. Generate `base/iconmap.css` with data URIs (`svg-to-css.js`)
3. Bundle `src/css/style.css` and its `@import`s to CSS (`css:dist`, via lightningcss)
4. Copy assets to dist
5. Generate Tailwind utilities (`tailwind:dist` → `dist/css/5_utilities.css`)
6. [Production only] Minify CSS (`cssmin`, via lightningcss)

Sass has been fully retired — there is no SCSS source left in this package,
and lightningcss does not emit `.css.map` sourcemap sidecars the way sass did
(accepted dev-only loss; no equivalent flag is wired up).

`cssmin` was fixed in #11 (two separate lightningcss invocations with
unquoted globs, so the shell expands them instead of lightningcss receiving
the literal glob pattern) — `npm run build:prod` runs it and completes green.
`bb gather-assets.bb.clj prod` (used by `bundles/server/build.sh`) drives the
same `build:prod` path.
