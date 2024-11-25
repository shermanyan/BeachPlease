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
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/license.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/notice.txt")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/*.groovy_module")
            excludes.add("META-INF/annotations.xml")
            excludes.add("META-INF/INDEX.LIST")

        }
    }

    testOptions {
        // This line removes the warning about experimental features
        animationsDisabled = true
    }


}

dependencies {


    // Core app dependencies
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
    implementation(libs.room.compiler)
    implementation(libs.play.services.maps.v1802)
    implementation(libs.play.services.location.v2101)
    implementation(libs.bcrypt)
    implementation(libs.circleimageview)
    implementation(libs.google.flexbox)
    implementation(libs.lottie)
    implementation(libs.mpandroidchart)
    implementation(libs.core)
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.google.cloud:google-cloud-storage:2.3.0")

// Unit test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.core.testing)

// Instrumented test dependencies
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.core.testing)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.uiautomator)
//    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.byte.buddy) // Correct Byte Buddy version

    androidTestImplementation(libs.mockito.android.v5142) // Correct version


    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0")
            // Exclude the older annotations
            exclude(group = "com.intellij", module = "annotations")
        }
    }


}

