import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const rootDir = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  base: './',
  build: {
    outDir: path.resolve(rootDir, '../../src/main/webapp/assets/react/order-create'),
    emptyOutDir: true,
    lib: {
      entry: path.resolve(rootDir, 'src/main.tsx'),
      formats: ['es'],
      fileName: () => 'order-create.js',
    },
    rollupOptions: {
      output: {
        chunkFileNames: 'chunks/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      },
    },
  },
});
