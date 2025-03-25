# dynamic-markers-react

This is supporting code for the Mapbox turorial [Add Dynamic Markers and Popups to a Map in a React app](https://docs.mapbox.com/help/tutorials/dynamic-markers-react/).

## Overview

This Mapbox tutorial walks through how to integrate [Mapbox GL JS](https://docs.mapbox.com/mapbox-gl-js/) with custom React components to create a dynamic map with custom markers and popups using external data sources. 

You'll learn how to:
- Build a React component to display a Mapbox GL JS map.
- Query [geoJSON data](https://docs.mapbox.com/glossary/geojson) from an external API using the map's current bounds.
- Add markers to the map using a composable React component.
- Use React state to add an active state to a marker on click.
- Show a popup for the active marker using a composable React component.

The combination of markers, popups, and bounds-based data is commonly used in real estate and store locator apps where users are exploring point features on an interactive map.

The finished product shows earthquake data from the USGS API on a map, with magnitude data shown in each marker. When the user drags the map, new data is fetched for the current map bounds. When the user clicks on a marker, its style is changed to show that it is active, and a popup appears with additional data about the earthquake.

## How to run

Prerequisites: Node.js v18 or higher and npm

- Clone this repository and navigate to this directory
- Install dependencies `npm install`
- Replace `YOUR_MAPBOX_ACCESS_TOKEN` in `src/Map.jsx` with an access token from your Mapbox account.
- Run the vite development server `npm run dev` and open the app in your browser.
