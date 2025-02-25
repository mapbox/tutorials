## Mapbox Maps SDK for iOS Marker App

This repo is a demo application to help developers understand how to use [Mapbox Maps SDK for iOS](https://docs.mapbox.com/ios/maps) in an iOS application using SwiftUI.

The app demonstrates basic functionality including:

- Rendering a full screen map (with a custom style)
- Listening for map events and retrieving map feature data on click
- Using map [Gestures](https://docs.mapbox.com/ios/maps/guides/user-interaction/gestures/)
- Rendering feature data in a `DrawerView`
- Resetting the map view with a UI element

 ### Requirements
 - A [Mapbox Account](https://console.mapbox.com) and Access Token
 - xCode

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/ios/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services.  Add your access token to the `Marker-App-Info.plist` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token.  Find your token in the [Mapbox Console](https://console.mapbox.com).

### Custom Map Style with an Embeded Dataset

This app uses a custom map style created in [Mapbox Studio](https://console.mapbox.com/studio) that incorporates a Tileset that has been uploaded via the [Data Manager](https://console.mapbox.com/studio/tilesets).
The tileset consists of GeoJSON data (point features) of Boston Dog Grooming locations.  The custom style also includes a font, a marker image and style rules.

While you can add data to your map at runtime, this app uses embedded data so that it can focus on presenting Maps SDK features & interactivity of the app.


   