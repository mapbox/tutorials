# Mapbox React App

Minimal Vite + React starter that shows a Mapbox GL JS map.

Setup

1. Copy this directory or open it in VS Code.
2. Add your Mapbox token to the `.env` file as VITE_MAPBOX_TOKEN.
3. Install dependencies:

   npm install

4. Run the dev server:

   npm run dev

Notes

- This project uses `mapbox-gl` v2. If you plan to use Mapbox's official styles and raster/vector tiles, you need a valid token. Without a token, the map will still initialize but tiles may fail to load.
- For production build, follow Vite build instructions.
