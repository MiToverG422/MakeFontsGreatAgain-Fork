plugins {
    id("com.android.application")
}

android {
    namespace = "com.mfga.xposed"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.mfga.xposed"
        targetSdk = 37
        versionCode = 15
        versionName = "1.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].resources.directories.add("src/main/resources")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
    compileOnly("io.github.libxposed:api:102.0.0")
}
