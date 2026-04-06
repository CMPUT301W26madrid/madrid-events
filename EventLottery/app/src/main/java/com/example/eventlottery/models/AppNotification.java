package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;

/**
 * Role: Data Model / Entity
 * Purpose: Represents a system-generated alert or message sent to a user.
 * It structures the data required for both in-app notification lists and
 * push notifications delivered via Firebase Cloud Messaging.
 *
 * Design Pattern: Data Transfer Object (DTO). This class facilitates the
 * mapping of Firestore documents into local Java objects.

 * Encapsulates all data related to a user notification, including type,
 * read status, and associated event details.
 */
public class AppNotification {
    @DocumentId
    private String id;
    private String userId;
    private String eventId;
    private String eventTitle;
    private String type;
    private String title;
    private String message;
    private boolean read;           // field name "read" in Firestore (NOT "isRead")
    private boolean actionRequired;
    private long createdAt;
    private String senderName;
    private int recipientCount;

    public static final String TYPE_WIN          = "win";
    public static final String TYPE_LOSS         = "loss";
    public static final String TYPE_INVITATION   = "invitation";
    public static final String TYPE_UPDATE       = "update";
    public static final String TYPE_CO_ORGANIZER = "co_organizer";
    public static final String TYPE_CANCELLED    = "cancelled";

    /**
     * Default constructor required for Firebase Firestore deserialization.
     * Initializes createdAt to the current time and read status to false.
     */
    public AppNotification() {
        this.createdAt = System.currentTimeMillis();
        this.read = false;
    }

    /**
     * Constructs a new AppNotification with specified details.
     *
     * @param userId         The unique ID of the recipient user.
     * @param eventId        The unique ID of the associated event.
     * @param eventTitle     The display title of the associated event.
     * @param type           The category of notification (e.g., TYPE_WIN).
     * @param title          The headline to be displayed in the notification.
     * @param message        The detailed body text of the notification.
     * @param actionRequired Whether the user is expected to respond to this notification.
     */
    public AppNotification(String userId, String eventId, String eventTitle,
                           String type, String title, String message, boolean actionRequired) {
        this.userId        = userId;
        this.eventId       = eventId;
        this.eventTitle    = eventTitle;
        this.type          = type;
        this.title         = title;
        this.message       = message;
        this.actionRequired = actionRequired;
        this.read          = false;
        this.createdAt     = System.currentTimeMillis();
    }

    /**
     * Generates a relative time string based on the creation timestamp.
     *
     * @return A formatted string representing elapsed time (e.g., "5m", "2h", "1d").
     */

    public String getRelativeTime() {
        long diff = System.currentTimeMillis() - createdAt;
        long minutes = diff / 60000;
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        return (hours / 24) + "d";
    }

    /** @return The Firestore document ID. */
    public String getId()                              { return id; }
    /** @param id The Firestore document ID to set. */
    public void   setId(String id)                     { this.id = id; }

    /** @return The ID of the user this notification belongs to. */
    public String getUserId()                          { return userId; }
    /** @param userId The ID of the recipient user. */
    public void   setUserId(String userId)             { this.userId = userId; }
    /** @return The unique ID of the associated event. */
    public String getEventId()                         { return eventId; }
    /** @param eventId The unique ID of the associated event. */
    public void   setEventId(String eventId)           { this.eventId = eventId; }
    /** @return The display title of the associated event. */
    public String getEventTitle()                      { return eventTitle; }
    /** @param t The display title of the associated event to set */
    public void   setEventTitle(String t)              { this.eventTitle = t; }
    /** @return The notification type string. */
    public String getType()                            { return type; }
    /** @param type The type (e.g., win, loss, invitation). */
    public void   setType(String type)                 { this.type = type; }
    /** @return The notification title. */
    public String getTitle()                           { return title; }
    /** @param title The headline to display. */
    public void   setTitle(String title)               { this.title = title; }
    /** @return The body message. */
    public String getMessage()                         { return message; }
    /** @param message The detailed description text. */
    public void   setMessage(String message)           { this.message = message; }
    /** @return True if the notification has been viewed. */
    // isRead() is the correct Java-bean getter for a boolean field named "read"
    public boolean isRead()                            { return read; }
    /** @param read The read status to set. */
    public void    setRead(boolean read)               { this.read = read; }
    /** @return True if the notification requires a user action. */
    public boolean isActionRequired()                  { return actionRequired; }
    /** @param v The action requirement status. */
    public void    setActionRequired(boolean v)        { this.actionRequired = v; }
    /** @return Epoch timestamp of creation. */
    public long   getCreatedAt()                       { return createdAt; }
    /** @param createdAt The creation timestamp. */
    public void   setCreatedAt(long createdAt)         { this.createdAt = createdAt; }
    /** @return The name of the notification sender. */
    public String getSenderName()                      { return senderName; }
    /** @param senderName The name to display as sender. */
    public void   setSenderName(String senderName)     { this.senderName = senderName; }
    /** @return The number of users who received this notification. */
    public int    getRecipientCount()                  { return recipientCount; }
    /** @param recipientCount The total count of recipients. */
    public void   setRecipientCount(int recipientCount){ this.recipientCount = recipientCount; }
}
