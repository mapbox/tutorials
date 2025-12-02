# Getting Started with Maps in React Native Tutorial

This project contains source code for the Mapbox tutorial [Getting Started with Maps in React Native](https://docs.mapbox.com/help/tutorials/getting-started-react-native/)

## Features

- Add @rnmapbox/maps to your React Native project
- Display a map with a custom camera position
- Configure the Standard style with custom lighting
- Display your own data on the map with custom styling
- Enable user location with a pulsing indicator

## Prerequisites

- Node.js and npm/yarn
- React Native development environment set up
- Xcode (for iOS development)
- Android Studio (for Android development)
- Mapbox account and access token

## Setup

1. Clone the repository and navigate to the project:
   ```sh
   cd getting-started-react-native
   ```

2. Install dependencies:
   ```sh
   npm install
   # OR
   yarn install
   ```

3. Add your Mapbox access token:
   - Android: Replace YOUR_MAPBOX_ACCESS_TOKEN in `android/app/src/main/res/values/mapbox_access_token.xml` with your access token,
   - iOS: Replace YOUR_MAPBOX_ACCESS_TOKEN in `ios/Info.plist` with your access token.

4. Install iOS dependencies (iOS only):
   ```sh
   cd ios && pod install
   ```

## Running the App

### Android
```sh
npm run android
# OR
yarn android
```

### iOS
```sh
npm run ios
# OR
yarn ios
```

## Learn More

- [Maps SDK for React Native](https://rnmapbox.github.io/)
