# Weighday Full Test Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cover every class in the Weighday app (data, domain, ViewModels, UI, MainActivity) with automated tests, and produce a JaCoCo coverage report to prove it.

**Architecture:** Nearly everything runs as fast JVM tests using Robolectric plus an in-memory Room database (`app/src/test`). A small set of production seams (test hook on `AppDatabase`, injectable executor on `WeightRepository`, a getter on `SegmentedProgressView`) makes that possible. A short Espresso end-to-end suite (`app/src/androidTest`) covers the full user flow on a device/emulator. The plan is TDD where tests can fail first, and it fixes two real bugs the tests expose.

**Tech Stack:** JUnit 4.13, Robolectric 4.16, AndroidX Test (`ext:junit`, `core`, `fragment-testing`, `navigation-testing`, `arch core-testing`), Espresso 3.7, Room 2.6.1 in-memory, AGP built-in JaCoCo coverage.

---

## Code inventory and how each piece is covered

| Class (`xyz.drreub.weighday.…`) | Test type | Task |
|---|---|---|
| `util.DateConverter` | JVM unit | 1 |
| `domain.model.WeightEntry`, `WeightGoal` | JVM unit | 2 |
| `data.local.dao.*`, `entity.*`, `AppDatabase` | Robolectric + in-memory Room | 4 |
| `data.repository.WeightRepository` | Robolectric + in-memory Room | 5 |
| `ui.viewmodel.*` (3) | Robolectric + in-memory Room | 6 |
| `ui.adapters.WeightHistoryAdapter` | Robolectric | 7 |
| `ui.components.SegmentedProgressView` | Robolectric | 8 |
| `ui.fragments.*` (4) | Robolectric `FragmentScenario` | 9 |
| `MainActivity` | Robolectric `ActivityScenario` | 10 |
| Whole flow | Espresso on device | 11 |

## Known issues the tests will surface (fixed inside the plan)

1. **`WeightHistoryAdapter.onBindViewHolder`** uses `Resources.getSystem().getString(R.string.goal_weight_text, …)`. The system resources don't contain app strings, so this throws `Resources.NotFoundException` and the history screen crashes as soon as there is one entry. Fixed in Task 7.
2. **`AddWeightFragment`** splits the last weight with `Math.round((w - whole) * 10)`. For `72.96` that yields decimal `10`, which the picker (max 9) clamps to `9`, showing `72.9` instead of `73.0`. Fixed in Task 9.

Characterization only (tests document current behavior, no fix planned, flag to the owner):
- `WeightViewModel.checkGoalAchieved` treats `entry.weight <= goal.goalWeight` as achieved, so a *weight-gain* goal (start < goal) is marked achieved immediately.
- `SettingsFragment` is not in `nav_graph.xml` and `MainActivity`'s `action_settings` handler does nothing (it is repurposed as "History" by `WeightTrackerFragment`).

## File Structure

Production (small seams only):
- Modify `app/src/main/java/xyz/drreub/weighday/data/local/AppDatabase.java`: add `setInstanceForTesting`.
- Modify `app/src/main/java/xyz/drreub/weighday/data/repository/WeightRepository.java`: injectable DAOs and executor.
- Modify `app/src/main/java/xyz/drreub/weighday/ui/components/SegmentedProgressView.java`: add `getProgress()`.
- Modify `app/src/main/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapter.java` and `ui/fragments/AddWeightFragment.java`: bug fixes.

Test support (`app/src/test/java/xyz/drreub/weighday/testutil/`):
- `LiveDataTestUtil.java`: synchronous `getValue(LiveData)`.
- `BaseDbTest.java`: installs an in-memory `AppDatabase` as the singleton, `InstantTaskExecutorRule`, cleanup.
- `WriteExecutorUtil.java`: waits for the 4-thread write pool to drain.

Tests (all under `app/src/test/java/xyz/drreub/weighday/` unless noted):
`util/DateConverterTest`, `domain/model/WeightEntryTest`, `domain/model/WeightGoalTest`, `data/local/WeightEntryDaoTest`, `data/local/WeightGoalDaoTest`, `data/local/AppDatabaseTest`, `data/repository/WeightRepositoryTest`, `ui/viewmodel/{WeightViewModelTest,AddWeightViewModelTest,WeightHistoryViewModelTest}`, `ui/adapters/WeightHistoryAdapterTest`, `ui/components/SegmentedProgressViewTest`, `ui/fragments/{WeightTrackerFragmentTest,AddWeightFragmentTest,WeightHistoryFragmentTest,SettingsFragmentTest}`, `MainActivityTest`; plus `app/src/androidTest/java/xyz/drreub/weighday/WeightFlowTest.java`.

---

### Task 0: Test infrastructure

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/resources/robolectric.properties`
- Delete: `app/src/test/java/xyz/drreub/weighday/ExampleUnitTest.java`

- [ ] **Step 1: Commit the finished package rename so test work starts from a clean tree**

```bash
git add -A app/src docs app/build.gradle.kts
git commit -m "refactor: rename root package net.cynreub -> xyz.drreub"
```

- [ ] **Step 2: Add coverage + test dependencies to `app/build.gradle.kts`**

Inside `android { … }`, after `buildFeatures { viewBinding = true }`, add:

```kotlin
    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
```

(There is already a `buildTypes { release { … } }` block; put `debug { … }` inside that existing block instead of creating a second one.)

Replace the three existing test dependency lines at the bottom of `dependencies { … }` with:

```kotlin
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    testImplementation(libs.espresso.core)
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.navigation:navigation-testing:2.9.6")
    debugImplementation("androidx.fragment:fragment-testing:1.8.5")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test:rules:1.6.1")
```

- [ ] **Step 3: Pin the Robolectric SDK**

Create `app/src/test/resources/robolectric.properties`:

```properties
sdk=34
```

- [ ] **Step 4: Remove the template unit test**

```bash
git rm app/src/test/java/xyz/drreub/weighday/ExampleUnitTest.java
```

- [ ] **Step 5: Verify Gradle resolves and the (now empty) unit test task passes**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL` (NO-SOURCE is fine). If `org.robolectric:robolectric:4.16` does not resolve, use the latest 4.x release that resolves.

- [ ] **Step 6: Commit**

```bash
git add app/build.gradle.kts app/src/test
git commit -m "test: add Robolectric, AndroidX test deps and coverage config"
```

---

### Task 1: DateConverter

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/util/DateConverterTest.java`

- [ ] **Step 1: Write the tests** (pure JVM, no Android needed)

```java
package xyz.drreub.weighday.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateConverterTest {

    @Test
    public void fromLocalDateTime_formatsIso() {
        assertEquals("2026-03-05T07:08:09",
                DateConverter.fromLocalDateTime(LocalDateTime.of(2026, 3, 5, 7, 8, 9)));
    }

    @Test
    public void toLocalDateTime_parsesIso() {
        assertEquals(LocalDateTime.of(2026, 3, 5, 7, 8, 9),
                DateConverter.toLocalDateTime("2026-03-05T07:08:09"));
    }

    @Test
    public void localDateTime_roundTripKeepsNanos() {
        LocalDateTime original = LocalDateTime.of(2026, 3, 5, 7, 8, 9, 123_000_000);
        assertEquals(original,
                DateConverter.toLocalDateTime(DateConverter.fromLocalDateTime(original)));
    }

    @Test
    public void localDateTime_nullsPassThrough() {
        assertNull(DateConverter.fromLocalDateTime(null));
        assertNull(DateConverter.toLocalDateTime(null));
    }

    @Test
    public void fromLocalDate_formatsIso() {
        assertEquals("2026-03-05", DateConverter.fromLocalDate(LocalDate.of(2026, 3, 5)));
    }

    @Test
    public void toLocalDate_parsesIso() {
        assertEquals(LocalDate.of(2026, 3, 5), DateConverter.toLocalDate("2026-03-05"));
    }

    @Test
    public void localDate_nullsPassThrough() {
        assertNull(DateConverter.fromLocalDate(null));
        assertNull(DateConverter.toLocalDate(null));
    }
}
```

- [ ] **Step 2: Run**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.util.DateConverterTest"`
Expected: 7 tests PASS (code already exists; these lock its behavior).

