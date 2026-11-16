
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.google.gms.google.services)
}

// Las claves de Supabase se leen de local.properties (no se suben a git)
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

val supabaseUrl: String =
    localProperties.getProperty("SUPABASE_URL") ?: ""

val supabaseAnonKey: String =
    localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

android {
    namespace = "com.example.py_damb2026ii"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.molinocaral.incidentcaral"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"$supabaseUrl\""
        )

        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"$supabaseAnonKey\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    // AndroidX / UI
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Firebase (versiones gestionadas por el BoM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)

    // Red
    implementation(libs.volley)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Mapas, ubicación, QR e imágenes
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Base de datos local (Room, procesador para código Java)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Supabase (Kotlin)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.android)

    // Kotlin Coroutines
    implementation(libs.coroutines.android)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
