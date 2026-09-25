package xyz.drreub.weighday.util;

import static org.junit.Assert.assertNotNull;
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

    @Test
    public void canBeInstantiated() {
        assertNotNull(new DateConverter());
    }
}
