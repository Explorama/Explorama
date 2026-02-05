#!/usr/bin/env node

import { readdir, readFile, writeFile, mkdir } from 'fs/promises';
import { join, parse } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';
import juice from 'juice';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const SRC_DIR = join(__dirname, '..', 'emails', 'src', 'html');
const DEST_DIR = join(__dirname, '..', 'emails', 'dist');

async function inlineCss() {
  try {
    // Ensure destination directory exists
    await mkdir(DEST_DIR, { recursive: true });

    // Read all HTML files
    const files = await readdir(SRC_DIR);
    const htmlFiles = files.filter(file => file.endsWith('.html'));

    if (htmlFiles.length === 0) {
      console.log('No HTML files found in', SRC_DIR);
      return;
    }

    // Process each HTML file
    for (const file of htmlFiles) {
      const srcPath = join(SRC_DIR, file);
      const destPath = join(DEST_DIR, file);

      const html = await readFile(srcPath, 'utf8');
      const inlined = juice(html);

      await writeFile(destPath, inlined, 'utf8');
      console.log(`✓ Inlined CSS for ${file}`);
    }

    console.log(`✓ Processed ${htmlFiles.length} email templates`);
  } catch (error) {
    console.error('Error inlining CSS:', error);
    process.exit(1);
  }
}

inlineCss();
