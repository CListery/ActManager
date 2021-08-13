import com.clistery.gradle.AppConfig
import com.clistery.gradle.AppDependencies
import com.clistery.gradle.implementation

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdkVersion(AppConfig.compileSdk)
    buildToolsVersion(AppConfig.buildToolsVersion)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    defaultConfig {
        applicationId = "com.yh.demo"
        minSdkVersion(AppConfig.minSdk)
        targetSdkVersion(AppConfig.targetSdk)
        versionCode(AppConfig.versionCode)
        versionName(AppConfig.versionName)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(AppDependencies.baseLibs)
    implementation(AppDependencies.androidx.appcompat)
    implementation(AppDependencies.google.material)
    implementation(AppDependencies.clistery.appinject)
    implementation(project(mapOf("path" to ":lib_actmanager")))
    // leakcanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
    releaseImplementation("com.squareup.leakcanary:leakcanary-android-release:2.7")
    // Optional
    releaseImplementation("com.squareup.leakcanary:leakcanary-object-watcher-android:2.7")
}