- [ ] **Step 3: Commit**

```bash
git add app/src/test && git commit -m "test: cover DateConverter"
```

---

### Task 2: Domain models

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/domain/model/WeightEntryTest.java`
- Test: `app/src/test/java/xyz/drreub/weighday/domain/model/WeightGoalTest.java`

- [ ] **Step 1: Write `WeightEntryTest`**

```java
package xyz.drreub.weighday.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDateTime;

public class WeightEntryTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);

    @Test
    public void constructor_setsAllFields() {
        WeightEntry e = new WeightEntry(1, 180.5, "note", now, "u1", 7);
        assertEquals(1, e.getId());
        assertEquals(180.5, e.getWeight(), 0.0);
        assertEquals("note", e.getNote());
        assertEquals(now, e.getDate());
        assertEquals("u1", e.getUserId());
        assertEquals(Integer.valueOf(7), e.getGoalId());
    }

    @Test
    public void setters_updateFields() {
        WeightEntry e = new WeightEntry(1, 1, "a", now, "u1", 7);
        LocalDateTime later = now.plusDays(1);
        e.setId(2);
        e.setWeight(2.5);
        e.setNote("b");
        e.setDate(later);
        e.setUserId("u2");
        e.setGoalId(null);
        assertEquals(2, e.getId());
        assertEquals(2.5, e.getWeight(), 0.0);
        assertEquals("b", e.getNote());
        assertEquals(later, e.getDate());
        assertEquals("u2", e.getUserId());
        assertNull(e.getGoalId());
    }
}
```

- [ ] **Step 2: Write `WeightGoalTest`**

```java
package xyz.drreub.weighday.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;

public class WeightGoalTest {

    private final LocalDate start = LocalDate.of(2026, 1, 1);

    @Test
    public void constructor_setsAllFields() {
        WeightGoal g = new WeightGoal(1, 180, 200, start, null, "u1");
        assertEquals(1, g.getId());
        assertEquals(180, g.getGoalWeight(), 0.0);
        assertEquals(200, g.getStartWeight(), 0.0);
        assertEquals(start, g.getStartDate());
        assertNull(g.getAchievedDate());
        assertEquals("u1", g.getUserId());
    }

    @Test
    public void setters_updateFields() {
        WeightGoal g = new WeightGoal(1, 180, 200, start, null, "u1");
        LocalDate achieved = start.plusDays(30);
        g.setId(2);
        g.setGoalWeight(170);
        g.setStartWeight(190);
        g.setStartDate(start.plusDays(1));
        g.setAchievedDate(achieved);
        g.setUserId("u2");
        assertEquals(2, g.getId());
        assertEquals(170, g.getGoalWeight(), 0.0);
        assertEquals(190, g.getStartWeight(), 0.0);
        assertEquals(start.plusDays(1), g.getStartDate());
        assertEquals(achieved, g.getAchievedDate());
        assertEquals("u2", g.getUserId());
    }
}
```

- [ ] **Step 3: Run and commit**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.domain.*"`
Expected: 4 tests PASS.

```bash
git add app/src/test && git commit -m "test: cover domain models"
```

---

### Task 3: Testability seams and shared test support

**Files:**
- Modify: `app/src/main/java/xyz/drreub/weighday/data/local/AppDatabase.java`
- Modify: `app/src/main/java/xyz/drreub/weighday/data/repository/WeightRepository.java`
- Create: `app/src/test/java/xyz/drreub/weighday/testutil/LiveDataTestUtil.java`
- Create: `app/src/test/java/xyz/drreub/weighday/testutil/WriteExecutorUtil.java`
- Create: `app/src/test/java/xyz/drreub/weighday/testutil/BaseDbTest.java`

- [ ] **Step 1: Add a test hook to `AppDatabase`**

Add the import `androidx.annotation.VisibleForTesting;` and, below `getDatabase`, this method:

```java
    @VisibleForTesting
    public static void setInstanceForTesting(AppDatabase database) {
        INSTANCE = database;
    }
```

- [ ] **Step 2: Make `WeightRepository` injectable (behavior unchanged)**

Add imports `androidx.annotation.VisibleForTesting;` and `java.util.concurrent.Executor;`. Replace the fields and constructor:

```java
    private final WeightEntryDao weightEntryDao;
    private final WeightGoalDao weightGoalDao;
    private final Executor writeExecutor;

    public WeightRepository(Application application) {
        this(AppDatabase.getDatabase(application));
    }

    private WeightRepository(AppDatabase db) {
        this(db.weightEntryDao(), db.weightGoalDao(), AppDatabase.databaseWriteExecutor);
    }

    @VisibleForTesting
    public WeightRepository(WeightEntryDao weightEntryDao, WeightGoalDao weightGoalDao, Executor writeExecutor) {
        this.weightEntryDao = weightEntryDao;
        this.weightGoalDao = weightGoalDao;
        this.writeExecutor = writeExecutor;
    }
```

Then replace every `AppDatabase.databaseWriteExecutor.execute(` in the class body with `writeExecutor.execute(` (six occurrences: two `insert`, `insertWeightGoal`, `update`, `saveWeightEntryWithGoal`).

- [ ] **Step 3: Create `LiveDataTestUtil`**

```java
package xyz.drreub.weighday.testutil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicReference;

/** Reads the current value of a LiveData synchronously (use with InstantTaskExecutorRule). */
public final class LiveDataTestUtil {
    private LiveDataTestUtil() {}

    public static <T> T getValue(LiveData<T> liveData) {
        AtomicReference<T> ref = new AtomicReference<>();
        Observer<T> observer = ref::set;
        liveData.observeForever(observer);
        liveData.removeObserver(observer);
        return ref.get();
    }
}
```

- [ ] **Step 4: Create `WriteExecutorUtil`**

```java
package xyz.drreub.weighday.testutil;

import static org.junit.Assert.assertTrue;

import xyz.drreub.weighday.data.local.AppDatabase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * Blocks until every task queued on AppDatabase.databaseWriteExecutor (4 threads) has finished.
 * All 4 barrier tasks can only run together once every thread is idle.
 */
public final class WriteExecutorUtil {
    private static final int THREADS = 4;

    private WriteExecutorUtil() {}

    public static void flush() throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(THREADS);
        CountDownLatch done = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }
        assertTrue("write executor did not drain", done.await(10, TimeUnit.SECONDS));
    }
}
```

- [ ] **Step 5: Create `BaseDbTest`**

```java
package xyz.drreub.weighday.testutil;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.data.local.AppDatabase;

/** Installs a fresh in-memory AppDatabase as the app singleton for each test. */
@RunWith(AndroidJUnit4.class)
public abstract class BaseDbTest {
    public static final String USER = "testUser";

    @Rule public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    protected AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        AppDatabase.setInstanceForTesting(db);
    }

    @After
    public void closeDb() {
        AppDatabase.setInstanceForTesting(null);
        db.close();
    }
}
```

