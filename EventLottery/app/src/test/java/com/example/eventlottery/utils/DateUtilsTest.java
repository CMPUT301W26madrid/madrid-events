package com.example.eventlottery.utils;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Calendar;

public class DateUtilsTest {

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

    @Test
    public void testFormatRelative() {
        long now = System.currentTimeMillis();
        assertEquals("just now", DateUtils.formatRelative(now - 1000));
        assertEquals("5m ago", DateUtils.formatRelative(now - 5 * 60 * 1000 - 1000));
        assertEquals("2h ago", DateUtils.formatRelative(now - 2 * 60 * 60 * 1000 - 1000));
        assertEquals("3d ago", DateUtils.formatRelative(now - 3 * 24 * 60 * 60 * 1000 - 1000));
    }

    @Test
    public void testDaysUntil() {
        long now = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000;
        
        assertEquals(0, DateUtils.daysUntil(now - 1000));
        assertEquals(1, DateUtils.daysUntil(now + oneDayMs + 1000));
        assertEquals(5, DateUtils.daysUntil(now + 5 * oneDayMs + 1000));
    }
}
