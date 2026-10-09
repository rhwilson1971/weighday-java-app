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
