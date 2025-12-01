# Android Offline Maps Tutorial

This project contains source code for the Mapbox tutorial [Use Offline Maps in an Android App](https://docs.mapbox.com/help/tutorials/android-offline-maps/)

## Features

- Download predefined map regions for offline use
- Track download progress in real-time
- View downloaded region sizes
- Clear all offline regions
- Material 3 UI with Jetpack Compose

## Setup

1. Clone the repository and open the project in Android Studio

2. Add your Mapbox access token in `app/src/main/res/values/mapbox_access_token.xml`:
   ```xml
   <string name="mapbox_access_token">YOUR_MAPBOX_ACCESS_TOKEN</string>
   ```

3. Sync Gradle and run the app

## Architecture

- **OfflineRegionManager** - Handles tile downloads and cache management
- **TileRegionDownloadViewModel** - Manages UI state
- **TileRegionDownloadScreen** - Jetpack Compose UI
- **OfflineRegion** - Data class for map regions
