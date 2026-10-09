/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#0a0c16', panel: '#111421', line: '#24283a', muted: '#9096ad',
        violet: '#8b7bff', cyan: '#65d8e8', mint: '#74dfb1', amber: '#f2bd70',
      },
      fontFamily: { sans: ['DM Sans', 'sans-serif'], display: ['Manrope', 'sans-serif'] },
      boxShadow: { glow: '0 0 50px rgba(139,123,255,.14)' },
      backgroundImage: { 'hero-grid': 'radial-gradient(rgba(144,150,173,.17) 1px, transparent 1px)' },
    },
  },
  plugins: [],
}
