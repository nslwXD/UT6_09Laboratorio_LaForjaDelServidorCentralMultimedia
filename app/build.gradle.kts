plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cpo.ut6_09_laboratorio_laforjadelservidorcentralmultimedia"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.cpo.ut6_09_laboratorio_laforjadelservidorcentralmultimedia"
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
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    // media3: exoplayer (motor de video) y ui (playerview para mostrarlo)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    // common: clases compartidas (mediaitem, player, listener...)
    implementation(libs.androidx.media3.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}