import { defineConfig } from 'vitest/config';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import { resolve } from 'node:path';

export default defineConfig({
    plugins: [svelte({ hot: false })],
    test: {
        environment: 'node',
        globals: false,
        include: ['src/**/*.test.ts']
    },
    resolve: {
        alias: {
            $lib: resolve(__dirname, 'src/lib')
        }
    }
});
