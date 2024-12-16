plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.okhttp3:okhttp-urlconnection:4.9.0")// Thêm dòng này
    implementation ("androidx.compose.foundation:foundation:1.4.0")

    implementation("androidx.navigation:navigation-compose:2.5.3")

    // Thêm các phụ thuộc Compose UI để sử dụng Text, Button, TextField, v.v.
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.material:material:1.4.0")
    implementation("androidx.compose.material3:material3:1.0.0")

    // Các thư viện hỗ trợ preview và debugging Compose UI
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")

    implementation("androidx.compose.runtime:runtime:1.4.0")
    implementation("androidx.activity:activity-compose:1.7.0")  // Đảm bảo đã thêm thư viện này
    implementation("androidx.compose.runtime:runtime-livedata:1.3.0")

    implementation("com.google.code.gson:gson:2.8.8")





    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}