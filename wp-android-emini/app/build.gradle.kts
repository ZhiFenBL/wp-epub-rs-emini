val buildUniversalApk = providers.gradleProperty("buildUniversalApk").forUseAtConfigurationTime().orNull == "true"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "us.wpdl.wprust"
    compileSdk = 36

    defaultConfig {
        applicationId = "us.wpdl.wprust"
        minSdk = 29
        targetSdk = 36
        
        // Read version code from a gradle property, defaulting to 1 for local builds.
        versionCode = providers.gradleProperty("ciVersionCode").orNull?.toInt() ?: 1
        
        // Read version name from a gradle property, defaulting to "1.0-local" for local builds.
        versionName = providers.gradleProperty("ciVersionName").orNull ?: "1.0-local"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    splits {
        // This block configures splitting based on Application Binary Interface (ABI)
        abi {
            // Enables the generation of separate APKs for each ABI.
            isEnable = !buildUniversalApk

            // Clears the list of ABIs, so we only build for the ones we explicitly include.
            reset()

            // Specify which architectures you want to create an APK for.
            // This should match the folders you have in your `jniLibs` directory.
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
    }
}

dependencies {
    implementation(libs.lottie)

    // https://mvnrepository.com/artifact/net.java.dev.jna/jna
    implementation("net.java.dev.jna:jna:5.17.0@aar")
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
