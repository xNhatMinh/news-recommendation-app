# Newsora Android App

Newsora is an Android news recommendation app built with Kotlin and Jetpack Compose. It connects to the News Recommendation System backend for news, search, recommendations, authentication, saved articles, summaries, preferences, and notifications.

## Features

- Browse recent news and open article details.
- Search articles and view personalized recommendations.
- Register and sign in with email/password or Google.
- Save articles, manage topic preferences, and view notifications.
- Generate news summaries and explain terms in article content.
- Receive Firebase Cloud Messaging notifications.

## Built with

- Kotlin and Jetpack Compose
- Android Navigation Compose
- Retrofit, OkHttp, and Gson
- Firebase Cloud Messaging and Google sign-in

## Requirements

- Android Studio with Android SDK 36 installed
- JDK 17 (Android Studio's bundled JDK is suitable)
- An Android emulator or device running Android 7.0 (API 24) or newer
- The companion News Recommendation System backend
- Firebase Android configuration for this app

## Setup

1. Clone or open this project in Android Studio.
2. In Firebase, register an Android app with package name `com.example.newsrecommendation` and place its `google-services.json` in `app/`.
3. Start the backend and confirm it is reachable from the emulator or device.
4. Sync the Gradle project, then run the `app` configuration on an emulator or device.

The Google Services Gradle plugin is enabled. Firebase features require a matching `app/google-services.json` from the Firebase project. Do not put Firebase Admin service-account credentials in the Android app.

## Backend URL

The Retrofit client currently uses:

```text
http://10.0.2.2:8000/
```

`10.0.2.2` is the Android Emulator address for reaching a backend running on the development computer at port `8000`. If you use a physical device, change the base URL in `app/src/main/java/com/example/newsrecommendation/api/RetrofitClient.kt` to the computer's LAN address, and make sure the device can reach that address. For a deployed backend, use its reachable HTTPS URL.

The app currently allows cleartext HTTP for local development. Use HTTPS for a deployed backend.

## Build and run from the command line

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

## App structure

```text
app/src/main/java/com/example/newsrecommendation/
├── api/          Retrofit client and backend API interface
├── auth/         Google sign-in integration
├── model/        API request and response models
├── notification/ Firebase Messaging service
├── storage/      Local session token storage
└── ui/
    ├── navigation/
    ├── screens/
    ├── theme/
    └── viewmodel/
```
