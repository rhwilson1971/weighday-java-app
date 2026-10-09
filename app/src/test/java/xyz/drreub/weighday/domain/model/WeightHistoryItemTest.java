package xyz.drreub.weighday.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.time.LocalDateTime;

public class WeightHistoryItemTest {

    /**
     * Verifies that construction retains the supplied entity and direction.
     */
    @Test
    public void constructor_setsEntityAndDirection() {
        WeightEntryEntity entity = new WeightEntryEntity(180.0, "notes", LocalDateTime.now(), "user1", null);
        WeightHistoryItem item = new WeightHistoryItem(entity, WeightDirection.UP);

        assertEquals(entity, item.getEntity());
        assertEquals(WeightDirection.UP, item.getDirection());
    }

    /**
     * Verifies that all four supported direction values can be resolved by name.
     */
    @Test
    public void enumValues_containAllDirections() {
        assertNotNull(WeightDirection.valueOf("UP"));
        assertNotNull(WeightDirection.valueOf("DOWN"));
        assertNotNull(WeightDirection.valueOf("UNCHANGED"));
        assertNotNull(WeightDirection.valueOf("NONE"));
        assertEquals(4, WeightDirection.values().length);
    }

    /**
     * Checks equality and hash consistency for shared entities and differing directions.
     */
    @Test
    public void equalsAndHashCode_matchContract() {
        WeightEntryEntity entity1 = new WeightEntryEntity(180.0, "notes", LocalDateTime.now(), "user1", null);
        WeightHistoryItem item1 = new WeightHistoryItem(entity1, WeightDirection.UP);
        WeightHistoryItem item2 = new WeightHistoryItem(entity1, WeightDirection.UP);
        WeightHistoryItem item3 = new WeightHistoryItem(entity1, WeightDirection.DOWN);

        assertEquals(item1, item1);
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
        assertNotEquals(item1, null);
        assertNotEquals(item1, new Object());
    }

    /**
     * Verifies that a history item rejects a null entity.
     */
    @Test(expected = NullPointerException.class)
    public void constructor_nullEntity_throwsNullPointerException() {
        new WeightHistoryItem(null, WeightDirection.UP);
    }

    /**
     * Verifies that a history item rejects a null direction.
     */
    @Test(expected = NullPointerException.class)
    public void constructor_nullDirection_throwsNullPointerException() {
        WeightEntryEntity entity = new WeightEntryEntity(180.0, "notes", LocalDateTime.now(), "user1", null);
        new WeightHistoryItem(entity, null);
    }
}
