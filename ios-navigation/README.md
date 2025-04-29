## Add turn-by-turn navigation to an iOS app

This repo is a demo application to help developers understand how to use [Mapbox Navigation SDK for iOS](https://docs.mapbox.com/ios/navigation) in an iOS application using SwiftUI.

The corresponding walk-through tutorial is available on [docs.mapbox.com](https://docs.mapbox.com/help/tutorials/ios-navigation/).

The app demonstrates the use of [`NavigationViewController`](https://docs.mapbox.com/ios/navigation/api/3.8.1/navigation/documentation/mapboxnavigationuikit/navigationviewcontroller) as a "drop-in" navigation experience for iOS. The user can choose from a list of predefined destinations. Tapping one passes the current device location coordinates and the destination coordinates to `NavigationLoader`, which calculates routes. The routes are then used to render `NavigationViewController` which presents turn-by-turn navigation with voice prompts in a dismissable fullscreen view.


 ### Requirements
 - A [Mapbox Account](https://console.mapbox.com) and Access Token
 - xCode

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/ios/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services.  Add your access token to the `Marker-App-Info.plist` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token.  Find your token in the [Mapbox Console](https://console.mapbox.com).

