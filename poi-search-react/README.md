# poi-search

This is supporting code for the Mapbox tutorial [Add Point of Interest (POI) Search to a Map in a React app](https://docs.mapbox.com/help/tutorials/poi-search-react/).

## Overview

This Mapbox tutorial walks through how to integrate Points of Interest (POI) search results from the [Mapbox Search Box API](https://docs.mapbox.com/api/search/search-box/) into a React app, allowing the user to discover nearby businesses and services for a given map location.


You'll learn how to:
- Build UI to trigger category search queries in a mapping application.
- Use the `SearchBoxCore` component of [Mapbox Search JS](https://docs.mapbox.com/mapbox-search-js/api/core/search/#searchboxcore) to search for POIs.
- Get the map's bounds and use them as a search option to limit results to the current map view. 
- Use a React component to add custom markers and popups to the map for each point of interest in the search results.
- Add a "search this area" button to trigger another category search if the user moves the map.

Buttons on a map to search for nearby points of interest is a common UX pattern in mapping applications and can be useful for users to find nearby businesses, services, or other locations of interest. For example, in a real estate app, users may want to explore nearby schools, parks, or grocery stores. In a travel app, users may want to find nearby restaurants, hotels, or attractions.

The Mapbox [Search Box API](https://docs.mapbox.com/api/search/search-box/) provides a powerful and flexible way to search for points of interest, and the `SearchBoxCore` component of *Mapbox Search JS* allows for seamless integration in a JavaScript environment.


## How to run

Prerequisites: Node.js v18 or higher and npm

- Clone this repository and navigate to this directory
- Install dependencies `npm install`
- Replace `YOUR_MAPBOX_ACCESS_TOKEN` in `src/App.jsx` with an access token from your [Mapbox account](https://console.mapbox.com/).
- Run the vite development server `npm run dev` and open the app in your browser.
