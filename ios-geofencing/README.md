## Mapbox Maps SDK for iOS Geofencing App

This repo is a demo application to help developers understand how to use the [Geofencing API](https://docs.mapbox.com/ios/maps/guides/geofencing/) in the [Mapbox Maps SDK for iOS](https://docs.mapbox.com/ios/maps).

The app demonstrates basic functionality including:

- Adding GeoJSON data to your project, visualizing it on the map
- Initializing the geofencing service and adding geofences to watch
- Imitating a user's location with sample location data and watching geofence events
- Displaying geofence events in your app and styling the geofenced area based on the event type

You can follow the tutorial step-by-step here: https://docs.mapbox.com/help/tutorials/ios-geofencing/. 

 ### Requirements
 - A [Mapbox Account](https://console.mapbox.com) and Access Token
 - Xcode

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/ios/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services.  Add your access token to the `Info.plist` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token.  Find your token in the [Mapbox Console](https://console.mapbox.com).

### Custom GeoJSON and GPX Data

This app uses custom GeoJSON data held in the `yellowstone.geojson` file in the root of this directory. Additionally, a GPX file `yellowstone_grand_loop_road`, which contains a sample user's route, is in the `Geofencing` folder. 


   
