package xyz.drreub.weighday.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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

    @Test
    public void constructor_isPrivate() throws Exception {
        Constructor<WeightHistoryMapper> constructor = WeightHistoryMapper.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
