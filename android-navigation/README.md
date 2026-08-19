## Add turn-by-turn navigation to an Android app

This repo is a demo application to help developers understand how to use [Mapbox Navigation SDK for Android](https://docs.mapbox.com/android/navigation/) in an Android application using Kotlin and Jetpack Compose.

The corresponding walk-through tutorial is available on [docs.mapbox.com](https://docs.mapbox.com/help/tutorials/android-navigation/).

The app demonstrates the use of [`NavigationView`](https://docs.mapbox.com/android/navigation/v2/guides/drop-in-ui/integrate-drop-in-ui/) as a "drop-in" navigation experience for Android. The user can choose from a list of predefined destinations. Tapping one passes the current device location coordinates and the destination coordinates to `NavigationLoader`, which calculates routes. The routes are then used to render `NavigationView`, which presents turn-by-turn navigation with voice prompts in a fullscreen view.

### Requirements
- A [Mapbox Account](https://console.mapbox.com) and Access Token
- Android Studio

#### Add your Access Token
Mapbox's Navigation SDK is hosted on a private Maven repository, so getting the project to build requires **two** tokens:

- A **secret** token with the `Downloads:Read` scope, used only by Gradle to download the SDK. Add it to `~/.gradle/gradle.properties` (outside this repo, never committed):
  ```properties
  MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_TOKEN
  ```
- A **public** token, used by the app at runtime. Add it to `local.properties` in the project root (already gitignored):
  ```properties
  MAPBOX_PUBLIC_TOKEN=YOUR_PUBLIC_TOKEN
  ```

Find both tokens in the [Mapbox Console](https://console.mapbox.com).
