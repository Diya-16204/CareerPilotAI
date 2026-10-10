import { configDefaults, defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
export default defineConfig({
    plugins: [tailwindcss(), react()],
    server: { port: 5173 },
    test: { environment: 'jsdom', setupFiles: './src/test/setup.js', exclude: [...configDefaults.exclude, 'e2e/**'] },
});
