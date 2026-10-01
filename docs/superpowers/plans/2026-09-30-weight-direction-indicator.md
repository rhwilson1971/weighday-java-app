# Weight Direction Indicator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a leading direction indicator column (green arrow for weight decrease, red arrow for weight increase, gray dash for unchanged or initial entry) to the Weight History list.

**Architecture:** Transform repository entities (`WeightEntryEntity`) into UI presentation models (`WeightHistoryItem`) containing precomputed `WeightDirection` states (`UP`, `DOWN`, `UNCHANGED`, `NONE`). The `WeightHistoryAdapter` binds these states to a leading `ImageView` using tinted Material vector drawables and accessible content descriptions.

**Tech Stack:** Java 11, Android SDK (minSdk 26), AndroidX Room, AndroidX Lifecycle (LiveData, Transformations), JUnit 4, Robolectric.

---

### Task 1: Add Direction Resources (Colors, Strings, and Vector Drawables)

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/drawable/ic_arrow_upward.xml`
- Create: `app/src/main/res/drawable/ic_arrow_downward.xml`
- Create: `app/src/main/res/drawable/ic_remove.xml`

- [ ] **Step 1: Add direction colors to `colors.xml`**

In `app/src/main/res/values/colors.xml`, add the semantic colors for up, down, and neutral states:

```xml
    <!-- Direction Indicator Colors -->
    <color name="direction_up">#E53935</color>
    <color name="direction_down">#43A047</color>
    <color name="direction_neutral">#757575</color>
```

- [ ] **Step 2: Add accessibility strings to `strings.xml`**

In `app/src/main/res/values/strings.xml`, add accessibility content description strings:

```xml
    <string name="direction_up_description">Weight increased</string>
    <string name="direction_down_description">Weight decreased</string>
    <string name="direction_unchanged_description">Weight unchanged</string>
    <string name="direction_none_description">Initial recorded weight</string>
```

- [ ] **Step 3: Create vector drawable `ic_arrow_upward.xml`**

Create `app/src/main/res/drawable/ic_arrow_upward.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M4,12l1.41,1.41L11,7.83V20h2V7.83l5.58,5.59L20,12l-8,-8 -8,8z" />
</vector>
```

- [ ] **Step 4: Create vector drawable `ic_arrow_downward.xml`**

Create `app/src/main/res/drawable/ic_arrow_downward.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z" />
</vector>
```

- [ ] **Step 5: Create vector drawable `ic_remove.xml`**

Create `app/src/main/res/drawable/ic_remove.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,13H5v-2h14v2z" />
</vector>
```

- [ ] **Step 6: Verify resource build**

Run: `./gradlew :app:mergeDebugResources`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit resources**

```bash
git add app/src/main/res/values/colors.xml app/src/main/res/values/strings.xml app/src/main/res/drawable/ic_arrow_upward.xml app/src/main/res/drawable/ic_arrow_downward.xml app/src/main/res/drawable/ic_remove.xml
git commit -m "feat: add direction indicator colors, strings, and vector drawables"
```

---

### Task 2: Create Presentation Models (`WeightDirection` and `WeightHistoryItem`)

**Files:**
- Create: `app/src/main/java/xyz/drreub/weighday/domain/model/WeightDirection.java`
- Create: `app/src/main/java/xyz/drreub/weighday/domain/model/WeightHistoryItem.java`
- Create: `app/src/test/java/xyz/drreub/weighday/domain/model/WeightHistoryItemTest.java`

- [ ] **Step 1: Write test for `WeightHistoryItem` and `WeightDirection`**

Create `app/src/test/java/xyz/drreub/weighday/domain/model/WeightHistoryItemTest.java`:

```java
package xyz.drreub.weighday.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.time.LocalDateTime;

public class WeightHistoryItemTest {

    @Test
    public void constructor_setsEntityAndDirection() {
        WeightEntryEntity entity = new WeightEntryEntity(180.0, "notes", LocalDateTime.now(), "user1", null);
        WeightHistoryItem item = new WeightHistoryItem(entity, WeightDirection.UP);

        assertEquals(entity, item.getEntity());
        assertEquals(WeightDirection.UP, item.getDirection());
    }