- [ ] **Step 6: Verify everything still compiles and the app is unchanged**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all earlier tests PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src && git commit -m "test: add DB test seams and shared test utilities"
```

---

### Task 4: DAOs, entities, AppDatabase

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/data/local/WeightEntryDaoTest.java`
- Test: `app/src/test/java/xyz/drreub/weighday/data/local/WeightGoalDaoTest.java`
- Test: `app/src/test/java/xyz/drreub/weighday/data/local/AppDatabaseTest.java`

- [ ] **Step 1: Write `WeightEntryDaoTest`**

```java
package xyz.drreub.weighday.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import org.junit.Test;

import xyz.drreub.weighday.data.local.dao.WeightEntryDao;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;
import java.util.List;

public class WeightEntryDaoTest extends BaseDbTest {

    private final LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 8, 0);

    private WeightEntryEntity entry(double w, LocalDateTime d, String user) {
        return new WeightEntryEntity(w, "n" + w, d, user, null);
    }

    @Test
    public void getAllEntries_emptyByDefault() {
        assertEquals(0, getValue(db.weightEntryDao().getAllEntries(USER)).size());
    }

    @Test
    public void getAllEntries_ordersNewestFirst() {
        WeightEntryDao dao = db.weightEntryDao();
        dao.insert(entry(200, t0, USER));
        dao.insert(entry(198, t0.plusDays(2), USER));
        dao.insert(entry(199, t0.plusDays(1), USER));

        List<WeightEntryEntity> all = getValue(dao.getAllEntries(USER));
        assertEquals(3, all.size());
        assertEquals(198, all.get(0).weight, 0.0);
        assertEquals(199, all.get(1).weight, 0.0);
        assertEquals(200, all.get(2).weight, 0.0);
    }

    @Test
    public void getAllEntries_onlyReturnsRequestedUser() {
        WeightEntryDao dao = db.weightEntryDao();
        dao.insert(entry(200, t0, USER));
        dao.insert(entry(150, t0, "someoneElse"));

        List<WeightEntryEntity> all = getValue(dao.getAllEntries(USER));
        assertEquals(1, all.size());
        assertEquals(200, all.get(0).weight, 0.0);
    }

    @Test
    public void getMostRecentEntry_returnsLatestByDate() {
        WeightEntryDao dao = db.weightEntryDao();
        dao.insert(entry(200, t0, USER));
        dao.insert(entry(195, t0.plusDays(5), USER));
        dao.insert(entry(199, t0.plusDays(1), USER));

        assertEquals(195, getValue(dao.getMostRecentEntry(USER)).weight, 0.0);
    }

    @Test
    public void getMostRecentEntry_nullWhenNone() {
        assertNull(getValue(db.weightEntryDao().getMostRecentEntry(USER)));
    }

    @Test
    public void insert_persistsAllFieldsIncludingConvertedDate() {
        WeightEntryEntity e = new WeightEntryEntity(180.5, "hello", t0, USER, 3);
        db.weightEntryDao().insert(e);

        WeightEntryEntity read = getValue(db.weightEntryDao().getMostRecentEntry(USER));
        assertEquals(180.5, read.weight, 0.0);
        assertEquals("hello", read.note);
        assertEquals(t0, read.date);
        assertEquals(USER, read.userId);
        assertEquals(Integer.valueOf(3), read.goalId);
        assertEquals(1, read.id); // auto-generated
    }

    @Test
    public void insert_sameIdReplacesRow() {
        WeightEntryDao dao = db.weightEntryDao();
        WeightEntryEntity first = entry(200, t0, USER);
        first.id = 42;
        dao.insert(first);
        WeightEntryEntity second = entry(190, t0, USER);
        second.id = 42;
        dao.insert(second);

        List<WeightEntryEntity> all = getValue(dao.getAllEntries(USER));
        assertEquals(1, all.size());
        assertEquals(190, all.get(0).weight, 0.0);
    }
}
```

- [ ] **Step 2: Write `WeightGoalDaoTest`**

```java
package xyz.drreub.weighday.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import org.junit.Test;

import xyz.drreub.weighday.data.local.dao.WeightGoalDao;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDate;
import java.util.List;

public class WeightGoalDaoTest extends BaseDbTest {

    private final LocalDate d0 = LocalDate.of(2026, 1, 1);

    private WeightGoalEntity goal(double goal, double start, String user) {
        return new WeightGoalEntity(goal, start, d0, null, user);
    }

    @Test
    public void insert_returnsGeneratedIdsInIncreasingOrder() {
        WeightGoalDao dao = db.weightGoalDao();
        long a = dao.insert(goal(180, 200, USER));
        long b = dao.insert(goal(170, 190, USER));
        assertTrue(a > 0);
        assertTrue(b > a);
    }

    @Test
    public void getAllGoals_filtersByUser() {
        WeightGoalDao dao = db.weightGoalDao();
        dao.insert(goal(180, 200, USER));
        dao.insert(goal(120, 150, "other"));

        List<WeightGoalEntity> all = getValue(dao.getAllGoals(USER));
        assertEquals(1, all.size());
        assertEquals(180, all.get(0).goalWeight, 0.0);
    }

    @Test
    public void getMostRecentGoal_returnsHighestId() {
        WeightGoalDao dao = db.weightGoalDao();
        dao.insert(goal(180, 200, USER));
        dao.insert(goal(170, 190, USER));

        assertEquals(170, getValue(dao.getMostRecentGoal(USER)).goalWeight, 0.0);
    }

    @Test
    public void getMostRecentGoal_nullWhenNone() {
        assertNull(getValue(db.weightGoalDao().getMostRecentGoal(USER)));
    }

    @Test
    public void getMostRecentGoalSync_returnsHighestIdOrNull() {
        WeightGoalDao dao = db.weightGoalDao();
        assertNull(dao.getMostRecentGoalSync(USER));
        dao.insert(goal(180, 200, USER));
        dao.insert(goal(170, 190, USER));
        assertEquals(170, dao.getMostRecentGoalSync(USER).goalWeight, 0.0);
    }

    @Test
    public void insert_persistsDatesAndNullAchievedDate() {
        db.weightGoalDao().insert(goal(180, 200, USER));
        WeightGoalEntity read = db.weightGoalDao().getMostRecentGoalSync(USER);
        assertEquals(d0, read.startDate);
        assertNull(read.achievedDate);
        assertEquals(200, read.startWeight, 0.0);
    }

    @Test
    public void update_persistsAchievedDate() {
        WeightGoalDao dao = db.weightGoalDao();
        dao.insert(goal(180, 200, USER));
        WeightGoalEntity stored = dao.getMostRecentGoalSync(USER);
        stored.achievedDate = d0.plusDays(10);
        dao.update(stored);

        assertEquals(d0.plusDays(10), dao.getMostRecentGoalSync(USER).achievedDate);
    }
}
```

- [ ] **Step 3: Write `AppDatabaseTest`**

```java
package xyz.drreub.weighday.data.local;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {

    @After
    public void resetSingleton() {
        AppDatabase.setInstanceForTesting(null);
    }

    @Test
    public void getDatabase_returnsSameSingletonAndExposesDaos() {
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase first = AppDatabase.getDatabase(context);
        AppDatabase second = AppDatabase.getDatabase(context);

        assertSame(first, second);
        assertNotNull(first.weightEntryDao());
        assertNotNull(first.weightGoalDao());
        first.close();
    }

    @Test
    public void writeExecutor_isAvailable() {
        assertNotNull(AppDatabase.databaseWriteExecutor);
    }
}
```

