import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { mkdirSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';
import type { Plugin } from 'vite';

function browserInventory(): Plugin {
  let applicationBuild = false;
  return {
    name: 'browser-dependency-inventory',
    apply: 'build',
    configResolved(config) {
      applicationBuild = resolve(config.build.outDir) === resolve('build');
    },
    generateBundle(_options, bundle) {
      if (!applicationBuild) return;
      const packages = new Set<string>();
      const serverOnlyModules: string[] = [];
      for (const output of Object.values(bundle)) {
        if (output.type !== 'chunk') continue;
        for (const [id, module] of Object.entries(output.modules)) {
          if (module.renderedLength === 0) continue;
          const path = id.split('/node_modules/').pop();
          if (!path || path === id) continue;
          const parts = path.split('/');
          packages.add(parts[0].startsWith('@') ? parts.slice(0, 2).join('/') : parts[0]);
          if (/^(axios\/lib\/(adapters\/http|platform\/node)|follow-redirects\/|form-data\/)/.test(path)) {
            serverOnlyModules.push(path);
          }
        }
      }
      const directory = resolve('build-reports');
      mkdirSync(directory, { recursive: true });
      writeFileSync(resolve(directory, 'browser-packages.json'), JSON.stringify({
        packages: [...packages].sort(),
        serverOnlyModules: serverOnlyModules.sort(),
        files: Object.keys(bundle).sort(),
      }, null, 2) + '\n');
    },
  };
}

export default defineConfig({
  plugins: [react(), browserInventory()],
  build: {
    outDir: 'build',
    // Preserve Spring Security's public /static/** resource boundary.
    assetsDir: 'static/assets',
    target: 'baseline-widely-available',
    sourcemap: false,
  },
  server: {
    host: '127.0.0.1',
    port: 3000,
    strictPort: true,
    cors: false,
    allowedHosts: ['localhost'],
    proxy: {
      '/api': { target: 'http://localhost:8094', changeOrigin: false },
      '/oauth': { target: 'http://localhost:8094', changeOrigin: false },
      '/login': {
        target: 'http://localhost:8094',
        changeOrigin: false,
        // Browser navigation must load the current SPA, not an older backend bundle.
        bypass: (request) => request.method === 'GET' ? '/index.html' : undefined,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/setupTests.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    clearMocks: true,
    restoreMocks: true,
    maxWorkers: 1,
    fileParallelism: false,
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['src/**/*.stories.tsx', 'src/**/index.ts', 'src/app/**', 'src/Models/**', 'src/Components/Context/**'],
    },
  },
});
