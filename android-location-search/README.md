## Add Location Search to an Android app

This is supporting code for the Mapbox [tutorial on adding location search to an Android app](https://docs.mapbox.com/help/tutorials/android-location-search). This demo application helps developers understand how to use [Mapbox Search SDK for Android](https://docs.mapbox.com/android/search) with the [Mapbox Maps SDK for Android](https://docs.mapbox.com/android/maps) in an Android application using Jetpack Compose.

The app demonstrates basic functionality including:

- Rendering a full screen map with Mapbox Standard style using Jetpack Compose
- Implementing location search with autocomplete using Mapbox Search SDK
- Creating a floating search interface with real-time suggestions
- Adding markers to the map for selected search results using PointAnnotation
- Animating the map camera to fly to selected locations with smooth transitions
- Search suggestion handling with proper result selection callbacks

### Requirements
- A [Mapbox Account](https://console.mapbox.com) and Access Token
- Android Studio

#### Add your Access Token 
As outlined in the [Install Guide](https://docs.mapbox.com/android/maps/guides/install/) you **must** configure your public token to make requests to Mapbox services. Find the `res/values/mapbox_access_token.xml` file and replace `YOUR_MAPBOX_ACCESS_TOKEN` with your public token. Find your token in the [Mapbox Console](https://console.mapbox.com).

### Key Features

**Location Search**: The app uses Mapbox's Search Engine with SearchBox API to provide real-time search suggestions as users type. The implementation includes proper callback handling for both suggestions and result selection.

**Modern UI**: Built with Jetpack Compose, the app features a floating search interface that overlays the map using Box layout and zIndex for proper layering.

**Map Integration**: Selected search results are displayed as custom markers on the map using PointAnnotation, with MapViewportState handling smooth camera animations to the selected location.

**Comprehensive Implementation**: The code demonstrates proper integration patterns for combining Mapbox Search SDK with Maps SDK in a Compose-based Android application, including state management and UI composition best practices.
