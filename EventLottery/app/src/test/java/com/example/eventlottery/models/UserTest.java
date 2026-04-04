package com.example.eventlottery.models;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Arrays;

public class UserTest {

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

    @Test
    public void testNameLowercase() {
        User user = new User();
        user.setName("Alice Wonderland");
        assertEquals("alice wonderland", user.getNameLowercase());

        user.setName(null);
        assertNull(user.getNameLowercase());
    }

    @Test
    public void testHasRole() {
        User user = new User();
        user.setRoles(Arrays.asList("entrant", "organizer"));

        assertTrue(user.hasRole("entrant"));
        assertTrue(user.hasRole("organizer"));
        assertFalse(user.hasRole("admin"));
        assertFalse(user.hasRole(null));
    }

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
