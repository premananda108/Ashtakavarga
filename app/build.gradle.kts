plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // Добавьте KSP плагин
}

android {
    namespace = "ua.pp.soulrise.ashtakavarga"
    compileSdk = 35

    defaultConfig {
        applicationId = "ua.pp.soulrise.ashtakavarga"
        minSdk = 26
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
    // Удалите buildFeatures { compose = true } и composeOptions { ... } если они были
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx) // Добавлено для Transformations
    implementation(libs.androidx.activity.ktx)
    implementation(libs.gson) // Для сериализации JSON
    implementation(libs.androidx.appcompat) // Обновлено через toml
    implementation(libs.androidx.constraintlayout) // Через toml
    implementation(libs.mpandroidchart) // Через toml
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // Добавлено для ViewModel

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Используем ksp

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.google.material)
}