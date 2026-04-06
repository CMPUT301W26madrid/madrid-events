package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * Unit tests for the {@link Registration} model class.
 *
 * <p>Role in application: validates default registration state, constructor behaviour,
 * status constants, and geolocation flag aliases used by the waitlist and lottery flows.
 *
 * <p>Outstanding issues: replacement-draw and invitation transition rules are tested
 * elsewhere and are not fully covered in this class.
 */
public class RegistrationTest {
    /**
     * Verifies that the default constructor initializes a waiting registration with
     * a valid join timestamp.
     */
    @Test
    public void testDefaultConstructor() {
        Registration reg = new Registration();
        assertEquals(Registration.STATUS_WAITING, reg.getStatus());
        assertTrue(reg.getJoinedAt() > 0);
    }
    /**
     * Verifies that the parameterized constructor stores the supplied event and user
     * information while keeping the initial status as waiting.
     */
    @Test
    public void testParameterizedConstructor() {
        Registration reg = new Registration("event123", "user456", "John Doe", "john@example.com");
        assertEquals("event123", reg.getEventId());
        assertEquals("user456", reg.getUserId());
        assertEquals("John Doe", reg.getUserName());
        assertEquals("john@example.com", reg.getUserEmail());
        assertEquals(Registration.STATUS_WAITING, reg.getStatus());
    }
    /**
     * Verifies that the registration status constants match the string values expected
     * across the application's lottery lifecycle.
     */
    @Test
    public void testStatusConstants() {
        assertEquals("invited", Registration.STATUS_INVITED);
        assertEquals("waiting", Registration.STATUS_WAITING);
        assertEquals("selected", Registration.STATUS_SELECTED);
        assertEquals("accepted", Registration.STATUS_ACCEPTED);
        assertEquals("declined", Registration.STATUS_DECLINED);
        assertEquals("cancelled", Registration.STATUS_CANCELLED);
    }
    /**
     * Verifies that the geolocation helper and alias methods stay synchronized when the
     * underlying geolocation verification flag is changed.
     */
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
