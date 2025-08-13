## Add Location Search to an iOS app

This is supporting code for the Mapbox [tutorial on adding location search to an iOS app](https://docs.mapbox.com/help/tutorials/ios-location-search). This demo application helps developers understand how to use [Mapbox Search SDK for iOS](https://docs.mapbox.com/ios/search) with the [Mapbox Maps SDK for iOS](https://docs.mapbox.com/ios/maps) in an iOS application using SwiftUI.

The app demonstrates basic functionality including:

- Rendering a full screen map with Mapbox Standard style
- Implementing location search with autocomplete using Mapbox Search SDK
- Creating a floating search interface with real-time suggestions
- Adding markers to the map for selected search results
- Animating the map camera to fly to selected locations with smooth transitions
- Geographic search biasing with bounding box and proximity parameters

### Requirements
- A [Mapbox Account](https://console.mapbox.com) and Access Token
- Xcode

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/ios/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services. Add your access token to the `Info.plist` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token. Find your token in the [Mapbox Console](https://console.mapbox.com).

### Key Features

**Location Search**: The app uses Mapbox's PlaceAutocomplete API to provide real-time search suggestions as users type. The search is geographically biased to the Toronto, Canada area for demonstration purposes, but can be easily modified for any region.

**Interactive UI**: Built with SwiftUI, the app features a floating search interface that overlays the map, providing an intuitive user experience for location discovery.

**Map Integration**: Selected search results are displayed as custom markers on the map, with smooth camera animations that fly to the selected location at an appropriate zoom level.

**Comprehensive Comments**: The code includes detailed explanatory comments making it perfect for learning how to integrate Mapbox Search functionality into iOS applications.
