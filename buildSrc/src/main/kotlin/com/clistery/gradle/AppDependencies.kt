package com.clistery.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler

object AppDependencies {

    object clistery{
        const val appbasic = "io.github.clistery:appbasic:${AppVersion.clistery.appbasic}"
        const val appinject = "io.github.clistery:appinject:${AppVersion.clistery.appinject}"
    }

    object kotlin {
        
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${AppVersion.kotlin.version}"
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${AppVersion.kotlin.version}"
    }

    object androidx {
        
        const val coreKtx = "androidx.core:core-ktx:${AppVersion.androidx.coreKtx}"
        const val appcompat = "androidx.appcompat:appcompat:${AppVersion.androidx.appcompat}"
    }
    
    object google {
        
        const val material = "com.google.android.material:material:${AppVersion.google.material}"
    }
    
    val baseLibs: ArrayList<String>
        get() = arrayListOf(
            kotlin.stdlib,
            androidx.coreKtx,
            androidx.appcompat
        )
    
}
