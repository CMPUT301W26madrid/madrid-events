package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;

public class AppNotificationTest {

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
