import com.android.build.api.dsl.Packaging

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.jetpackcomposetest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jetpackcomposetest"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // To compile the current version of UX Framework you need to add only these three lines:
    packaging{
        resources {
            excludes += setOf(
                // To compile the current version of UX Framework you need to add only these three lines:
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "dash-sdk.properties",
            )
        }
    }

    configurations.all {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
//    implementation("com.mapbox.maps:android:11.11.0")
//    implementation("com.mapbox.navigationcore:navigation:3.9.0-beta.1")
//    implementation("com.mapbox.navigationcore:ui-maps:3.9.0-beta.1")
//    implementation("com.mapbox.extension:maps-compose:11.11.0")
    implementation("com.mapbox.navigationux:android:1.0.0-rc.3.3")
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.location)
    implementation(libs.androidx.runtime.saved.instance.state)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}