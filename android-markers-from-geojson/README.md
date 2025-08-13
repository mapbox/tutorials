## Build an Android marker app using GeoJSON data

This is supporting code for the Mapbox [tutorial on building an Android marker app using GeoJSON data](https://docs.mapbox.com/help/tutorials/android-markers-from-geojson/). This demo application helps developers understand how to use [Mapbox Maps SDK for Android](https://docs.mapbox.com/android/maps) in an Android application using Jetpack Compose with external GeoJSON data.

The app demonstrates a common UX pattern for Android mapping applications including:

- Rendering a full screen map with Mapbox Standard style using Jetpack Compose
- Loading and displaying markers from external GeoJSON data
- Adding custom markers for multiple coffee shop locations in Providence
- Implementing tap interactions to show detailed information about each marker
- Using ModalBottomSheet to present additional business information in a slide-in UI panel
- Parsing GeoJSON properties to display location details (name, address, phone number)

### Requirements
- A [Mapbox Account](https://console.mapbox.com) and Access Token
- Android Studio

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/android/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services. Find the `res/values/mapbox_access_token.xml` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token. Find your token in the [Mapbox Console](https://console.mapbox.com).

### Key Features

**GeoJSON Integration**: The app demonstrates how to load and parse external GeoJSON data containing point features for coffee shop locations, including custom properties for business information.

**Interactive Markers**: Each coffee shop location is displayed as a custom marker on the map. Users can tap markers to reveal additional information about the business.

**Modern UI with ModalBottomSheet**: Built with Jetpack Compose, the app uses Material3's ModalBottomSheet component to create a smooth slide-in interface for displaying detailed location information.

