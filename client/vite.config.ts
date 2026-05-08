import { sveltekit } from '@sveltejs/kit/vite';
import { enhancedImages } from '@sveltejs/enhanced-img';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vite';
import { readFileSync } from 'node:fs';

const pkg = JSON.parse(readFileSync('./package.json', 'utf-8'));

export default defineConfig({
	plugins: [tailwindcss(), enhancedImages(), sveltekit()],
	define: {
		__APP_VERSION__: JSON.stringify(pkg.version)
	}
});