    @Test
    public void enumValues_containAllDirections() {
        assertNotNull(WeightDirection.valueOf("UP"));
        assertNotNull(WeightDirection.valueOf("DOWN"));
        assertNotNull(WeightDirection.valueOf("UNCHANGED"));
        assertNotNull(WeightDirection.valueOf("NONE"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.domain.model.WeightHistoryItemTest"`
Expected: Compilation failure because `WeightDirection` and `WeightHistoryItem` do not exist yet.

- [ ] **Step 3: Implement `WeightDirection`**

Create `app/src/main/java/xyz/drreub/weighday/domain/model/WeightDirection.java`:

```java
package xyz.drreub.weighday.domain.model;

public enum WeightDirection {
    UP,
    DOWN,
    UNCHANGED,
    NONE
}
```

- [ ] **Step 4: Implement `WeightHistoryItem`**

Create `app/src/main/java/xyz/drreub/weighday/domain/model/WeightHistoryItem.java`:

```java
package xyz.drreub.weighday.domain.model;

import androidx.annotation.NonNull;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.Objects;

public class WeightHistoryItem {
    @NonNull
    private final WeightEntryEntity entity;
    @NonNull
    private final WeightDirection direction;

    public WeightHistoryItem(@NonNull WeightEntryEntity entity, @NonNull WeightDirection direction) {
        this.entity = Objects.requireNonNull(entity, "entity must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
    }

    @NonNull
    public WeightEntryEntity getEntity() {
        return entity;
    }

    @NonNull
    public WeightDirection getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightHistoryItem that = (WeightHistoryItem) o;
        return entity.equals(that.entity) && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, direction);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.domain.model.WeightHistoryItemTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 6: Commit presentation models**

```bash
git add app/src/main/java/xyz/drreub/weighday/domain/model/WeightDirection.java app/src/main/java/xyz/drreub/weighday/domain/model/WeightHistoryItem.java app/src/test/java/xyz/drreub/weighday/domain/model/WeightHistoryItemTest.java
git commit -m "feat: add WeightDirection and WeightHistoryItem presentation models"
```

---

### Task 3: Implement `WeightHistoryMapper` with Unit Tests (TDD)

**Files:**
- Create: `app/src/test/java/xyz/drreub/weighday/domain/model/WeightHistoryMapperTest.java`
- Create: `app/src/main/java/xyz/drreub/weighday/domain/model/WeightHistoryMapper.java`

- [ ] **Step 1: Write unit tests covering all calculation edge cases**

Create `app/src/test/java/xyz/drreub/weighday/domain/model/WeightHistoryMapperTest.java`:

```java
package xyz.drreub.weighday.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeightHistoryMapperTest {

    private WeightEntryEntity entry(double weight, LocalDateTime date) {
        return new WeightEntryEntity(weight, "", date, "user", null);
    }

    @Test
    public void toHistoryItems_nullOrEmpty_returnsEmptyList() {
        assertTrue(WeightHistoryMapper.toHistoryItems(null).isEmpty());
        assertTrue(WeightHistoryMapper.toHistoryItems(Collections.emptyList()).isEmpty());
    }

    @Test
    public void toHistoryItems_singleEntry_returnsNone() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 8, 0);
        List<WeightEntryEntity> entries = Collections.singletonList(entry(180.0, date));

        List<WeightHistoryItem> items = WeightHistoryMapper.toHistoryItems(entries);

        assertEquals(1, items.size());
        assertEquals(WeightDirection.NONE, items.get(0).getDirection());
        assertEquals(180.0, items.get(0).getEntity().weight, 0.001);
    }

    @Test
    public void toHistoryItems_weightIncrease_returnsUp() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 8, 0);
        // Descending order: index 0 is newest (182.0), index 1 is older (180.0)
        List<WeightEntryEntity> entries = Arrays.asList(
                entry(182.0, date.plusDays(1)),
                entry(180.0, date)
        );

        List<WeightHistoryItem> items = WeightHistoryMapper.toHistoryItems(entries);

        assertEquals(2, items.size());
        assertEquals(WeightDirection.UP, items.get(0).getDirection());
        assertEquals(WeightDirection.NONE, items.get(1).getDirection());
    }

