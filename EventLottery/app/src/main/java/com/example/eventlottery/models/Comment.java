package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;

/**
 * Role: Data Model / Entity
 * Purpose: Represents a user-generated comment on an event page. It facilitates
 * real-time communication and moderation by storing message content, authorship
 * timestamps, and associated user identifiers.
 *
 * Design Pattern: Data Transfer Object (DTO). This class facilitates the
 * mapping of Firestore documents into local Java objects.

 * Encapsulates the data for a single comment, including the sender's identity,
 * the text content, and metadata such as creation time and edit status.
 */
public class Comment {
    @DocumentId
    private String id;
    private String eventId;
    private String userId;
    private String userName;
    private String text;
    private long createdAt;
    private boolean edited;

    /**
     * Default constructor required for Firebase Firestore deserialization.
     * Initializes the timestamp to the current system time and sets edited status to false.
     */
    public Comment() {
        this.createdAt = System.currentTimeMillis();
        this.edited = false;
    }

    /**
     * Constructs a new Comment with specified authorship and content.
     *
     * @param eventId  The unique ID of the event where the comment is posted.
     * @param userId   The unique ID of the user posting the comment.
     * @param userName The display name of the user.
     * @param text     The text content of the comment.
     */
    public Comment(String eventId, String userId, String userName, String text) {
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
        this.edited = false;
    }

    /** @return The unique Firestore document ID. */
    public String getId() { return id; }
    /** @param id The unique Firestore document ID to set. */
    public void setId(String id) { this.id = id; }

    /** @return The ID of the event this comment belongs to. */
    public String getEventId() { return eventId; }
    /** @param eventId The event ID to associate with this comment. */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return The ID of the user who authored the comment. */
    public String getUserId() { return userId; }
    /** @param userId The ID of the author. */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return The display name of the author. */
    public String getUserName() { return userName; }
    /** @param userName The display name to set for the author. */
    public void setUserName(String userName) { this.userName = userName; }

    /** @return The text content of the comment. */
    public String getText() { return text; }
    /** @param text The text content to set. */
    public void setText(String text) { this.text = text; }

    /** @return The epoch timestamp when the comment was created. */
    public long getCreatedAt() { return createdAt; }
    /** @param createdAt The creation timestamp to set. */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /** @return True if the comment has been modified after creation. */
    public boolean isEdited() { return edited; }
    /** @param edited The modified status to set. */
    public void setEdited(boolean edited) { this.edited = edited; }

    /**
     * Extracts and returns the initials of the user's name for display in avatars.
     * If the name contains multiple parts, it returns the first letter of the first
     * and second parts. Otherwise, it returns the first letter of the name.
     *
     * @return A capitalized string of initials (e.g., "JD" for "John Doe"), or "?" if empty.
     */
    public String getInitials() {
        if (userName == null || userName.isEmpty()) return "?";
        String[] parts = userName.trim().split("\\s+");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase()
                    + String.valueOf(parts[1].charAt(0)).toUpperCase();
        }
        return String.valueOf(userName.charAt(0)).toUpperCase();
    }
}
