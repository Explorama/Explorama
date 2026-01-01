#!/usr/bin/env node

import { readdir, readFile, writeFile } from 'fs/promises';
import { join, parse } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const SVG_DIR = join(__dirname, '..', 'src', 'img', 'svg', 'svgmin');
const OUTPUT_FILE = join(__dirname, '..', 'src', 'scss', 'base', '_iconmap.scss');

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

    // Generate SASS map
    const sassMap = `$imap:(\n${icons.map(icon => `${icon.name}: '${icon.dataUri}',`).join('\n')}\n)\n`;

    // Write to output file
    await writeFile(OUTPUT_FILE, sassMap, 'utf8');
    console.log(`✓ Generated ${OUTPUT_FILE} with ${icons.length} icons`);
  } catch (error) {
    console.error('Error generating SVG CSS:', error);
    process.exit(1);
  }
}

generateSvgCss();