    @Test
    public void toHistoryItems_weightDecrease_returnsDown() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 8, 0);
        // Descending order: index 0 is newest (178.5), index 1 is older (180.0)
        List<WeightEntryEntity> entries = Arrays.asList(
                entry(178.5, date.plusDays(1)),
                entry(180.0, date)
        );

        List<WeightHistoryItem> items = WeightHistoryMapper.toHistoryItems(entries);

        assertEquals(2, items.size());
        assertEquals(WeightDirection.DOWN, items.get(0).getDirection());
        assertEquals(WeightDirection.NONE, items.get(1).getDirection());
    }

    @Test
    public void toHistoryItems_equalWeight_returnsUnchanged() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 8, 0);
        List<WeightEntryEntity> entries = Arrays.asList(
                entry(180.0, date.plusDays(1)),
                entry(180.0, date)
        );

        List<WeightHistoryItem> items = WeightHistoryMapper.toHistoryItems(entries);

        assertEquals(2, items.size());
        assertEquals(WeightDirection.UNCHANGED, items.get(0).getDirection());
        assertEquals(WeightDirection.NONE, items.get(1).getDirection());
    }

    @Test
    public void toHistoryItems_multiEntryMixedSequence_mapsCorrectly() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 8, 0);
        // Descending order:
        // Day 4: 179.0 (vs Day 3: 180.0 -> DOWN)
        // Day 3: 180.0 (vs Day 2: 180.0 -> UNCHANGED)
        // Day 2: 180.0 (vs Day 1: 178.0 -> UP)
        // Day 1: 178.0 (oldest -> NONE)
        List<WeightEntryEntity> entries = Arrays.asList(
                entry(179.0, base.plusDays(3)),
                entry(180.0, base.plusDays(2)),
                entry(180.0, base.plusDays(1)),
                entry(178.0, base)
        );

        List<WeightHistoryItem> items = WeightHistoryMapper.toHistoryItems(entries);

        assertEquals(4, items.size());
        assertEquals(WeightDirection.DOWN, items.get(0).getDirection());
        assertEquals(WeightDirection.UNCHANGED, items.get(1).getDirection());
        assertEquals(WeightDirection.UP, items.get(2).getDirection());
        assertEquals(WeightDirection.NONE, items.get(3).getDirection());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.domain.model.WeightHistoryMapperTest"`
Expected: Compilation failure because `WeightHistoryMapper` does not exist yet.

- [ ] **Step 3: Implement `WeightHistoryMapper`**

Create `app/src/main/java/xyz/drreub/weighday/domain/model/WeightHistoryMapper.java`:

```java
package xyz.drreub.weighday.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeightHistoryMapper {

    private static final double EPSILON = 0.001;

    private WeightHistoryMapper() {
        // Utility class
    }

    @NonNull
    public static List<WeightHistoryItem> toHistoryItems(@Nullable List<WeightEntryEntity> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<WeightHistoryItem> items = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            WeightEntryEntity current = entries.get(i);
            WeightDirection direction;

            if (i == entries.size() - 1) {
                direction = WeightDirection.NONE;
            } else {
                WeightEntryEntity previous = entries.get(i + 1);
                double diff = current.weight - previous.weight;
                if (Math.abs(diff) < EPSILON) {
                    direction = WeightDirection.UNCHANGED;
                } else if (diff > 0) {
                    direction = WeightDirection.UP;
                } else {
                    direction = WeightDirection.DOWN;
                }
            }
            items.add(new WeightHistoryItem(current, direction));
        }

        return items;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.domain.model.WeightHistoryMapperTest"`
Expected: `BUILD SUCCESSFUL`, 6 tests passed.

- [ ] **Step 5: Commit `WeightHistoryMapper`**

```bash
git add app/src/main/java/xyz/drreub/weighday/domain/model/WeightHistoryMapper.java app/src/test/java/xyz/drreub/weighday/domain/model/WeightHistoryMapperTest.java
git commit -m "feat: implement WeightHistoryMapper with direction calculation logic"
```

---

### Task 4: Update Layout `item_weight_entry.xml`

**Files:**
- Modify: `app/src/main/res/layout/item_weight_entry.xml`

- [ ] **Step 1: Update `item_weight_entry.xml` to include leading direction `ImageView`**

Modify `app/src/main/res/layout/item_weight_entry.xml` to:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:padding="16dp">

    <ImageView
        android:id="@+id/image_direction"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:layout_marginEnd="16dp"
        android:contentDescription="@string/direction_none_description"
        android:src="@drawable/ic_remove" />

    <TextView
        android:id="@+id/text_date"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:textSize="16sp"
        android:text="Date"/>

    <TextView
        android:id="@+id/text_weight"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:textStyle="bold"
        android:gravity="end"
        android:text="150.0 lbs"/>

