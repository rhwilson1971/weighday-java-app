# Design Specification: Weight Direction Indicator in History

## 1. Overview
The Weight History screen displays past recorded weights ordered by date descending (newest entry first). This feature adds a leading column to each history row displaying an indicator showing whether the weight increased, decreased, or remained unchanged relative to the immediately preceding recorded weight.

## 2. User Experience & Layout

### 2.1 Row Layout (`item_weight_entry.xml`)
The history item row (`item_weight_entry.xml`) contains three horizontally aligned elements:
1. **Direction Indicator (`ImageView` - `@id/image_direction`):**
   - Positioned on the far left (leading column).
   - Fixed size (24dp x 24dp), centered vertically.
   - Shows a directional vector icon tinted with semantic color.
2. **Date (`TextView` - `@id/text_date`):**
   - Positioned in the middle with `android:layout_weight="1"`.
   - Formatted date (e.g., "Oct 24, 2026").
3. **Weight (`TextView` - `@id/text_weight`):**
   - Positioned on the right, right-aligned.
   - Formatted weight string (e.g., "182.4 lbs").

### 2.2 Visual Styles & Assets
- **Weight Increase (Up):**
  - Icon: `ic_arrow_upward.xml` (Material arrow pointing up `↑`)
  - Tint Color: `@color/direction_up` (`#E53935` / Red)
  - Accessibility Content Description: `"Weight increased"` (`@string/direction_up_description`)
- **Weight Decrease (Down):**
  - Icon: `ic_arrow_downward.xml` (Material arrow pointing down `↓`)
  - Tint Color: `@color/direction_down` (`#43A047` / Green)
  - Accessibility Content Description: `"Weight decreased"` (`@string/direction_down_description`)
- **Weight Unchanged (Neutral):**
  - Icon: `ic_remove.xml` (Material horizontal dash/minus `—`)
  - Tint Color: `@color/direction_neutral` (`#757575` / Gray)
  - Accessibility Content Description: `"Weight unchanged"` (`@string/direction_unchanged_description`)
- **Initial / Oldest Record (Baseline):**
  - Icon: `ic_remove.xml` (Material horizontal dash/minus `—`)
  - Tint Color: `@color/direction_neutral` (`#757575` / Gray)
  - Accessibility Content Description: `"Initial recorded weight"` (`@string/direction_none_description`)

## 3. Architecture & Data Flow

### 3.1 Domain / Presentation Models
- **`WeightDirection` (Enum):**
  ```java
  public enum WeightDirection {
      UP,
      DOWN,
      UNCHANGED,
      NONE
  }
  ```
- **`WeightHistoryItem` (Class):**
  Wraps a `WeightEntryEntity` along with its computed `WeightDirection`:
  ```java
  public class WeightHistoryItem {
      private final WeightEntryEntity entity;
      private final WeightDirection direction;

      public WeightHistoryItem(WeightEntryEntity entity, WeightDirection direction) {
          this.entity = entity;
          this.direction = direction;
      }

      public WeightEntryEntity getEntity() { return entity; }
      public WeightDirection getDirection() { return direction; }
  }
  ```

### 3.2 Mapper / Calculation Logic
`WeightHistoryMapper.toHistoryItems(List<WeightEntryEntity> entries)`:
- Input entries are ordered chronologically descending (index 0 is the newest weigh-in, index `N-1` is the oldest).
- For an entry at index `i`:
  - If `i == entries.size() - 1` (the chronologically earliest entry in the list): direction is `WeightDirection.NONE`.
  - Otherwise, compare `entries.get(i).weight` with `entries.get(i + 1).weight`:
    - Difference `diff = entries.get(i).weight - entries.get(i + 1).weight`.
    - If `Math.abs(diff) < 0.001`: `WeightDirection.UNCHANGED`.
    - If `diff > 0`: `WeightDirection.UP`.
    - If `diff < 0`: `WeightDirection.DOWN`.
- If `entries` is null or empty, returns an empty list.

### 3.3 ViewModel & Adapter Integration
- `WeightHistoryViewModel` exposes `LiveData<List<WeightHistoryItem>>`:
  - Transformed from repository's `getAllEntries(userId)` using `Transformations.map(..., WeightHistoryMapper::toHistoryItems)`.
- `WeightHistoryAdapter` accepts `List<WeightHistoryItem>`:
  - Binds date and weight from `item.getEntity()`.
  - Sets icon drawable, tint color, and `contentDescription` on `@id/image_direction` based on `item.getDirection()`.

## 4. Error Handling & Edge Cases
- **Empty List:** Adapter handles 0 items gracefully without errors.
- **Single Entry:** Direction evaluated as `NONE`, displaying neutral dash with `"Initial recorded weight"`.
- **Identical Subsequent Weights:** Evaluated as `UNCHANGED` with tolerance for floating-point comparison, displaying neutral dash with `"Weight unchanged"`.
- **Multiple Entries with Identical Dates/Timestamps:** Preserves list order from database query.
- **Null Dates in Entities:** Preserves existing fallback handling without crashing.

## 5. Testing Strategy
- **Unit Tests (`WeightHistoryMapperTest`):**
  - Verify null/empty list handling.
  - Verify single entry produces `NONE`.
  - Verify weight increase produces `UP`.
  - Verify weight decrease produces `DOWN`.
  - Verify identical weights produce `UNCHANGED`.
  - Verify multi-item list with mixed directions correctly computes relative to `i + 1`.
- **Adapter Tests (`WeightHistoryAdapterTest`):**
  - Test view binding for each `WeightDirection` state: verifies correct drawable, image tint, and content description.
  - Test backward compatibility or overload methods for `setEntries(List<WeightHistoryItem>)` and `setLegacyEntries(List<WeightEntryEntity>)` if needed.
- **Fragment Tests (`WeightHistoryFragmentTest`):**
  - Verify fragment observes transformed history items and renders the direction `ImageView`.
- **Full Test Suite Verification:**
  - Run `./gradlew testDebugUnitTest` to verify 100% test pass rate with JaCoCo coverage intact.
