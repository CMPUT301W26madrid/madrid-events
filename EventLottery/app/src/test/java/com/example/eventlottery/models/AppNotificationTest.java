package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * Unit tests for the {@link AppNotification} model class.
 *
 * <p>Role in application: verifies that notification objects correctly store
 * constructor values and present relative time strings used by the application UI.
 *
 * <p>Outstanding issues: these tests cover core happy-path behaviour, but do not yet
 * validate edge cases such as null values or very large timestamp differences.
 */
public class AppNotificationTest {
    /**
     * Verifies that the parameterized constructor stores all provided field values
     * and leaves the notification unread by default.
     */
    @Test
    public void testConstructor() {
        AppNotification notif = new AppNotification(
                "user123", "event456", "Summer Gala",
                AppNotification.TYPE_WIN, "Congrats!", "You won!", true
        );

        assertEquals("user123", notif.getUserId());
        assertEquals("event456", notif.getEventId());
        assertEquals("Summer Gala", notif.getEventTitle());
        assertEquals(AppNotification.TYPE_WIN, notif.getType());
        assertEquals("Congrats!", notif.getTitle());
        assertEquals("You won!", notif.getMessage());
        assertTrue(notif.isActionRequired());
        assertFalse(notif.isRead());
    }
    /**
     * Verifies that relative time formatting returns short human-readable labels
     * for recent notifications across second, minute, hour, and day ranges.
     */
    @Test
    public void testGetRelativeTime() {
        AppNotification notif = new AppNotification();
        long now = System.currentTimeMillis();
        
        notif.setCreatedAt(now - 1000);
        assertEquals("just now", notif.getRelativeTime());

        notif.setCreatedAt(now - 5 * 60 * 1000 - 1000);
        assertEquals("5m", notif.getRelativeTime());

        notif.setCreatedAt(now - 3 * 60 * 60 * 1000 - 1000);
        assertEquals("3h", notif.getRelativeTime());

        notif.setCreatedAt(now - 2 * 24 * 60 * 60 * 1000 - 1000);
        assertEquals("2d", notif.getRelativeTime());
    }
}
