# VeriSafeAgent_Library

A developer library for building verifiable and safe Android applications with built-in state verification capabilities.

## VeriSafeAgent System

[VeriSafeAgent System on GitHub](https://github.com/VeriSafeAgent/VeriSafeAgent)

## Version Information

- Library Version: 1.0.0
- Minimum SDK Version: 24 (Android 7.0)
- Target SDK Version: 34 (Android 14)
- Gradle Version: 8.2+
- Kotlin Version: 1.9.0+

## Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17 or newer
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools 34.0.0

## Overview

VeriSafeAgent Developer Library (VSADL) is a framework that enables Android app developers to implement verifiable state tracking and safety mechanisms in their applications. It provides a domain-specific language for defining predicates and rules that describe the expected behavior and state of application objects.

## Installation

Add the following to your app's `build.gradle`:

```gradle
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
        // ...
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'com.verisafeagent:developer-library:1.0.0'
    // Required dependencies
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    // ...
}
```

## Project Structure

```
VeriSafeAgent_Developer_library/
├── VSADeveloperLibrary/          # Main library module
│   ├── src/
│   │   ├── main/                # Library source code
│   │   ├── androidTest/         # Android-specific tests
│   │   └── test/               # Unit tests
│   └── build.gradle            # Library build configuration
├── test_vsa/                    # Test applications
├── release/                     # Release artifacts
└── Developer_GuideLines.md      # Detailed development guidelines
```

## Development Guidelines

### Defining Predicates

1. Define predicates for important objects first
2. Do not divide a single object into multiple predicates
3. Describe the context of the object as detailed as possible
4. Avoid duplicate predicates for the same object
5. Set unique values as keys (e.g., id, username, option name)

### Updating Predicates

1. Update state at decisive moments when app state is finalized
2. If no decisive moment exists, update at state change events
3. Focus on tracking critical application states

For detailed guidelines, see [Developer_GuideLines.md](Developer_GuideLines.md).

