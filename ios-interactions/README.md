## Mapbox Maps SDK for iOS Interactions App

This repo is a demo application to help developers understand how to use the [Interactions API](https://docs.mapbox.com/ios/maps/guides/user-interaction/Interactions/) in the [Mapbox Maps SDK for iOS](https://docs.mapbox.com/ios/maps).

The app demonstrates basic functionality including:

- Adding a TapInteraction to a Standard style featureset
- Adding a LongPressInteraction to remove selected features
- Importing a custom style to your map, and add a TapInteraction to one of its featuresets
- Creating a custom MapViewAnnotation

You can follow the tutorial step-by-step here: https://docs.mapbox.com/help/tutorials/ios-interactions/. 

 ### Requirements
 - A [Mapbox Account](https://console.mapbox.com) and Access Token
 - Xcode

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/ios/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services.  Add your access token to the `Info.plist` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token.  Find your token in the [Mapbox Console](https://console.mapbox.com).

### Custom New York Hotels Style

This app uses a local style file called `new-york-hotels.json`, which will be imported into the app. You can find this file in the `Interactions` folder. 


   
