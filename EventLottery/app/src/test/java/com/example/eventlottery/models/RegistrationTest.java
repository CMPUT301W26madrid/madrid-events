package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;

public class RegistrationTest {

    @Test
    public void testDefaultConstructor() {
        Registration reg = new Registration();
        assertEquals(Registration.STATUS_WAITING, reg.getStatus());
        assertTrue(reg.getJoinedAt() > 0);
    }

    @Test
    public void testParameterizedConstructor() {
        Registration reg = new Registration("event123", "user456", "John Doe", "john@example.com");
        assertEquals("event123", reg.getEventId());
        assertEquals("user456", reg.getUserId());
        assertEquals("John Doe", reg.getUserName());
        assertEquals("john@example.com", reg.getUserEmail());
        assertEquals(Registration.STATUS_WAITING, reg.getStatus());
    }

    @Test
    public void testStatusConstants() {
        assertEquals("invited", Registration.STATUS_INVITED);
        assertEquals("waiting", Registration.STATUS_WAITING);
        assertEquals("selected", Registration.STATUS_SELECTED);
        assertEquals("accepted", Registration.STATUS_ACCEPTED);
        assertEquals("declined", Registration.STATUS_DECLINED);
        assertEquals("cancelled", Registration.STATUS_CANCELLED);
    }

    @Test
    public void testGeoVerifiedAlias() {
        Registration reg = new Registration();
        reg.setGeoVerified(true);
        assertTrue(reg.isGeoVerified());
        assertTrue(reg.isHasGeolocation());

        reg.setHasGeolocation(false);
        assertFalse(reg.isGeoVerified());
        assertFalse(reg.isHasGeolocation());
    }
}
