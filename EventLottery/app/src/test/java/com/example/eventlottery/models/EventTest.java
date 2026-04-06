package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * Unit tests for the {@link Event} model class.
 *
 * <p>Role in application: validates event registration state logic and formatted values
 * that are displayed to entrants and organizers throughout the event lottery workflow.
 *
 * <p>Outstanding issues: these tests focus on representative scenarios and do not yet
 * cover every status combination or boundary case around exact timestamps.
 */
public class EventTest {
    /**
     * Verifies that registration is considered open only when the current time falls
     * within the registration window and the event status is open.
     */
    @Test
    public void testIsRegistrationOpen() {
        Event event = new Event();
        long now = System.currentTimeMillis();

        // Case 1: Open
        event.setRegistrationOpenDate(now - 10000);
        event.setRegistrationCloseDate(now + 10000);
        event.setStatus(Event.STATUS_OPEN);
        assertTrue(event.isRegistrationOpen());

        // Case 2: Not yet open
        event.setRegistrationOpenDate(now + 10000);
        event.setRegistrationCloseDate(now + 20000);
        assertFalse(event.isRegistrationOpen());

        // Case 3: Already closed (by date)
        event.setRegistrationOpenDate(now - 20000);
        event.setRegistrationCloseDate(now - 10000);
        assertFalse(event.isRegistrationOpen());

        // Case 4: Closed status
        event.setRegistrationOpenDate(now - 10000);
        event.setRegistrationCloseDate(now + 10000);
        event.setStatus(Event.STATUS_CLOSED);
        assertFalse(event.isRegistrationOpen());
    }
    /**
     * Verifies that event prices are formatted as user-facing strings, including the
     * special free label and rounded dollar formatting.
     */
    @Test
    public void testGetFormattedPrice() {
        Event event = new Event();
        event.setPrice(0.0);
        assertEquals("Free", event.getFormattedPrice());

        event.setPrice(15.0);
        assertEquals("$15", event.getFormattedPrice());

        event.setPrice(15.99);
        assertEquals("$16", event.getFormattedPrice()); // Note: format is %.0f
    }
    /**
     * Verifies that the days-left helper returns the number of remaining whole days
     * before registration closes and returns zero once the deadline has passed.
     */
    @Test
    public void testGetDaysLeftToRegister() {
        Event event = new Event();
        long now = System.currentTimeMillis();
        long oneDayMs = 1000 * 60 * 60 * 24;

        event.setRegistrationCloseDate(now + (2 * oneDayMs) + 1000);
        assertEquals(2, event.getDaysLeftToRegister());

        event.setRegistrationCloseDate(now - 1000);
        assertEquals(0, event.getDaysLeftToRegister());
    }
}
