package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;

public class EventTest {

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
