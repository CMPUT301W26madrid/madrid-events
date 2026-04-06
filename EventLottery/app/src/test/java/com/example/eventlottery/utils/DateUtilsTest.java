package com.example.eventlottery.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Calendar;
/**
 * Instrumentation-style unit tests for {@link DateUtils} helper methods.
 *
 * Purpose:
 * Verifies that date formatting and relative-time utility behaviour remains
 * consistent for common application display scenarios.
 *
 * Role in application:
 * Supports the utility layer used by the Event Lottery app to present human-
 * readable dates and countdown information in the UI.
 *
 * Outstanding issues:
 * Some assertions are locale-sensitive and therefore validate key substrings
 * instead of a single fully fixed formatted date string.
 */
public class DateUtilsTest {
    /**
     * Verifies that {@link DateUtils#formatDate(long)} returns a readable date
     * string containing the expected month, day, and year components.
     */
    @Test
    public void testFormatDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 27);
        long millis = cal.getTimeInMillis();
        
        String formatted = DateUtils.formatDate(millis);
        // Result depends on locale, but should contain these
        assertTrue(formatted.contains("Oct") || formatted.contains("10"));
        assertTrue(formatted.contains("27"));
        assertTrue(formatted.contains("2023"));
    }
    /**
     * Verifies that {@link DateUtils#formatRelative(long)} produces the expected
     * relative-time labels for recently created timestamps across minute, hour,
     * and day ranges.
     */
    @Test
    public void testFormatRelative() {
        long now = System.currentTimeMillis();
        assertEquals("just now", DateUtils.formatRelative(now - 1000));
        assertEquals("5m ago", DateUtils.formatRelative(now - 5 * 60 * 1000 - 1000));
        assertEquals("2h ago", DateUtils.formatRelative(now - 2 * 60 * 60 * 1000 - 1000));
        assertEquals("3d ago", DateUtils.formatRelative(now - 3 * 24 * 60 * 60 * 1000 - 1000));
    }
    /**
     * Verifies that {@link DateUtils#daysUntil(long)} returns zero for past
     * dates and returns the correct number of whole remaining days for future
     * timestamps.
     */
    @Test
    public void testDaysUntil() {
        long now = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000;
        
        assertEquals(0, DateUtils.daysUntil(now - 1000));
        assertEquals(1, DateUtils.daysUntil(now + oneDayMs + 1000));
        assertEquals(5, DateUtils.daysUntil(now + 5 * oneDayMs + 1000));
    }
}
