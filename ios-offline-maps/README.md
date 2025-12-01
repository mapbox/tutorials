# iOS Offline Maps Tutorial

This project contains source code for the Mapbox tutorial [Use Offline Maps in an iOS App](https://docs.mapbox.com/help/tutorials/ios-offline-maps/)

## Features

- Download predefined map regions for offline use
- Track download progress in real-time
- View downloaded region sizes
- Clear all offline regions
- SwiftUI interface

## Setup

1. Clone the repository and open the project in Xcode

2. Add your Mapbox access token:
   - Update the `MBXAccessToken` key in `Info.plist`. Its value should be a valid Mapbox public access token.

3. Build and run the app

## Architecture

- **OfflineRegionManager** - Handles tile downloads and cache management
- **TileRegionDownloadViewModel** - Manages UI state
- **TileRegionDownloadView** - SwiftUI interface
- **OfflineRegion** - Struct for map regions