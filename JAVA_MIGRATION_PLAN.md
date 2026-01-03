# WeighDay Java Migration Plan

This document outlines the plan to port the WeighDay application from Kotlin/Jetpack Compose to Java/Android View System.

## Phase 1: Project Configuration & Dependencies

**Goal:** Ensure the `weighday-java` project has all necessary libraries to replicate the original functionality.

1.  **Update `libs.versions.toml` (or `build.gradle.kts` directly):**
    *   Add **Room Database** (Java) dependencies.
        *   `androidx.room:room-runtime`
        *   `androidx.room:room-compiler` (annotation processor)
    *   Add **Lifecycle/ViewModel** dependencies.
        *   `androidx.lifecycle:lifecycle-viewmodel`
        *   `androidx.lifecycle:lifecycle-livedata`
    *   Add **Java 8 Desugaring** (optional, but good for `java.time` support if strictly needed on older APIs, though `minSdk` 24 supports most).

2.  **Verify `build.gradle.kts`:**
    *   Ensure `viewBinding` is enabled (Done).
    *   Add `annotationProcessor` configuration for Room.

## Phase 2: Data Layer (The Foundation)

**Goal:** Replicate the database schema and repository pattern.

1.  **Packages:** Create the following package structure under `net.cynreub.weighday`:
    *   `data.local.entity`
    *   `data.local.dao`
    *   `data.repository`
    *   `domain.model`
    *   `util` (for converters)

2.  **Domain Models (POJOs):**
    *   Create `WeightEntry.java` (fields: `id`, `weight`, `date`, `note`).
    *   Create `WeightGoal.java` (fields: `id`, `goalWeight`, `startDate`, `startWeight`, `achievedDate`).

3.  **Room Entities:**
    *   Create `WeightEntryEntity.java` (Annotated with `@Entity`, maps to `WeightEntry`).
    *   Create `WeightGoalEntity.java` (Annotated with `@Entity`, maps to `WeightGoal`).

4.  **DAOs (Interfaces):**
    *   Create `WeightEntryDao.java`:
        *   `@Insert`, `@Query("SELECT * FROM weight_entries ORDER BY date DESC")`, etc.
    *   Create `WeightGoalDao.java`:
        *   `@Insert`, `@Update`, queries for active goals.

5.  **Database:**
    *   Create `AppDatabase.java` extending `RoomDatabase`.
    *   Create `DateConverter.java` for `LocalDate` <-> `Long`/`String`.

6.  **Repository:**
    *   Create `WeightRepository.java` class (Singleton).
    *   Implement methods to access DAOs using a background executor (e.g., `Executors.newSingleThreadExecutor()`).
    *   Return `LiveData` for observation.

## Phase 3: Custom UI Components

**Goal:** Replicate the custom Jetpack Compose visuals using standard Views.

1.  **`SegmentedProgressView.java`:**
    *   Extend `android.view.View`.
    *   Override `onDraw(Canvas canvas)`.
    *   **Logic:**
        *   Define `Paint` for segments (Red, Orange, Yellow, Green) and background shadow.
        *   Calculate angles: 8 segments, ~45 degrees each minus gap.
        *   Draw arcs based on a `progress` float (0.0 to 1.0).
    *   Expose `setProgress(float progress)` method that calls `invalidate()`.

## Phase 4: ViewModels (Business Logic)

**Goal:** Manage state and business rules.

1.  **`WeightViewModel.java`:**
    *   Extend `AndroidViewModel`.
    *   Hold `LiveData<WeightEntry>` (most recent).
    *   Hold `LiveData<WeightGoal>` (current active).
    *   Implement `syncGoals()` logic (check if current weight <= goal weight).
    *   Expose methods like `setGoal()`.

2.  **`AddWeightViewModel.java`:**
    *   Methods to `saveWeight(float weight, String note)`.

3.  **`WeightHistoryViewModel.java`:**
    *   Expose `LiveData<List<WeightEntry>>` for the history list.

## Phase 5: Screens (Fragments & XML Layouts)

**Goal:** Build the UI using XML layouts and bind them to ViewModels.

1.  **`WeightTrackerFragment` (Home):**
    *   **XML (`fragment_weight_tracker.xml`):**
        *   `ConstraintLayout`.
        *   Include `SegmentedProgressView` (center).
        *   `TextView` for "GOAL" / "SET GOAL" (layered on top of circle or inside custom view).
        *   `TextView` for "Last recorded weight".
        *   `FloatingActionButton` or `Button` for "Add Weight".
        *   Button for "History" (in App Bar or on screen).
    *   **Java:**
        *   Observe `WeightViewModel`.
        *   Update `SegmentedProgressView` progress.
        *   Handle "Set Goal" click -> Show `SetGoalDialogFragment`.

2.  **`AddWeightFragment`:**
    *   **XML (`fragment_add_weight.xml`):**
        *   `NumberPicker` (Standard Android Widget) for whole numbers.
        *   `NumberPicker` (optional) for decimals, or just one for whole lbs.
        *   `EditText` for optional note.
        *   `Button` for "Save".
    *   **Java:**
        *   Setup `NumberPicker` (min/max/values).
        *   On Save -> `viewModel.saveWeight()`, then `findNavController().popBackStack()`.

3.  **`WeightHistoryFragment`:**
    *   **XML (`fragment_weight_history.xml`):**
        *   `RecyclerView`.
    *   **Adapter (`WeightHistoryAdapter.java`):**
        *   ViewType for `WeightEntry`.
        *   `item_weight_entry.xml`: Date and Weight text.

4.  **Dialogs:**
    *   `SetGoalDialogFragment.java`: Custom Dialog or AlertDialog with custom view for inputting Start/Goal weight.

## Phase 6: Navigation & Main Activity

1.  **Navigation Graph (`nav_graph.xml`):**
    *   Destinations: `trackerFragment`, `addWeightFragment`, `historyFragment`.
    *   Actions: Tracker -> Add, Tracker -> History.

2.  **`MainActivity.java`:**
    *   Setup `NavController` with `FragmentContainerView`.
    *   Setup `Toolbar` / `ActionBar` (if using one).

## Phase 7: Verification

1.  **Run Application:**
    *   Verify Database creation.
    *   Verify Goal Setting persistence.
    *   Verify Progress Circle drawing (angles, colors).
    *   Verify Add Weight updates the tracker immediately.
    *   Verify History list shows data.
