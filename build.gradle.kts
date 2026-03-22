plugins {
    id("com.android.library") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("maven-publish")
    id("signing")
}

android {
    namespace = "ai.quantumencoding.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "ai.quantumencoding"
            artifactId = "quantum-sdk"
            version = "0.2.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Quantum SDK")
                description.set("Quantum Encoding AI SDK for Android/Kotlin — 100+ AI endpoints across 10 providers")
                url.set("https://quantumencoding.ai/developers")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("quantumencoding")
                        name.set("Quantum Encoding Ltd")
                        email.set("info@quantumencoding.io")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/quantum-encoding/quantum-sdk-android.git")
                    developerConnection.set("scm:git:ssh://github.com:quantum-encoding/quantum-sdk-android.git")
                    url.set("https://github.com/quantum-encoding/quantum-sdk-android")
                }
            }
        }
    }

    repositories {
        maven {
            name = "centralPortal"
            url = uri("https://central.sonatype.com/api/v1/publisher/deployments/download/")
            credentials {
                username = System.getenv("MAVEN_CENTRAL_USERNAME") ?: ""
                password = System.getenv("MAVEN_CENTRAL_PASSWORD") ?: ""
            }
        }
        // Local staging directory for manual bundle upload
        maven {
            name = "staging"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}
