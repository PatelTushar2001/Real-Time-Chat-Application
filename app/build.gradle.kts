plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.task1921_2_24createachatappusingfirebase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.task1921_2_24createachatappusingfirebase"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

//    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))


    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // navigation dependency
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // firebase bom dependency
    implementation(platform("com.google.firebase:firebase-bom:26.1.1"))

    // firebase auth dependency
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // firebase database dependency
    implementation("com.google.firebase:firebase-database")

    // firebase storage dependency
    implementation("com.google.firebase:firebase-storage")

    // Declare the dependency for the Cloud Firestore library
    implementation("com.google.firebase:firebase-firestore")

    //glide dependency
    implementation("com.github.bumptech.glide:glide:4.14.2")

    // messaging dependency
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-core")

    // Retrofit Dependency
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // GSON Dependency
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Moshi Converter
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")

    // Lifecycle components dependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // one-tap signin dependency
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // pdf view dependency
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")

    // dexter permission dependency
    implementation("com.karumi:dexter:6.2.2")

    // Touch Imageview dependency for pinch zoom imageview
    implementation("com.github.MikeOrtiz:TouchImageView:1.4.1")

    // zego (video-audio call) dependency
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:+")

    // file picker
//    implementation ("com.github.atwa:filepicker:1.0.7")

    // Dependency to include Maps SDK for Android
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.2.0")
}