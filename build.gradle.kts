
// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP 9 ya incluye soporte de Kotlin: NO se aplica 'org.jetbrains.kotlin.android'.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
