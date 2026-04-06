package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;

/**
 * Role: Data Model / Entity
 * Purpose: Represents a system user within the application. This class acts as a
 * structured data container for profile information, role-based access control,
 * and Firebase Cloud Messaging (FCM) tokens. It includes utility methods for
 * generating UI-ready data like name-based initials.
 *
 * Design Pattern: Data Transfer Object (DTO). This class facilitates the
 * mapping of Firestore documents into local Java objects.

 * Encapsulates all data related to a user profile, including authentication
 * metadata, contact details, and system permissions.
 */

public class User {
    @DocumentId
    private String id;
    private String name;
    private String nameLowercase; // For case-insensitive search
    private String email;
    private String phone;
    private String password; // Added for security
    private List<String> roles; // "entrant", "organizer", "admin"
    private boolean pushNotificationsEnabled;
    private String deviceId;
    private String fcmToken;
    private long createdAt;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     * Initializes the roles list, enables push notifications by default,
     * and sets the creation timestamp to the current time.
     */
    public User() {
        this.roles = new ArrayList<>();
        this.pushNotificationsEnabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Constructs a new User with specified profile and device details.
     *
     * @param name     The display name of the user.
     * @param email    The email address (will be converted to lowercase).
     * @param phone    The contact phone number.
     * @param roles    A list of assigned system roles.
     * @param deviceId The unique identifier for the user's hardware device.
     */
    public User(String name, String email, String phone, List<String> roles, String deviceId) {
        this.name = name;
        this.setName(name); // Use setter to populate nameLowercase
        this.email = email != null ? email.toLowerCase() : null;
        this.phone = phone;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.deviceId = deviceId;
        this.pushNotificationsEnabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    /** @return The unique Firestore document ID. */
    public String getId() { return id; }
    /** @param id The unique Firestore document ID to set. */
    public void setId(String id) { this.id = id; }

    /** @return The display name of the user. */
    public String getName() { return name; }
    /**
     * Sets the user's name and automatically updates the lowercase version for search.
     * @param name The display name to set.
     */
    public void setName(String name) {
        this.name = name;
        this.nameLowercase = name != null ? name.toLowerCase() : null;
    }

    /** @return The lowercase name used for case-insensitive database queries. */
    public String getNameLowercase() { return nameLowercase; }
    /** @param nameLowercase The lowercase name to set. */
    public void setNameLowercase(String nameLowercase) { this.nameLowercase = nameLowercase; }

    /** @return The user's email address. */
    public String getEmail() { return email; }
    /**
     * Sets the email address, ensuring it is stored in lowercase.
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase() : null;
    }

    /** @return The user's phone number. */
    public String getPhone() { return phone; }
    /** @param phone The phone number to set. */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return The user's account password. */
    public String getPassword() { return password; }
    /** @param password The password to set. */
    public void setPassword(String password) { this.password = password; }

    /** @return The list of roles assigned to the user. */
    public List<String> getRoles() { return roles; }
    /** @param roles The list of roles to set. */
    public void setRoles(List<String> roles) { this.roles = roles; }

    /** @return True if the user has opted into push notifications. */
    public boolean isPushNotificationsEnabled() { return pushNotificationsEnabled; }
    /** @param pushNotificationsEnabled The notification preference status. */
    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }

    /** @return The unique ID of the user's device. */
    public String getDeviceId() { return deviceId; }
    /** @param deviceId The device ID to set. */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /** @return The Firebase Cloud Messaging registration token. */
    public String getFcmToken() { return fcmToken; }
    /** @param fcmToken The FCM token to set. */
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    /** @return The epoch timestamp when the user profile was created. */
    public long getCreatedAt() { return createdAt; }
    /** @param createdAt The creation timestamp to set. */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Helper method to verify if the user possesses a specific system role.
     *
     * @param role The role string to check (e.g., "admin").
     * @return True if the user has the specified role, false otherwise.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Extracts and formats initials from the user's name for UI avatar placeholders.
     *
     * @return A capitalized string of initials (e.g., "JD" for "John Doe"), or "?" if invalid.
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(name.charAt(0)).toUpperCase();
    }
}
