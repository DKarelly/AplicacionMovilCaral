plugins {
    alias(libs.plugins.android.application)
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.example.py_damb2026ii"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.py_damb2026ii"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    implementation(libs.volley)
    implementation(libs.gson)
    // implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation(libs.retrofit.v2110)
    //implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation(libs.converter.gson)
    implementation(libs.play.services.maps)
    implementation("com.google.android.gms:play-services-location:21.4.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}