- [ ] **Step 4: Run**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.data.local.*"`
Expected: all PASS. If a `LiveData` assertion returns `null` unexpectedly, confirm the class extends `BaseDbTest` (it provides `InstantTaskExecutorRule`).

- [ ] **Step 5: Commit**

```bash
git add app/src/test && git commit -m "test: cover Room DAOs, entities and AppDatabase"
```

---

### Task 5: WeightRepository

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/data/repository/WeightRepositoryTest.java`

- [ ] **Step 1: Write the tests** (a direct executor makes writes synchronous)

```java
package xyz.drreub.weighday.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class WeightRepositoryTest extends BaseDbTest {

    private WeightRepository repo;

    @Before
    public void createRepo() {
        repo = new WeightRepository(db.weightEntryDao(), db.weightGoalDao(), Runnable::run);
    }

    private WeightGoalEntity goal(double goal, double start) {
        return new WeightGoalEntity(goal, start, LocalDate.of(2026, 1, 1), null, USER);
    }

    @Test
    public void insertEntry_thenGetAllAndMostRecent() {
        repo.insert(new WeightEntryEntity(200, "a", LocalDateTime.of(2026, 1, 1, 8, 0), USER, null));
        repo.insert(new WeightEntryEntity(199, "b", LocalDateTime.of(2026, 1, 2, 8, 0), USER, null));

        assertEquals(2, getValue(repo.getAllEntries(USER)).size());
        assertEquals(199, getValue(repo.getMostRecentEntry(USER)).weight, 0.0);
    }

    @Test
    public void insertGoal_thenGetAllAndMostRecent() {
        repo.insert(goal(180, 200));
        repo.insert(goal(170, 190));

        assertEquals(2, getValue(repo.getAllGoals(USER)).size());
        assertEquals(170, getValue(repo.getMostRecentGoal(USER)).goalWeight, 0.0);
    }

    @Test
    public void insertWeightGoal_invokesCallbackWithGeneratedId() {
        AtomicLong received = new AtomicLong(-1);
        repo.insertWeightGoal(goal(180, 200), received::set);

        assertTrue(received.get() > 0);
        assertEquals(received.get(), getValue(repo.getMostRecentGoal(USER)).id);
    }

    @Test
    public void update_persistsChange() {
        repo.insert(goal(180, 200));
        WeightGoalEntity stored = getValue(repo.getMostRecentGoal(USER));
        stored.achievedDate = LocalDate.of(2026, 2, 1);
        repo.update(stored);

        assertEquals(LocalDate.of(2026, 2, 1), getValue(repo.getMostRecentGoal(USER)).achievedDate);
    }

    @Test
    public void saveWeightEntryWithGoal_linksMostRecentGoal() {
        repo.insert(goal(180, 200));
        repo.insert(goal(170, 190));
        int latestGoalId = getValue(repo.getMostRecentGoal(USER)).id;

        repo.saveWeightEntryWithGoal(185.5, "note", USER);

        WeightEntryEntity saved = getValue(repo.getMostRecentEntry(USER));
        assertEquals(185.5, saved.weight, 0.0);
        assertEquals("note", saved.note);
        assertEquals(USER, saved.userId);
        assertEquals(Integer.valueOf(latestGoalId), saved.goalId);
        assertNotNull(saved.date);
    }

    @Test
    public void saveWeightEntryWithGoal_goalIdNullWhenNoGoal() {
        repo.saveWeightEntryWithGoal(185.5, "", USER);
        assertNull(getValue(repo.getMostRecentEntry(USER)).goalId);
    }
}
```

- [ ] **Step 2: Run and commit**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.data.repository.*"`
Expected: 6 tests PASS.

```bash
git add app/src/test && git commit -m "test: cover WeightRepository"
```

---

### Task 6: ViewModels

