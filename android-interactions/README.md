## Mapbox Maps SDK for Android Interactions App

This repo is a demo application to help developers understand how to use the [Interactions API](https://docs.mapbox.com/android/maps/guides/user-interaction/interactions/) in the [Mapbox Maps SDK for Android](https://docs.mapbox.com/android/maps).

The app demonstrates basic functionality including:

- Set configuration options for the Standard Style.
- Add interactions to map elements in the Standard style using the Interactions API.
- Set a feature's feature-state to change its appearance when a user interacts with it.
- Import a style with custom data to your map at runtime.
- Add interactions to the featuresets in the imported style.
- Create a custom ViewAnnotation to display information about a selected feature.

You can follow the tutorial step-by-step here: https://docs.mapbox.com/help/tutorials/android-interactions/. 

 ### Requirements
 - A [Mapbox Account](https://console.mapbox.com) and Access Token
 - Android Studio

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/android/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services. Find the `res/values/mapbox_access_token.xml` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token. Find your token in the [Mapbox Console](https://console.mapbox.com).

### Custom Style Import

This app uses a custom style json stored in the `new-york-hotels.json` file in `app/src/main/assets`. 