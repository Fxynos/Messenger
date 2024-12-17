plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.vl.messenger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vl.messenger"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "ADDRESS", "\"10.0.2.2:8080\"")
        }
        release {
            initWith(buildTypes.getByName("debug"))
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    implementation(project(":data"))
    implementation(project(":domain"))

    /* Android */
    implementation(libs.androidx.core)
    implementation(libs.theme.appcompat)
    implementation(libs.theme.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.lifecycle.viewmodel)

    /* Jetpack */
    implementation(libs.datastore.preferences)
    implementation(libs.paging.runtime)

    /* Hilt */
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    /* Coil */
    implementation(libs.coil)

    /* Adapter Delegates */
    implementation(libs.adapter.delegates.dsl)
    implementation(libs.adapter.delegates.pagination)
    implementation(libs.adapter.delegates.viewbinding)

    /* Retrofit */
    implementation(libs.retrofit.client)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    /* STOMP */
    implementation(libs.stomp)
    implementation(libs.okhttp.client)
    implementation(libs.rxjava)
}

kapt {
    correctErrorTypes = true
}