</LinearLayout>
```

- [ ] **Step 2: Verify layout compilation**

Run: `./gradlew :app:compileDebugUnitTestJavaWithJavac`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit updated layout**

```bash
git add app/src/main/res/layout/item_weight_entry.xml
git commit -m "feat: add leading image_direction ImageView to item_weight_entry.xml"
```

---

### Task 5: Update `WeightHistoryAdapter` and Adapter Tests (TDD)

**Files:**
- Modify: `app/src/test/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapterTest.java`
- Modify: `app/src/main/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapter.java`

- [ ] **Step 1: Write tests in `WeightHistoryAdapterTest` for direction icon and tint binding**

Update `app/src/test/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapterTest.java`:

```java
package xyz.drreub.weighday.ui.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.domain.model.WeightDirection;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;

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
    public void setHistoryItems_updatesItemCount() {
        WeightHistoryItem item1 = new WeightHistoryItem(entry(200, LocalDateTime.of(2026, 1, 1, 8, 0)), WeightDirection.UP);
        WeightHistoryItem item2 = new WeightHistoryItem(entry(199, LocalDateTime.of(2026, 1, 2, 8, 0)), WeightDirection.NONE);

        adapter.setHistoryItems(Arrays.asList(item1, item2));
        assertEquals(2, adapter.getItemCount());
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

    @Test
    public void bind_directionUp_setsUpIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.UP);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_up_description), imageView.getContentDescription().toString());
    }

    @Test
    public void bind_directionDown_setsDownIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.DOWN);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_down_description), imageView.getContentDescription().toString());
    }

    @Test
    public void bind_directionUnchanged_setsRemoveIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.UNCHANGED);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_unchanged_description), imageView.getContentDescription().toString());
    }

    @Test
    public void bind_directionNone_setsRemoveIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.NONE);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_none_description), imageView.getContentDescription().toString());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.adapters.WeightHistoryAdapterTest"`
Expected: Compilation failure because `setHistoryItems` is not yet defined on `WeightHistoryAdapter`.

- [ ] **Step 3: Update `WeightHistoryAdapter` implementation**

Modify `app/src/main/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapter.java`:

```java
package xyz.drreub.weighday.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.domain.model.WeightDirection;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;
import xyz.drreub.weighday.domain.model.WeightHistoryMapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {

    private List<WeightHistoryItem> items = new ArrayList<>();

    public void setHistoryItems(@Nullable List<WeightHistoryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setEntries(@Nullable List<WeightEntryEntity> entries) {
        setHistoryItems(WeightHistoryMapper.toHistoryItems(entries));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightHistoryItem item = items.get(position);
        WeightEntryEntity entry = item.getEntity();
        Context context = holder.itemView.getContext();

        holder.textWeight.setText(
                context.getString(R.string.goal_weight_text, entry.weight)
        );

        if (entry.date != null) {
            holder.textDate.setText(entry.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }

        bindDirection(holder.imageDirection, item.getDirection(), context);
    }

    private void bindDirection(@NonNull ImageView imageView, @NonNull WeightDirection direction, @NonNull Context context) {
        int iconRes;
        int colorRes;
        int descRes;

        switch (direction) {
            case UP:
                iconRes = R.drawable.ic_arrow_upward;
                colorRes = R.color.direction_up;
                descRes = R.string.direction_up_description;
                break;
            case DOWN:
                iconRes = R.drawable.ic_arrow_downward;
                colorRes = R.color.direction_down;
                descRes = R.string.direction_down_description;
                break;
            case UNCHANGED:
                iconRes = R.drawable.ic_remove;
                colorRes = R.color.direction_neutral;
                descRes = R.string.direction_unchanged_description;
                break;
            case NONE:
            default:
                iconRes = R.drawable.ic_remove;
                colorRes = R.color.direction_neutral;
                descRes = R.string.direction_none_description;
                break;
        }

        imageView.setImageResource(iconRes);
        imageView.setColorFilter(ContextCompat.getColor(context, colorRes));
        imageView.setContentDescription(context.getString(descRes));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageDirection;
        TextView textDate;
        TextView textWeight;

        ViewHolder(View itemView) {
            super(itemView);
            imageDirection = itemView.findViewById(R.id.image_direction);
            textDate = itemView.findViewById(R.id.text_date);
            textWeight = itemView.findViewById(R.id.text_weight);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.adapters.WeightHistoryAdapterTest"`
Expected: `BUILD SUCCESSFUL`, 8 tests passed.

- [ ] **Step 5: Commit `WeightHistoryAdapter` changes**

```bash
git add app/src/main/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapter.java app/src/test/java/xyz/drreub/weighday/ui/adapters/WeightHistoryAdapterTest.java
git commit -m "feat: bind direction indicators in WeightHistoryAdapter"
```

---

### Task 6: Update `WeightHistoryViewModel` and ViewModel Tests (TDD)

**Files:**
- Modify: `app/src/test/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModelTest.java`
- Modify: `app/src/main/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModel.java`

- [ ] **Step 1: Write test for `getHistoryItems()` in `WeightHistoryViewModelTest`**

