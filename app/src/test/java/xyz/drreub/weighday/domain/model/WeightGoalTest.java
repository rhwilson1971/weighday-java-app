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
