# use-mapbox-gl-js-with-angular

This is supporting code for the Mapbox tutorial [Use Mapbox GL JS in an Angular app](https://docs.mapbox.com/help/tutorials/use-mapbox-gl-js-with-angular/).

## Overview

This tutorial walks through how to setup [Mapbox GL JS](https://docs.mapbox.com/mapbox-gl-js/) in an [Angular](https://angular.dev) project.  


You'll learn how to:
- How to set up an Angular app using the Angular CLI.
- How to install Mapbox GL JS and its dependencies.
- Use Mapbox GL JS to render a full screen map.
- How to add a toolbar which displays map state like `longitude`, `latitude`, and `zoom` level and is updated as the map is interacted with (showing the map to app data flow).
- How to create a UI button to reset the map to its original view (showing the app to map data flow).


## Prerequisites

- Node v18.20 or higher
- [Angular CLI](https://angular.dev/tools/cli)
- npm

## How to run

- Clone this repository and navigate to this directory
- Install [Angular CLI](https://angular.dev/tools/cli) (if not already installed)
- Install dependencies with `npm install`
- Replace `YOUR_MAPBOX_ACCESS_TOKEN` in `src/app/map/map.component.ts` with an access token from your [Mapbox account](https://console.mapbox.com/).
- Run the development server with `npm start` and open the app in your browser at [http://localhost:4200](http://localhost:4200).