The ViewModels build their own `WeightRepository(Application)`, which uses the in-memory singleton installed by `BaseDbTest` and the real 4-thread write pool, hence `WriteExecutorUtil.flush()` after writes.

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/ui/viewmodel/WeightViewModelTest.java`
- Test: `app/src/test/java/xyz/drreub/weighday/ui/viewmodel/AddWeightViewModelTest.java`
- Test: `app/src/test/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModelTest.java`

- [ ] **Step 1: Write `WeightViewModelTest`**

```java
package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeightViewModelTest extends BaseDbTest {

    private WeightViewModel vm;

    @Before
    public void createViewModel() {
        vm = new WeightViewModel((Application) ApplicationProvider.getApplicationContext());
    }

    private WeightEntryEntity entryOf(double weight) {
        return new WeightEntryEntity(weight, "", LocalDateTime.now(), USER, null);
    }

    private WeightGoalEntity storedGoal(double goal, double start) {
        db.weightGoalDao().insert(new WeightGoalEntity(goal, start, LocalDate.of(2026, 1, 1), null, USER));
        return db.weightGoalDao().getMostRecentGoalSync(USER);
    }

    @Test
    public void setNewGoal_insertsGoalAndInitialEntry() throws Exception {
        vm.setNewGoal(180, 200);
        WriteExecutorUtil.flush();

        WeightGoalEntity goal = db.weightGoalDao().getMostRecentGoalSync(USER);
        assertNotNull(goal);
        assertEquals(180, goal.goalWeight, 0.0);
        assertEquals(200, goal.startWeight, 0.0);
        assertEquals(LocalDate.now(), goal.startDate);
        assertNull(goal.achievedDate);
        assertEquals(USER, goal.userId);

        WeightEntryEntity entry = getValue(db.weightEntryDao().getMostRecentEntry(USER));
        assertNotNull(entry);
        assertEquals(200, entry.weight, 0.0);
        assertEquals("Initial", entry.note);
        assertEquals(Integer.valueOf(goal.id), entry.goalId);
    }

    @Test
    public void getMostRecentEntryAndGoal_exposeRepositoryData() {
        assertNull(getValue(vm.getMostRecentEntry()));
        assertNull(getValue(vm.getMostRecentGoal()));

        db.weightEntryDao().insert(entryOf(190));
        storedGoal(180, 200);

        assertEquals(190, getValue(vm.getMostRecentEntry()).weight, 0.0);
        assertEquals(180, getValue(vm.getMostRecentGoal()).goalWeight, 0.0);
    }

    @Test
    public void checkGoalAchieved_marksGoalWhenWeightAtOrBelowTarget() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        vm.checkGoalAchieved(entryOf(180), goal);
        WriteExecutorUtil.flush();

        assertEquals(LocalDate.now(), db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void checkGoalAchieved_ignoresWeightAboveTarget() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        vm.checkGoalAchieved(entryOf(180.1), goal);
        WriteExecutorUtil.flush();

        assertNull(db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void checkGoalAchieved_doesNotOverwriteExistingAchievedDate() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        LocalDate earlier = LocalDate.of(2026, 1, 15);
        goal.achievedDate = earlier;
        db.weightGoalDao().update(goal);

        vm.checkGoalAchieved(entryOf(170), goal);
        WriteExecutorUtil.flush();

        assertEquals(earlier, db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void checkGoalAchieved_nullArgumentsAreNoOps() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        vm.checkGoalAchieved(null, goal);
        vm.checkGoalAchieved(entryOf(170), null);
        vm.checkGoalAchieved(null, null);
        WriteExecutorUtil.flush();

        assertNull(db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }
}
```

- [ ] **Step 2: Write `AddWeightViewModelTest`**

```java
package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;

import java.time.LocalDate;

public class AddWeightViewModelTest extends BaseDbTest {

    private AddWeightViewModel vm;

    @Before
    public void createViewModel() {
        vm = new AddWeightViewModel((Application) ApplicationProvider.getApplicationContext());
    }

    @Test
    public void saveWeight_storesEntryLinkedToCurrentGoal() throws Exception {
        db.weightGoalDao().insert(new WeightGoalEntity(180, 200, LocalDate.now(), null, USER));
        int goalId = db.weightGoalDao().getMostRecentGoalSync(USER).id;

        vm.saveWeight(195.5, "felt good");
        WriteExecutorUtil.flush();

        WeightEntryEntity saved = getValue(vm.getLastWeight());
        assertEquals(195.5, saved.weight, 0.0);
        assertEquals("felt good", saved.note);
        assertEquals(Integer.valueOf(goalId), saved.goalId);
    }

    @Test
    public void getLastWeight_nullWhenNoEntries() {
        assertNull(getValue(vm.getLastWeight()));
    }
}
```

- [ ] **Step 3: Write `WeightHistoryViewModelTest`**

```java
package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;
import java.util.List;

public class WeightHistoryViewModelTest extends BaseDbTest {

    @Test
    public void getAllEntries_returnsNewestFirst() {
        WeightHistoryViewModel vm =
                new WeightHistoryViewModel((Application) ApplicationProvider.getApplicationContext());
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));

        List<WeightEntryEntity> all = getValue(vm.getAllEntries());
        assertEquals(2, all.size());
        assertEquals(199, all.get(0).weight, 0.0);
    }
}
```

- [ ] **Step 4: Run and commit**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.viewmodel.*"`
Expected: 9 tests PASS.

```bash
git add app/src/test && git commit -m "test: cover ViewModels"
```

---

### Task 7: WeightHistoryAdapter (test first, then fix bug #1)

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapterTest.java`
- Modify: `app/src/main/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapter.java`

- [ ] **Step 1: Write the tests**

```java
package xyz.drreub.weighday.ui.adapters;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class WeightHistoryAdapterTest {

    private Context context;
    private FrameLayout parent;
    private WeightHistoryAdapter adapter;

    @Before
    public void setUp() {
        context = new androidx.appcompat.view.ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(), R.style.Theme_Weighday);
        parent = new FrameLayout(context);
        adapter = new WeightHistoryAdapter();
    }

    private WeightEntryEntity entry(double weight, LocalDateTime date) {
        return new WeightEntryEntity(weight, "", date, "u", null);
    }

    @Test
    public void itemCount_isZeroInitially() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void setEntries_updatesItemCount() {
        adapter.setEntries(Arrays.asList(
                entry(200, LocalDateTime.of(2026, 1, 1, 8, 0)),
                entry(199, LocalDateTime.of(2026, 1, 2, 8, 0))));
        assertEquals(2, adapter.getItemCount());

        adapter.setEntries(Collections.emptyList());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void bind_showsFormattedWeightAndDate() {
        adapter.setEntries(Collections.singletonList(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0))));
        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        assertEquals("185.5 lbs", ((TextView) holder.itemView.findViewById(R.id.text_weight)).getText().toString());
        assertEquals("Mar 05, 2026", ((TextView) holder.itemView.findViewById(R.id.text_date)).getText().toString());
    }

    @Test
    public void bind_withNullDate_leavesDateTextUntouched() {
        adapter.setEntries(Collections.singletonList(entry(185.5, null)));
        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        TextView dateView = holder.itemView.findViewById(R.id.text_date);
        CharSequence before = dateView.getText();

        adapter.onBindViewHolder(holder, 0);

        assertEquals(before.toString(), dateView.getText().toString());
    }
}
```

`ViewHolder` is package-private (`static class`), and this test lives in the same package, so it can access it.

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.adapters.*"`
Expected: `bind_showsFormattedWeightAndDate` and `bind_withNullDate_leavesDateTextUntouched` FAIL with `android.content.res.Resources$NotFoundException: String resource ID #0x…`. That is bug #1.

- [ ] **Step 3: Fix the adapter**

In `onBindViewHolder`, replace

```java
        holder.textWeight.setText(

                Resources.getSystem().
                getString(R.string.goal_weight_text, entry.weight)
        );
```

with

```java
        holder.textWeight.setText(
                holder.itemView.getContext().getString(R.string.goal_weight_text, entry.weight)
        );
```

and delete the now-unused `import android.content.res.Resources;`.

- [ ] **Step 4: Run to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.adapters.*"`
Expected: 4 tests PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src && git commit -m "fix: history adapter used system resources for an app string; add adapter tests"
```

---

### Task 8: SegmentedProgressView

The view keeps `progress` private. Add a getter so tests can assert clamping, and record `drawArc` calls with a Canvas subclass.

**Files:**
- Modify: `app/src/main/java/xyz/drreub/weighday/ui/components/SegmentedProgressView.java`
- Test: `app/src/test/java/xyz/drreub/weighday/ui/components/SegmentedProgressViewTest.java`

- [ ] **Step 1: Add the getter** (below `setProgress`)

```java
    public float getProgress() {
        return progress;
    }
```

- [ ] **Step 2: Write the tests**

```java
package xyz.drreub.weighday.ui.components;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SegmentedProgressViewTest {

    private static class Arc {
        final float start, sweep;
        final int color;
        Arc(float start, float sweep, int color) { this.start = start; this.sweep = sweep; this.color = color; }
    }

    private static class RecordingCanvas extends Canvas {
        final List<Arc> arcs = new ArrayList<>();
        @Override
        public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
            arcs.add(new Arc(startAngle, sweepAngle, paint.getColor()));
        }
    }

    private Context context;
    private SegmentedProgressView view;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        view = new SegmentedProgressView(context);
        view.layout(0, 0, 400, 400);
    }

    private List<Arc> draw() {
        RecordingCanvas canvas = new RecordingCanvas();
        view.draw(canvas);
        return canvas.arcs;
    }

    @Test
    public void attributeConstructor_works() {
        assertEquals(0f, new SegmentedProgressView(context, null).getProgress(), 0f);
    }

    @Test
    public void setProgress_clampsToZeroAndOne() {
        view.setProgress(-3f);
        assertEquals(0f, view.getProgress(), 0f);
        view.setProgress(7f);
        assertEquals(1f, view.getProgress(), 0f);
        view.setProgress(0.25f);
        assertEquals(0.25f, view.getProgress(), 0f);
    }

    @Test
    public void onMeasure_isSquareUsingSmallerSide() {
        view.measure(View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY));
        assertEquals(300, view.getMeasuredWidth());
        assertEquals(300, view.getMeasuredHeight());
    }

    @Test
    public void draw_zeroProgress_drawsOnlyEightBackgroundSegments() {
        view.setProgress(0f);
        List<Arc> arcs = draw();
        assertEquals(8, arcs.size());
        for (Arc a : arcs) {
            assertEquals(Color.LTGRAY, a.color);
            assertEquals(45f - 6f, a.sweep, 0.001f); // degreesPerSegment - gap
        }
    }

    @Test
    public void draw_halfProgress_fillsFirstFourSegmentsWithTheirColors() {
        view.setProgress(0.5f);
        List<Arc> arcs = draw();
        assertEquals(8 + 4, arcs.size());
        assertEquals(Color.RED, arcs.get(8).color);
        assertEquals(Color.RED, arcs.get(9).color);
        assertEquals(0xFFFFA500, arcs.get(10).color);
        assertEquals(0xFFFFA500, arcs.get(11).color);
    }

    @Test
    public void draw_fullProgress_fillsAllEightSegments() {
        view.setProgress(1f);
        List<Arc> arcs = draw();
        assertEquals(16, arcs.size());
        assertEquals(Color.GREEN, arcs.get(15).color);
    }

    @Test
    public void draw_partialSegment_drawsPartialSweep() {
        view.setProgress(0.0625f); // 22.5 degrees = half of the first 45-degree segment
        List<Arc> arcs = draw();
        assertEquals(9, arcs.size());
        assertEquals(22.5f, arcs.get(8).sweep, 0.001f);
    }
}
```

- [ ] **Step 3: Run and commit**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.components.*"`
Expected: 7 tests PASS. If `paint.getColor()` inside the recording canvas always reads `0` under the default graphics mode, add `@GraphicsMode(GraphicsMode.Mode.LEGACY)` (from `org.robolectric.annotation`) to the test class.

