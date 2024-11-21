plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.beachplease"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.beachplease"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes.add("META-INF/INDEX.LIST")
        }
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.places)
    implementation(libs.play.services.fitness)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.play.services.maps.v1802)
    implementation(libs.play.services.location.v2101)
    implementation(libs.bcrypt)
    implementation(libs.circleimageview)
    implementation(libs.google.flexbox)
    implementation(libs.lottie)
    implementation(libs.mpandroidchart)
    implementation(libs.mockito.core)
    implementation(libs.mockito.inline)
    implementation(libs.core)
    implementation(libs.core.testing)

    // Add Picasso for image loading
    implementation("com.squareup.picasso:picasso:2.8")

    // Add Google Cloud Storage for image upload
    implementation("com.google.cloud:google-cloud-storage:2.3.0")
}
