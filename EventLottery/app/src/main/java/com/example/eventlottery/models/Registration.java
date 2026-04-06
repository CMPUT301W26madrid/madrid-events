package com.example.eventlottery.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.DocumentId;

/**
 * Role: Data Model / Entity
 * Purpose: Represents the relationship between a User and an Event within the
 * lottery system. It tracks the specific state of a user's application (e.g.,
 * waiting, selected, or cancelled) and stores the geographical coordinates
 * of the entry if the event requires geolocation.
 *
 * Design Pattern: Data Transfer Object (DTO). This class facilitates the
 * mapping of Firestore documents into local Java objects.
 *
 *
 * Encapsulates the participation data for a user in a specific event,
 * including their current lottery status and registration metadata.
 *
 */

public class Registration {
    @DocumentId
    private String id;
    private String eventId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String status;      // "invited", "waiting", "selected", "accepted", "declined", "cancelled"
    private long joinedAt;
    private long respondedAt;
    private double latitude;
    private double longitude;
    private boolean geoVerified; // renamed from hasGeolocation to avoid boolean serialization issues

    public static final String STATUS_INVITED   = "invited";
    public static final String STATUS_WAITING   = "waiting";
    public static final String STATUS_SELECTED  = "selected";
    public static final String STATUS_ACCEPTED  = "accepted";
    public static final String STATUS_DECLINED  = "declined";
    public static final String STATUS_CANCELLED = "cancelled";

    /**
     * Default constructor required for Firebase Firestore deserialization.
     * Initializes the status to "waiting" and sets the joined timestamp to the current time.
     */
    public Registration() {
        this.joinedAt = System.currentTimeMillis();
        this.status   = STATUS_WAITING;
    }

    /**
     * Constructs a new Registration with essential event and user details.
     *
     * @param eventId   The unique ID of the associated event.
     * @param userId    The unique ID of the user registering.
     * @param userName  The display name of the user.
     * @param userEmail The email address of the user.
     */
    public Registration(String eventId, String userId, String userName, String userEmail) {
        this.eventId   = eventId;
        this.userId    = userId;
        this.userName  = userName;
        this.userEmail = userEmail;
        this.status    = STATUS_WAITING;
        this.joinedAt  = System.currentTimeMillis();
    }

    /** @return The unique Firestore document ID. */
    public String getId()               { return id; }
    /** @param id The unique Firestore document ID to set. */
    public void   setId(String id)      { this.id = id; }

    /** @return The associated event ID. */
    public String getEventId()                    { return eventId; }
    /** @param eventId The associated event ID to set. */
    public void   setEventId(String eventId)      { this.eventId = eventId; }

    /** @return The ID of the user. */
    public String getUserId()                     { return userId; }
    /** @param userId The ID of the user. */
    public void   setUserId(String userId)        { this.userId = userId; }

    /** @return The display name of the user. */
    public String getUserName()                   { return userName; }
    /** @param userName The display name of the user. */
    public void   setUserName(String userName)    { this.userName = userName; }

    /** @return The user's email address. */
    public String getUserEmail()                  { return userEmail; }
    /** @param userEmail The user's email address. */
    public void   setUserEmail(String userEmail)  { this.userEmail = userEmail; }

    /** @return The user's phone number. */
    public String getUserPhone()                  { return userPhone; }
    /** @param userPhone The user's phone number. */
    public void   setUserPhone(String userPhone)  { this.userPhone = userPhone; }

    /** @return The current lottery status (e.g., "waiting", "selected"). */
    public String getStatus()                     { return status; }
    /** @param status The new lottery status to set. */
    public void   setStatus(String status)        { this.status = status; }

    /** @return The epoch timestamp when the user joined the waiting list. */
    public long   getJoinedAt()                   { return joinedAt; }
    /** @param joinedAt The join timestamp. */
    public void   setJoinedAt(long joinedAt)      { this.joinedAt = joinedAt; }

    /** @return The epoch timestamp when the user responded to an invitation. */
    public long   getRespondedAt()                { return respondedAt; }
    /** @param respondedAt The response timestamp. */
    public void   setRespondedAt(long respondedAt){ this.respondedAt = respondedAt; }

    /** @return The latitude coordinate of the user's entry. */
    public double getLatitude()                   { return latitude; }
    /** @param latitude The latitude coordinate. */
    public void   setLatitude(double latitude)    { this.latitude = latitude; }

    /** @return The longitude coordinate of the user's entry. */
    public double getLongitude()                  { return longitude; }
    /** @param longitude The longitude coordinate. */
    public void   setLongitude(double longitude)  { this.longitude = longitude; }
    /**
     * @return True if the registration has been verified via geolocation.
     */
    // "geoVerified" stores cleanly as boolean in Firestore
    public boolean isGeoVerified()                   { return geoVerified; }
    /** @param geoVerified The geolocation verification status. */
    public void    setGeoVerified(boolean geoVerified){ this.geoVerified = geoVerified; }

    // Keep old name as alias so existing callers compile
    /**
     * Alias for isGeoVerified to support legacy code callers.
     * @return True if geolocation data is present and verified.
     */
    public boolean isHasGeolocation()                { return geoVerified; }
    /**
     * Alias for setGeoVerified to support legacy code callers.
     * @param v The geolocation verification status.
     */
    public void    setHasGeolocation(boolean v)      { this.geoVerified = v; }
}
