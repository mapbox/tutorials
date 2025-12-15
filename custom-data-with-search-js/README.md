# custom-data-with-search-js

This is supporting code for the Mapbox tutorial [Add custom data to Mapbox Search JS](https://docs.mapbox.com/help/tutorials/custom-data-with-search-js/).

## Overview

This tutorial focuses on integrating [Mapbox GL JS](https://docs.mapbox.com/mapbox-gl-js/) and [Mapbox Search JS](https://docs.mapbox.com/mapbox-search-js/) in an [React](https://react.dev) project and adding custom data into the map's interactive Search by creating custom UI components and utilizing  [Mapbox Search JS](https://docs.mapbox.com/mapbox-search-js/) core methods & classes.


You'll learn how to:
- Created a React application using the `npm create @mapbox/web-app` command
- Imported custom airport data & created a hash map of the data for efficient indexing
- Written your own search function to return results for Airports
- Implement a custom Search Box component architecture using [`SearchBoxCore`](https://docs.mapbox.com/mapbox-search-js/api/core/search/#searchboxcore) and [`SearchSession`](https://docs.mapbox.com/mapbox-search-js/api/core/search_session/) to power the search and [TailWind CSS](https://tailwindcss.com/) to style the Search box and search suggestions
- Handle search suggestion clicks with the [`SearchSession.retrieve`](https://docs.mapbox.com/mapbox-search-js/api/core/search_session/#searchsession#retrieve) and the [`flyTo`](https://docs.mapbox.com/mapbox-gl-js/api/map/#map#flyto) method. 

Try the [finished app here](https://docs.mapbox.com/help/demos/custom-data-with-search-js/final.html).

## Prerequisites

- Node v20.20 or higher
- npm

## How to run

- Clone this repository and navigate to this directory
- Install dependencies with `npm install`
- Rename the `.env.example` file to `.env` and add your Mapbox access token from your [Mapbox account](https://console.mapbox.com/)
- Run the development server with `npm run dev` and open the app in your browser at [http://localhost:5173](http://localhost:5173).