```bash
git add app/src && git commit -m "test: cover SegmentedProgressView; expose getProgress"
```

---

### Task 9: Fragments

**Files:**
- Test: `.../ui/fragments/WeightTrackerFragmentTest.java`, `AddWeightFragmentTest.java`, `WeightHistoryFragmentTest.java`, `SettingsFragmentTest.java`
- Modify: `app/src/main/java/xyz/drreub/weighday/ui/fragments/AddWeightFragment.java` (bug #2)

All fragment tests extend `BaseDbTest`, launch with `FragmentScenario.launchInContainer(Fragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null)`, and seed data through `db.*Dao()` **before** launching. Room invalidation posts to the main looper, so after seeding or writing, call `shadowOf(Looper.getMainLooper()).idle()`.

- [ ] **Step 1: Write `WeightTrackerFragmentTest`**

```java
package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowAlertDialog;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;
import xyz.drreub.weighday.ui.components.SegmentedProgressView;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeightTrackerFragmentTest extends BaseDbTest {

    private TestNavHostController nav;
    private FragmentScenario<WeightTrackerFragment> scenario;

    @Before
    public void setUpNav() {
        nav = new TestNavHostController(ApplicationProvider.getApplicationContext());
    }

    private void seedGoal(double goal, double start) {
        db.weightGoalDao().insert(new WeightGoalEntity(goal, start, LocalDate.now(), null, USER));
    }

    private void seedEntry(double weight) {
        db.weightEntryDao().insert(new WeightEntryEntity(weight, "", LocalDateTime.now(), USER, null));
    }

    private void launch() {
        scenario = FragmentScenario.launchInContainer(WeightTrackerFragment.class, null,
                R.style.Theme_Weighday, (androidx.fragment.app.FragmentFactory) null);
        scenario.onFragment(f -> {
            nav.setGraph(R.navigation.nav_graph);
            Navigation.setViewNavController(f.requireView(), nav);
        });
        shadowOf(Looper.getMainLooper()).idle();
    }

    private <T extends View> T find(int id) {
        final Object[] holder = new Object[1];
        scenario.onFragment(f -> holder[0] = f.requireView().findViewById(id));
        //noinspection unchecked
        return (T) holder[0];
    }

    @Test
    public void noGoalNoEntry_showsSetGoalPromptAndDisablesAdd() {
        launch();
        assertEquals("SET GOAL", ((TextView) find(R.id.text_goal_label)).getText().toString());
        assertEquals(View.GONE, find(R.id.text_goal_weight).getVisibility());
        assertEquals("No weight recorded yet", ((TextView) find(R.id.text_last_weight)).getText().toString());
        assertFalse(find(R.id.button_add_weight).isEnabled());
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void goalAndEntry_showGoalLastWeightAndProgress() {
        seedGoal(180, 200);
        seedEntry(190);
        launch();

        assertEquals("GOAL", ((TextView) find(R.id.text_goal_label)).getText().toString());
        assertEquals("180.0 lbs", ((TextView) find(R.id.text_goal_weight)).getText().toString());
        assertEquals(View.VISIBLE, find(R.id.text_goal_weight).getVisibility());
        assertEquals("Last recorded weight: 190.0 lbs",
                ((TextView) find(R.id.text_last_weight)).getText().toString());
        assertTrue(find(R.id.button_add_weight).isEnabled());
        assertEquals(0.5f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0.001f);
    }

    @Test
    public void goalWithoutEntry_progressIsZero() {
        seedGoal(180, 200);
        launch();
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void startEqualsGoal_progressIsZeroNotNaN() {
        seedGoal(180, 180);
        seedEntry(180);
        launch();
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void weightAboveStart_progressClampedToZero() {
        seedGoal(180, 200);
        seedEntry(210);
        launch();
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void reachingGoal_marksAchievedAndFillsProgress() throws Exception {
        seedGoal(180, 200);
        seedEntry(179);
        launch();
        WriteExecutorUtil.flush();

        assertEquals(1f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
        assertNotNull(db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void addWeightButton_navigatesToAddWeight() {
        seedGoal(180, 200);
        launch();
        find(R.id.button_add_weight).performClick();
        assertEquals(R.id.AddWeightFragment, nav.getCurrentDestination().getId());
    }

    @Test
    public void historyMenuItem_navigatesToHistory() {
        launch();
        scenario.onFragment(f -> {
            android.app.Activity activity = f.requireActivity();
            activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, new RoboMenuItem(R.id.action_settings));
        });
        assertEquals(R.id.WeightHistoryFragment, nav.getCurrentDestination().getId());
    }

    @Test
    public void menu_relabelsSettingsItemAsHistory() {
        launch();
        final RoboMenu[] menu = new RoboMenu[1];
        scenario.onFragment(f -> {
            menu[0] = new RoboMenu(f.requireContext());
            f.requireActivity().onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu[0]);
        });
        assertEquals("History", menu[0].findItem(R.id.action_settings).getTitle().toString());
    }

    @Test
    public void unknownMenuItem_isNotHandled() {
        launch();
        scenario.onFragment(f -> {
            boolean handled = f.requireActivity()
                    .onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, new RoboMenuItem(0));
            assertFalse(handled);
        });
        assertEquals(R.id.WeightTrackerFragment, nav.getCurrentDestination().getId());
    }

    @Test
    public void tappingGoalLabel_opensDialogPrefilledFromEntryAndGoal() {
        seedGoal(180, 200);
        seedEntry(190);
        launch();
        find(R.id.text_goal_label).performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertTrue(dialog.isShowing());
        assertEquals("190.0", ((EditText) dialog.findViewById(R.id.edit_start_weight)).getText().toString());
        assertEquals("180.0", ((EditText) dialog.findViewById(R.id.edit_goal_weight)).getText().toString());
    }

    @Test
    public void dialogPrefill_usesGoalStartWeightWhenNoEntry() {
        seedGoal(180, 200);
        launch();
        find(R.id.text_goal_weight).performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals("200.0", ((EditText) dialog.findViewById(R.id.edit_start_weight)).getText().toString());
    }

    @Test
    public void dialogPrefill_blankWhenNothingExists() {
        launch();
        find(R.id.progress_view).performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals("", ((EditText) dialog.findViewById(R.id.edit_start_weight)).getText().toString());
        assertEquals("", ((EditText) dialog.findViewById(R.id.edit_goal_weight)).getText().toString());
    }

    @Test
    public void dialogSave_createsGoalAndInitialEntry() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ((EditText) dialog.findViewById(R.id.edit_start_weight)).setText("210.5");
        ((EditText) dialog.findViewById(R.id.edit_goal_weight)).setText("185");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        WriteExecutorUtil.flush();
        shadowOf(Looper.getMainLooper()).idle();

        WeightGoalEntity goal = db.weightGoalDao().getMostRecentGoalSync(USER);
        assertEquals(185, goal.goalWeight, 0.0);
        assertEquals(210.5, goal.startWeight, 0.0);
        assertEquals("GOAL", ((TextView) find(R.id.text_goal_label)).getText().toString());
    }

    @Test
    public void dialogSave_withBlankFieldsDoesNothing() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        WriteExecutorUtil.flush();

        assertEquals(null, db.weightGoalDao().getMostRecentGoalSync(USER));
    }

    @Test
    public void dialogSave_withNonNumericInputDoesNothing() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ((EditText) dialog.findViewById(R.id.edit_start_weight)).setText("abc");
        ((EditText) dialog.findViewById(R.id.edit_goal_weight)).setText("1.2.3");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        WriteExecutorUtil.flush();

        assertEquals(null, db.weightGoalDao().getMostRecentGoalSync(USER));
    }

    @Test
    public void dialogCancel_dismissesWithoutSaving() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        WriteExecutorUtil.flush();

        assertFalse(dialog.isShowing());
        assertEquals(null, db.weightGoalDao().getMostRecentGoalSync(USER));
    }
}
```

- [ ] **Step 2: Run the tracker tests**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.fragments.WeightTrackerFragmentTest"`
Expected: all PASS. If a LiveData-driven assertion sees stale UI, add another `shadowOf(Looper.getMainLooper()).idle()` after the write (Room invalidation is asynchronous).

- [ ] **Step 3: Write `AddWeightFragmentTest` (including the failing rounding test for bug #2)**

```java
package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.os.Looper;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddWeightFragmentTest extends BaseDbTest {

    private TestNavHostController nav;
    private FragmentScenario<AddWeightFragment> scenario;

    @Before
    public void setUpNav() {
        nav = new TestNavHostController(ApplicationProvider.getApplicationContext());
    }

    private void launch() {
        scenario = FragmentScenario.launchInContainer(AddWeightFragment.class, null,
                R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> {
            nav.setGraph(R.navigation.nav_graph);
            nav.setCurrentDestination(R.id.AddWeightFragment);
            Navigation.setViewNavController(f.requireView(), nav);
        });
        shadowOf(Looper.getMainLooper()).idle();
    }

    private void seedEntry(double weight) {
        db.weightEntryDao().insert(new WeightEntryEntity(weight, "", LocalDateTime.now(), USER, null));
    }

    private int whole() { return picker(R.id.picker_weight_whole).getValue(); }
    private int decimal() { return picker(R.id.picker_weight_decimal).getValue(); }

    private NumberPicker picker(int id) {
        final NumberPicker[] p = new NumberPicker[1];
        scenario.onFragment(f -> p[0] = f.requireView().findViewById(id));
        return p[0];
    }

    @Test
    public void noPreviousEntry_pickersUseDefaultsAndRanges() {
        launch();
        assertEquals(150, whole());
        assertEquals(0, decimal());
        assertEquals(500, picker(R.id.picker_weight_whole).getMaxValue());
        assertEquals(9, picker(R.id.picker_weight_decimal).getMaxValue());
    }

    @Test
    public void previousEntry_prefillsPickers() {
        seedEntry(182.4);
        launch();
        assertEquals(182, whole());
        assertEquals(4, decimal());
    }

    @Test
    public void previousEntryNearNextWhole_roundsUpInsteadOfClamping() {
        seedEntry(72.96); // bug #2: decimal used to become 10 and clamp to 9 -> 72.9
        launch();
        assertEquals(73, whole());
        assertEquals(0, decimal());
    }

    @Test
    public void save_storesEntryWithPickerValueAndNote_thenPopsBackStack() throws Exception {
        db.weightGoalDao().insert(new WeightGoalEntity(180, 200, LocalDate.now(), null, USER));
        launch();
        scenario.onFragment(f -> {
            ((NumberPicker) f.requireView().findViewById(R.id.picker_weight_whole)).setValue(187);
            ((NumberPicker) f.requireView().findViewById(R.id.picker_weight_decimal)).setValue(3);
            ((EditText) f.requireView().findViewById(R.id.edit_note)).setText("after run");
            f.requireView().findViewById(R.id.button_save).performClick();
        });
        WriteExecutorUtil.flush();

        WeightEntryEntity saved = getValue(db.weightEntryDao().getMostRecentEntry(USER));
        assertEquals(187.3, saved.weight, 0.0001);
        assertEquals("after run", saved.note);
        assertEquals(Integer.valueOf(db.weightGoalDao().getMostRecentGoalSync(USER).id), saved.goalId);
    }
}
```

- [ ] **Step 4: Run to verify the rounding test fails**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.fragments.AddWeightFragmentTest"`
Expected: `previousEntryNearNextWhole_roundsUpInsteadOfClamping` FAILS (`expected:<73> but was:<72>`); the other 3 PASS.

- [ ] **Step 5: Fix `AddWeightFragment`**

Replace the two lines computing `whole` and `decimal` inside the `getLastWeight().observe` lambda:

```java
                int whole = (int) entry.weight;
                int decimal = (int) Math.round((entry.weight - whole) * 10);
```

with

```java
                int tenths = (int) Math.round(entry.weight * 10);
                int whole = tenths / 10;
                int decimal = tenths % 10;
```

- [ ] **Step 6: Run to verify all four pass**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.fragments.AddWeightFragmentTest"`
Expected: 4 tests PASS.

- [ ] **Step 7: Write `WeightHistoryFragmentTest`**

```java
package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Test;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;

public class WeightHistoryFragmentTest extends BaseDbTest {

    private int rowCount() {
        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        shadowOf(Looper.getMainLooper()).idle();
        final int[] count = new int[1];
        scenario.onFragment(f -> {
            RecyclerView rv = f.requireView().findViewById(R.id.recycler_history);
            count[0] = rv.getAdapter().getItemCount();
        });
        return count[0];
    }

    @Test
    public void emptyDatabase_showsNoRows() {
        assertEquals(0, rowCount());
    }

    @Test
    public void entries_areListedInRecycler() {
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));
        assertEquals(2, rowCount());
    }
}
```

- [ ] **Step 8: Write `SettingsFragmentTest`**

```java
package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.os.Bundle;
import android.widget.EditText;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowToast;

import xyz.drreub.weighday.R;

@RunWith(AndroidJUnit4.class)
public class SettingsFragmentTest {

    @Test
    public void newInstance_storesArguments() {
        SettingsFragment f = SettingsFragment.newInstance("a", "b");
        assertEquals("a", f.getArguments().getString("param1"));
        assertEquals("b", f.getArguments().getString("param2"));
    }

    @Test
    public void launch_withoutArguments_isFine() {
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(
                SettingsFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> assertNotNull(f.requireView().findViewById(R.id.some_button)));
        assertNull(f_args(scenario));
    }

    private Bundle f_args(FragmentScenario<SettingsFragment> scenario) {
        final Bundle[] b = new Bundle[1];
        scenario.onFragment(f -> b[0] = f.getArguments());
        return b[0];
    }

    @Test
    public void launch_withArguments_runsOnCreateWithArgs() {
        Bundle args = new Bundle();
        args.putString("param1", "x");
        args.putString("param2", "y");
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(
                SettingsFragment.class, args, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> assertEquals("x", f.getArguments().getString("param1")));
    }

    @Test
    public void button_showsEnteredTextInToast() {
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(
                SettingsFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> {
            ((EditText) f.requireView().findViewById(R.id.some_text)).setText("hello toast");
            f.requireView().findViewById(R.id.some_button).performClick();
        });
        assertEquals("hello toast", ShadowToast.getTextOfLatestToast());
    }
}
```

- [ ] **Step 9: Run all fragment tests and commit**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.fragments.*"`
Expected: all PASS.

```bash
git add app/src && git commit -m "fix: picker rounding near the next whole number; add fragment tests"
```

---

### Task 10: MainActivity

**Files:**
- Test: `app/src/test/java/xyz/drreub/weighday/MainActivityTest.java`

- [ ] **Step 1: Write the tests**

```java
package xyz.drreub.weighday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;

import xyz.drreub.weighday.testutil.BaseDbTest;

public class MainActivityTest extends BaseDbTest {

    @Test
    public void launch_setsUpToolbarAndStartsAtTracker() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.getSupportActionBar());
                NavController nav = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
                assertEquals(R.id.WeightTrackerFragment, nav.getCurrentDestination().getId());
            });
        }
    }

    @Test
    public void onCreateOptionsMenu_inflatesMenu() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                RoboMenu menu = new RoboMenu(activity);
                assertTrue(activity.onCreateOptionsMenu(menu));
                assertNotNull(menu.findItem(R.id.action_settings));
            });
        }
    }

    @Test
    public void onOptionsItemSelected_settingsIsConsumed_othersFallThrough() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertTrue(activity.onOptionsItemSelected(new RoboMenuItem(R.id.action_settings)));
                assertFalse(activity.onOptionsItemSelected(new RoboMenuItem(0)));
            });
        }
    }

    @Test
    public void onSupportNavigateUp_atStartDestination_returnsFalse_andWorksAfterNavigating() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertFalse(activity.onSupportNavigateUp());

                NavController nav = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
                nav.navigate(R.id.action_Tracker_to_History);
                assertEquals(R.id.WeightHistoryFragment, nav.getCurrentDestination().getId());

                assertTrue(activity.onSupportNavigateUp());
                assertEquals(R.id.WeightTrackerFragment, nav.getCurrentDestination().getId());
            });
        }
    }
}
```

- [ ] **Step 2: Run and commit**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.MainActivityTest"`
Expected: 4 tests PASS. `nav.navigate` may need `shadowOf(Looper.getMainLooper()).idle()` before the destination assertion under the Robolectric main looper; add it if `getCurrentDestination()` is stale.

```bash
git add app/src/test && git commit -m "test: cover MainActivity"
```

---

### Task 11: Instrumented end-to-end flow (device/emulator)

**Files:**
- Create: `app/src/androidTest/java/xyz/drreub/weighday/WeightFlowTest.java`

- [ ] **Step 1: Write the test** (uses an in-memory DB so runs never touch real user data)

```java
package xyz.drreub.weighday;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.data.local.AppDatabase;

@RunWith(AndroidJUnit4.class)
public class WeightFlowTest {

    private AppDatabase db;

    @Before
    public void useInMemoryDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstanceForTesting(db);
    }

    @After
    public void tearDown() {
        AppDatabase.setInstanceForTesting(null);
        db.close();
    }

    @Test
    public void setGoal_addWeight_thenSeeItInHistory() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            // Fresh install: nothing recorded, add button disabled
            onView(withText("SET GOAL")).check(matches(isDisplayed()));
            onView(withId(R.id.button_add_weight)).check(matches(not(isEnabled())));

            // Set a goal
            onView(withId(R.id.text_goal_label)).perform(click());
            onView(withId(R.id.edit_start_weight)).perform(replaceText("200"));
            onView(withId(R.id.edit_goal_weight)).perform(replaceText("180"));
            onView(withText("Save")).perform(click());

            onView(withText("GOAL")).check(matches(isDisplayed()));
            onView(withText("180.0 lbs")).check(matches(isDisplayed()));
            onView(withText("Last recorded weight: 200.0 lbs")).check(matches(isDisplayed()));

            // Add another weight (pickers are prefilled with the last weight, 200.0)
            onView(withId(R.id.button_add_weight)).perform(click());
            onView(withId(R.id.edit_note)).perform(replaceText("second entry"));
            onView(withId(R.id.button_save)).perform(click());

            // History shows both entries
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
            onView(withText("History")).perform(click());
            onView(withId(R.id.recycler_history)).check(matches(isDisplayed()));
            onView(withText("200.0 lbs")).check(matches(isDisplayed()));
        }
    }
}
```

- [ ] **Step 2: Run on an emulator/device**

Run: `./gradlew connectedDebugAndroidTest`
Expected: `ExampleInstrumentedTest` and `WeightFlowTest` PASS. Before Task 7's fix, the History step crashed, so this test also guards that regression. If `onView(withText("200.0 lbs"))` matches two rows (the initial entry and the new one both weigh 200.0), switch to `onView(withId(R.id.recycler_history)).check(matches(hasChildCount(2)))` (`androidx.test.espresso.matcher.ViewMatchers.hasChildCount`).

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest && git commit -m "test: add end-to-end weight flow test"
```

---

### Task 12: Coverage report and verification

- [ ] **Step 1: Run the whole unit suite with coverage**

Run: `./gradlew testDebugUnitTest createDebugUnitTestCoverageReport`
Expected: `BUILD SUCCESSFUL`, all tests PASS. The HTML report is at `app/build/reports/coverage/test/debug/index.html`.

- [ ] **Step 2: Review the report by package**

Open the report and confirm, for handwritten code in `xyz.drreub.weighday`:
- Line coverage of every handwritten class is at or above 90%. Room's generated `*_Impl` classes and view-binding classes may be ignored.
- The known-uncovered spots are only the empty `catch`/no-op branches you deliberately left as characterization (for example `SegmentedProgressView`'s empty `else if` branch).

If a handwritten class is under 90%, add a test for the specific uncovered lines shown in red rather than lowering the bar.

- [ ] **Step 3: Commit any additions**

```bash
git add app/src && git commit -m "test: close remaining coverage gaps"
```

---

## Self-Review

- **Spec coverage:** every class in the inventory table maps to a task; both bug fixes have failing-test-first steps (Tasks 7, 9); the E2E flow covers the wiring the JVM tests cannot (real navigation, real overflow menu).
- **Placeholders:** none. Every code step contains complete code.
- **Type consistency:** `BaseDbTest.USER`, `LiveDataTestUtil.getValue`, `WriteExecutorUtil.flush`, `AppDatabase.setInstanceForTesting`, the 3-arg `WeightRepository` constructor and `SegmentedProgressView.getProgress` are defined in Tasks 3 and 8 and used consistently afterward.
- **Risk notes:** Robolectric/`FragmentScenario` APIs and Room-LiveData timing are the most likely places to need small tweaks (extra `idle()` calls); each such spot has a note in its Run step.
