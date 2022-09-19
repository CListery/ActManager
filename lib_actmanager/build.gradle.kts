import com.clistery.gradle.AppDependencies

plugins {
    id("kre-publish")
}

android {
    buildTypes.configureEach {
        isMinifyEnabled = false
    }
}

dependencies {
    AppDependencies.baseLibs.forEach { implementation(it) }
    implementation(AppDependencies.clistery.appbasic)
    implementation(AppDependencies.clistery.appinject)
}
