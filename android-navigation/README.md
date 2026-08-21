## Add turn-by-turn navigation to an Android app

This repo is a demo application to help developers understand how to use [Mapbox Navigation SDK for Android](https://docs.mapbox.com/android/navigation/) in an Android application using Kotlin and Jetpack Compose.

The corresponding walk-through tutorial is available on [docs.mapbox.com](https://docs.mapbox.com/help/tutorials/android-navigation/).

The app demonstrates the use of [`NavigationView`](https://docs.mapbox.com/android/navigation/v2/guides/drop-in-ui/integrate-drop-in-ui/) as a "drop-in" navigation experience for Android. The user can choose from a list of predefined destinations. Tapping one passes the current device location coordinates and the destination coordinates to `NavigationLoader`, which calculates routes. The routes are then used to render `NavigationView`, which presents turn-by-turn navigation with voice prompts in a fullscreen view.

### Requirements
- A [Mapbox Account](https://console.mapbox.com) and Access Token
- Android Studio

#### Configure your download token
Mapbox's Navigation SDK is hosted on a private Maven repository, so getting the project to build requires a **secret** token with the `Downloads:Read` scope, used only by Gradle to download the SDK. Add it to `~/.gradle/gradle.properties` (outside this repo, never committed):
```properties
MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_TOKEN
```

#### Configure your public access token
In the project explorer, right-click the `app/src/main/res/values` folder and select **New > Values Resource File**, name it `mapbox_access_token.xml`, and add your public access token:

`app/src/main/res/values/mapbox_access_token.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="mapbox_access_token" translatable="false">YOUR_MAPBOX_ACCESS_TOKEN</string>
</resources>
```

Find both tokens in the [Mapbox Console](https://console.mapbox.com).
