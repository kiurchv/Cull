import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

android {
    namespace = "xyz.kiurchv.cull"
    compileSdk = 35

    defaultConfig {
        applicationId = "xyz.kiurchv.cull"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
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
        compose = true
    }

    lint {
        abortOnError = false
        htmlReport = true
        htmlOutput = file("build/reports/lint/lint-report.html")
        xmlReport = true
        xmlOutput = file("build/reports/lint/lint-report.xml")
        checkDependencies = true
    }
}

detekt {
    config.setFrom(files("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
    source.setFrom(
        fileTree("src/main/java") { include("**/*.kt") },
        fileTree("src/test/java") { include("**/*.kt") },
    )
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // WorkManager
    implementation(libs.work.runtime)

    // Coil
    implementation(libs.coil.compose)

    // ExifInterface
    implementation(libs.exifinterface)

    // Coroutines
    implementation(libs.coroutines.android)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Detekt formatting rules
    detektPlugins(libs.detekt.formatting)
}
