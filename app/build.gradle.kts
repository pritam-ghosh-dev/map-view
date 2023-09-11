plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    kotlin("kapt")
}

android {
    namespace = "com.example.map"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        applicationId = "com.example.map"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:${Versions.coreKtx}")
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    implementation ("androidx.webkit:webkit:${Versions.webkit}")
    implementation ("org.apache.commons:commons-text:${Versions.apacheCommonsText}")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}")
    implementation("androidx.room:room-runtime:${Versions.room}")
    implementation("androidx.room:room-ktx:${Versions.room}")
    annotationProcessor("androidx.room:room-compiler:${Versions.room}")
    kapt("androidx.room:room-compiler:${Versions.room}")
    testImplementation("junit:junit:${Versions.juint}")
    androidTestImplementation("androidx.test.ext:junit:${Versions.extJunit}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.espressoCore}")
}