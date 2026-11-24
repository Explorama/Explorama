#!/usr/bin/env node

import { readdir, readFile, writeFile } from 'fs/promises';
import { join, parse } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const SVG_DIR = join(__dirname, '..', 'src', 'img', 'svg', 'svgmin');
const OUTPUT_FILE = join(__dirname, '..', 'src', 'css', 'base', 'iconmap.css');

// Convert SVG content to data URI
function svgToDataUri(svgContent) {
  // Remove XML declaration if present
  const cleaned = svgContent.replace(/<\?xml[^?]*\?>/g, '');
  // Encode for data URI
  const encoded = encodeURIComponent(cleaned)
    .replace(/'/g, '%27')
    .replace(/"/g, '%22');
  return `data:image/svg+xml,${encoded}`;
}

async function generateSvgCss() {
  try {
    // Read all SVG files
    const files = await readdir(SVG_DIR);
    const svgFiles = files.filter(file => file.endsWith('.svg'));

    if (svgFiles.length === 0) {
      console.log('No SVG files found in', SVG_DIR);
      return;
    }

    // Process each SVG file
    const icons = [];
    for (const file of svgFiles) {
      const filePath = join(SVG_DIR, file);
      const content = await readFile(filePath, 'utf8');
      const dataUri = svgToDataUri(content);
      const name = parse(file).name;

      icons.push({ name, dataUri });
    }

    // Generate CSS: --icon-img-* custom properties + the per-icon .icon-* classes
    //
    // NOTE: the custom-property prefix is `--icon-img-`, NOT the brief's literal
    // `--icon-`. Two SVG filenames (`success`, `warning`) collide with pre-existing
    // *semantic icon-tint-color* custom properties of the same bare name
    // (`--icon-warning`/`--icon-success`, defined in base/_themes.scss, e.g.
    // `--icon-warning: var(--text-warning)`, consumed by ~50 real sites as a
    // color). Emitting `--icon-warning: url(...)` at `:root` would lose the
    // equal-specificity cascade to _themes.scss's `@media (prefers-color-scheme)`
    // :root rule (later in source order), so `.icon-warning`'s own
    // `mask-image: var(--icon-warning)` would silently resolve to a color string
    // (invalid for mask-image -> falls back to `none` -> renders as an unmasked
    // solid box) -- breaking the live `.icon-warning` glyph wired in
    // ui_base/components/misc/icon.cljs's "Status" group. The `--icon-img-`
    // prefix sidesteps the whole collision class (not just these 2 names) while
    // leaving the `.icon-<name>` CLASS names -- the actual contract -- untouched.
    const cssVars = icons.map(i => `  --icon-img-${i.name}: url("${i.dataUri}");`).join('\n');
    const cssClasses = icons.map(i =>
`.icon-${i.name} {
  display: block;
  width: .875rem;
  height: .875rem;
  background-color: var(--color-black);
  -webkit-mask-image: var(--icon-img-${i.name});
  -webkit-mask-size: contain;
  -webkit-mask-origin: content-box;
  -webkit-mask-position: center;
  -webkit-mask-repeat: no-repeat;
}`).join('\n');

    // Write to output file
    await writeFile(OUTPUT_FILE, `:root {\n${cssVars}\n}\n${cssClasses}\n`, 'utf8');
    console.log(`✓ Generated ${OUTPUT_FILE} with ${icons.length} icon vars + classes`);
  } catch (error) {
    console.error('Error generating SVG CSS:', error);
    process.exit(1);
  }
}

generateSvgCss();
