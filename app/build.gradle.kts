import com.clistery.gradle.AppDependencies

plugins {
    id("app")
}

dependencies {
    AppDependencies.baseLibs.forEach { implementation(it) }
    implementation(AppDependencies.androidx.appcompat)
    implementation(AppDependencies.google.material)
    implementation(AppDependencies.clistery.appbasic)
    implementation(AppDependencies.clistery.appinject)
    implementation(project(mapOf("path" to ":lib_actmanager")))
    // leakcanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
    releaseImplementation("com.squareup.leakcanary:leakcanary-android-release:2.7")
    // Optional
    releaseImplementation("com.squareup.leakcanary:leakcanary-object-watcher-android:2.7")
}
