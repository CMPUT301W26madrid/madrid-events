package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;
/**
 * Unit tests for the {@link User} model class.
 *
 * <p>Role in application: validates helper methods that support profile display,
 * normalized search fields, and role-based behaviour across entrant, organizer,
 * and administrator workflows.
 *
 * <p>Outstanding issues: these tests do not yet cover every optional field or profile
 * mutation path, especially around null collections and persistence integration.
 */
public class UserTest {
    /**
     * Verifies that initials are generated correctly for multi-word names, single-word
     * names, empty names, and null names.
     */
    @Test
    public void testGetInitials() {
        User user = new User();
        user.setName("John Doe");
        assertEquals("JD", user.getInitials());

        user.setName("jane smith");
        assertEquals("JS", user.getInitials());

        user.setName("Single");
        assertEquals("S", user.getInitials());

        user.setName("");
        assertEquals("?", user.getInitials());

        user.setName(null);
        assertEquals("?", user.getInitials());
    }
    /**
     * Verifies that the lowercase-name helper mirrors the current name value and returns
     * null when no name has been provided.
     */
    @Test
    public void testNameLowercase() {
        User user = new User();
        user.setName("Alice Wonderland");
        assertEquals("alice wonderland", user.getNameLowercase());

        user.setName(null);
        assertNull(user.getNameLowercase());
    }
    /**
     * Verifies that role lookup correctly reports whether a user holds a requested role
     * and safely handles missing roles.
     */
    @Test
    public void testHasRole() {
        User user = new User();
        user.setRoles(Arrays.asList("entrant", "organizer"));

        assertTrue(user.hasRole("entrant"));
        assertTrue(user.hasRole("organizer"));
        assertFalse(user.hasRole("admin"));
        assertFalse(user.hasRole(null));
    }
    /**
     * Verifies that the parameterized constructor normalizes and stores basic profile
     * fields, roles, and device identification data.
     */
    @Test
    public void testConstructor() {
        User user = new User("Bob", "BOB@example.com", "123456", Arrays.asList("entrant"), "dev123");
        assertEquals("Bob", user.getName());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("123456", user.getPhone());
        assertTrue(user.hasRole("entrant"));
        assertEquals("dev123", user.getDeviceId());
        assertEquals("bob", user.getNameLowercase());
    }
}
