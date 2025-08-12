plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.androidlocationsearch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.androidlocationsearch"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Search SDK dependencies
    implementation("com.mapbox.search:autofill:2.14.0-beta.1")
    implementation("com.mapbox.search:discover:2.14.0-beta.1")
    implementation("com.mapbox.search:place-autocomplete:2.14.0-beta.1")
    implementation("com.mapbox.search:offline:2.14.0-beta.1")
    implementation("com.mapbox.search:mapbox-search-android:2.14.0-beta.1")
    implementation("com.mapbox.search:mapbox-search-android-ui:2.14.0-beta.1")
    // If you're using compose also add the compose extension
    implementation(platform(libs.androidx.compose.bom))
    implementation("com.mapbox.extension:maps-compose:11.13.3")
    implementation("com.mapbox.maps:android:11.13.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.foundation:foundation:1.6.4")
    implementation("androidx.compose.ui:ui-text:1.6.4")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}