# Styles Build System

Modern npm scripts build system for Explorama styles

## Quick Start

```bash
npm install
npm run dev          # Development with watch + live reload (port 8020)
npm run build        # Standard build
npm run build:prod   # Production build with minification
```

## Main Commands

| Command              | Description                              |
| -------------------- | ---------------------------------------- |
| `npm run dev`        | Build + watch files + live reload server |
| `npm run build`      | SVG optimization → SASS → copy assets    |
| `npm run build:prod` | Build + CSS minification                 |
| `npm run init`       | Initialize repo (all tasks + emails)     |

## Individual Tasks

**SASS:** `sass:dist`, `sass:emails`, `sass:watch`
**SVG:** `svgmin` (optimize), `svgcss` (generate iconmap)
**Copy:** `copy:fonts`, `copy:img`, `copy:img-mosaic`, `copy:img-svg`, `copy:dist-other`, `copy:browser-dev`
**Other:** `cssmin` (minify CSS), `emails` (inline CSS for email templates)

## Project Structure

```
src/scss/          → SASS source
src/img/svg/       → SVG icons (auto-converted to $imap)
src/fonts/         → Fonts
other/             → Pre-compiled vendor CSS
dist/              → Build output
```

## Build Pipeline

1. Minify SVGs (`svgo`)
2. Generate SASS iconmap with data URIs
3. Compile SCSS to CSS (`sass`)
4. Copy assets to dist
5. [Production only] Minify CSS (`lightningcss`)

## Legacy Script

```bash
./build.sh dev      # Uses npm run build
./build.sh prod     # Uses npm run build:prod
```
