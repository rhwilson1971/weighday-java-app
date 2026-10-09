package net.cynreub.weighday.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DateConverterTest {

    @Test
    public void testToLocalDateTime_validString_returnsLocalDateTime() {
        String dateString = "2023-10-15T14:30:00";
        LocalDateTime result = DateConverter.toLocalDateTime(dateString);
        assertEquals(LocalDateTime.of(2023, 10, 15, 14, 30, 0), result);
    }

    @Test
    public void testToLocalDateTime_nullString_returnsNull() {
        assertNull(DateConverter.toLocalDateTime((String) null));
    }

    @Test
    public void testFromLocalDateTime_validDate_returnsString() {
        LocalDateTime date = LocalDateTime.of(2023, 10, 15, 14, 30, 0);
        String result = DateConverter.fromLocalDateTime(date);
        assertEquals("2023-10-15T14:30", result); // LocalDateTime omits seconds if 00
    }

    @Test
    public void testFromLocalDateTime_nullDate_returnsNull() {
        assertNull(DateConverter.fromLocalDateTime(null));
    }

    @Test
    public void testToLocalDate_validString_returnsLocalDate() {
        String dateString = "2023-10-15";
        LocalDate result = DateConverter.toLocalDate(dateString);
        assertEquals(LocalDate.of(2023, 10, 15), result);
    }

    @Test
    public void testToLocalDate_nullString_returnsNull() {
        assertNull(DateConverter.toLocalDate((String) null));
    }

    @Test
    public void testFromLocalDate_validDate_returnsString() {
        LocalDate date = LocalDate.of(2023, 10, 15);
        String result = DateConverter.fromLocalDate(date);
        assertEquals("2023-10-15", result);
    }

    @Test
    public void testFromLocalDate_nullDate_returnsNull() {
        assertNull(DateConverter.fromLocalDate(null));
    }
}
