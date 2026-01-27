# Weighday Java App

A secure, local-first Android application for tracking body weight progress and goals.

## Project Overview

Weighday allows users to log their weight, set target goals, and visualize their progress through a segmented progress circle. The app prioritizes privacy by storing all data locally and securing access with an encrypted PIN lock.

## Core Features

- **Local Authentication:** Secure "App Lock" functionality using a 4-digit PIN.
- **Weight Tracking:** Log daily weight entries with optional notes.
- **Goal Management:** Set start and target weights to track progress.
- **Visual Analytics:** 
    - **SegmentedProgressView:** A custom UI component showing progress through colored stages (Red, Orange, Yellow, Green).
    - **Weight History:** Chronological list of all recorded weights.
- **Secure Storage:** Sensitive credentials (PIN) are stored using hardware-backed encryption.

## Technologies & Libraries

### Core Android
- **Language:** Java (OpenJDK 11)
- **UI Framework:** Android XML with ViewBinding
- **Navigation:** Android Jetpack Navigation Component
- **Architecture:** MVVM (Model-View-ViewModel) with LiveData and ViewModel

### Data Persistence
- **Room Persistence Library:** Provides an abstraction layer over SQLite for robust local data storage.
- **EncryptedSharedPreferences:** Part of the Android Jetpack Security library, used for securely storing the user's PIN using AES-256 encryption.

### UI/UX Components
- **Material Components:** Used for standard UI elements like Buttons, TextInputLayouts, and Dialogs.
- **ConstraintLayout:** Primary layout engine for responsive and complex UI designs.

## Development Setup

- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 36
- **Build System:** Gradle (Kotlin DSL)
- **JDK:** Version 21 (forced via `gradle.properties` for CLI stability)

## Linear Project
- **Linear project:** Weighday - Java App Tutorial