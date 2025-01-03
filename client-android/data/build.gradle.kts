plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.vl.messenger.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))

    /* Retrofit */
    implementation(libs.retrofit.client)
    implementation(libs.retrofit.gson)

    /* Jetpack */
    implementation(libs.datastore.preferences)
    implementation(libs.paging.runtime)

    /* STOMP */
    implementation(libs.stomp)
    implementation(libs.okhttp.client)
    implementation(libs.rxjava)
}