Update `app/src/test/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModelTest.java`:

```java
package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.domain.model.WeightDirection;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;
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

    @Test
    public void getHistoryItems_transformsEntriesWithDirections() {
        WeightHistoryViewModel vm =
                new WeightHistoryViewModel((Application) ApplicationProvider.getApplicationContext());
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));

        List<WeightHistoryItem> items = getValue(vm.getHistoryItems());
        assertEquals(2, items.size());
        assertEquals(199, items.get(0).getEntity().weight, 0.0);
        assertEquals(WeightDirection.DOWN, items.get(0).getDirection());
        assertEquals(WeightDirection.NONE, items.get(1).getDirection());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.viewmodel.WeightHistoryViewModelTest"`
Expected: Compilation failure because `getHistoryItems()` does not exist on `WeightHistoryViewModel`.

- [ ] **Step 3: Implement `getHistoryItems()` in `WeightHistoryViewModel`**

Modify `app/src/main/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModel.java`:

```java
package xyz.drreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.repository.WeightRepository;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;
import xyz.drreub.weighday.domain.model.WeightHistoryMapper;

import java.util.List;

public class WeightHistoryViewModel extends AndroidViewModel {

    private final WeightRepository repository;
    private final String userId = "testUser";

    public WeightHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
    }

    public LiveData<List<WeightEntryEntity>> getAllEntries() {
        return repository.getAllEntries(userId);
    }

    public LiveData<List<WeightHistoryItem>> getHistoryItems() {
        return Transformations.map(getAllEntries(), WeightHistoryMapper::toHistoryItems);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.viewmodel.WeightHistoryViewModelTest"`
Expected: `BUILD SUCCESSFUL`, 2 tests passed.

- [ ] **Step 5: Commit `WeightHistoryViewModel` changes**

```bash
git add app/src/main/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModel.java app/src/test/java/xyz/drreub/weighday/ui/viewmodel/WeightHistoryViewModelTest.java
git commit -m "feat: expose getHistoryItems() LiveData in WeightHistoryViewModel"
```

---

### Task 7: Update `WeightHistoryFragment` and Fragment Tests

**Files:**
- Modify: `app/src/main/java/xyz/drreub/weighday/ui/fragments/WeightHistoryFragment.java`
- Modify: `app/src/test/java/xyz/drreub/weighday/ui/fragments/WeightHistoryFragmentTest.java`

- [ ] **Step 1: Update `WeightHistoryFragment` to observe `getHistoryItems()`**

In `app/src/main/java/xyz/drreub/weighday/ui/fragments/WeightHistoryFragment.java`, change line 43 from observing `getAllEntries()` to `getHistoryItems()`:

```java
        viewModel.getHistoryItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setHistoryItems(items);
        });
```

- [ ] **Step 2: Update `WeightHistoryFragmentTest` to verify direction images in rows**

Modify `app/src/test/java/xyz/drreub/weighday/ui/fragments/WeightHistoryFragmentTest.java`:

```java
package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.ImageView;

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
    public void entries_areListedInRecyclerWithDirectionIndicators() {
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));
        assertEquals(2, rowCount());

        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        shadowOf(Looper.getMainLooper()).idle();
        scenario.onFragment(f -> {
            RecyclerView rv = f.requireView().findViewById(R.id.recycler_history);
            RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(0);
            assertNotNull(holder);
            ImageView iv = holder.itemView.findViewById(R.id.image_direction);
            assertNotNull(iv);
            assertNotNull(iv.getDrawable());
            assertEquals("Weight decreased", iv.getContentDescription().toString());
        });
    }

    @Test
    public void destroyingView_releasesBinding() {
        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.DESTROYED);
    }
}
```

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests "xyz.drreub.weighday.ui.fragments.WeightHistoryFragmentTest"`
Expected: `BUILD SUCCESSFUL`, 3 tests passed.

- [ ] **Step 4: Commit fragment and test changes**

```bash
git add app/src/main/java/xyz/drreub/weighday/ui/fragments/WeightHistoryFragment.java app/src/test/java/xyz/drreub/weighday/ui/fragments/WeightHistoryFragmentTest.java
git commit -m "feat: observe history items and verify direction indicators in WeightHistoryFragment"
```

---

### Task 8: Full Test Suite Verification

**Files:** None (Verification only)

- [ ] **Step 1: Run complete test suite and code coverage**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all unit tests passing with zero failures.

- [ ] **Step 2: Verify git status is clean**

Run: `git status`
Expected: Working tree clean, all files tracked and committed.
