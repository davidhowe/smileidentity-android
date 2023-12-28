@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.moshix)
    alias(libs.plugins.parcelize)
}

val groupId = "com.smileidentity"
val artifactId = "android-sdk"
project.version = findProperty("VERSION_NAME") as? String ?: file("VERSION").readText().trim()

android {
    namespace = groupId
    resourcePrefix = "si_"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        all {
            val sentryDsn = findProperty("SENTRY_DSN")
                ?: throw IllegalArgumentException("Please set the SENTRY_DSN gradle property")
            buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
            buildConfigField("String", "VERSION_NAME", "\"${version}\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE*}"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        moduleName = "${groupId}_$artifactId"
        compileOptions {
            // https://kotlinlang.org/docs/opt-in-requirements.html#module-wide-opt-in
            // This is to provide us a blanket-allow us to use APIs annotated with @SmileIDOptIn
            // without having to add the opt-in annotation to every usage. The annotation's purpose
            // is primarily for consumers of the SDK to use, not for us.
            freeCompilerArgs += "-opt-in=com.smileidentity.SmileIDOptIn"
            if (project.hasProperty("enableComposeCompilerReports")) {
                val outputDir = layout.buildDirectory.dir("compose-reports").get().asFile.path
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$outputDir",
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$outputDir",
                )
            }
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    lint {
        enable += "ComposeM2Api"
        error += "ComposeM2Api"
    }
}

dependencies {
    // OkHttp is exposed in public SmileID interface (initialize), hence "api" vs "implementation"
    api(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)

    // Moshi is exposed in public SmileID interface, hence "api" vs "implementation"
    api(libs.moshi)
    implementation(libs.moshi.adapters)
    implementation(libs.moshi.adapters.lazy)

    // Immutable collections are exposed in public SmileID interface
    api(libs.kotlin.immutable.collections)
    implementation(libs.coroutines.core)

    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity.compose)

    implementation(libs.coil)

    // Logging
    implementation(libs.timber)

    // Sentry (crash reporting, tracing, breadcrumbs)
    implementation(platform(libs.sentry.bom))
    implementation(libs.sentry)

    // ViewModel and utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Jetpack Compose version is defined by BOM ("Bill-of-Materials")
    // Latest BOM version: https://developer.android.com/jetpack/compose/setup#bom-version-mapping
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    lintChecks(libs.compose.lint.checks)

    // Material Design components (ColorScheme and Typography exposed, hence api vs implementation)
    api(libs.androidx.compose.material3)
    // Jetpack Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    // Android Studio Preview support
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Android Studio Preview support
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Test rules
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // UI Tests (Needed for createAndroidComposeRule, but not createComposeRule)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Permissions Compose component
    implementation(libs.accompanist.permissions)
    // CameraX Compose component
    implementation(libs.camposer)

    // Unbundled model -- will be dynamically downloaded via Google Play Services
    implementation(libs.play.services.mlkit.face.detection)

    // Bundled model
    implementation(libs.mlkit.obj.detection)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.fragment)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.uiautomator)
}
