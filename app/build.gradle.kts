plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    implementation("androidx.core:core-ktx:${Versions.coreKtx}")
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    implementation ("androidx.webkit:webkit:${Versions.webkit}")
    implementation ("org.apache.commons:commons-text:${Versions.apacheCommonsText}")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}")
    testImplementation("junit:junit:${Versions.juint}")
    androidTestImplementation("androidx.test.ext:junit:${Versions.extJunit}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.espressoCore}")
}