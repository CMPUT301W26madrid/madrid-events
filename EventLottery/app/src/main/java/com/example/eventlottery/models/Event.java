package com.example.eventlottery.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.List;

/**
 * Role: Data Model / Entity
 * Purpose: Defines the core Event entity within the system. It encapsulates all
 * metadata associated with an event, including descriptive content, scheduling
 * information, attendee capacity constraints, and geographical coordinates
 * for location-restricted waiting lists.
 *
 * Design Pattern: Data Transfer Object (DTO). This class facilitates the
 * mapping of Firestore documents into local Java objects.

 * Encapsulates the configuration and state of an event, including
 * organizer details, participant limits, and media assets.
 */
public class Event {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private String organizerId;
    private String organizerName;
    private List<String> tags;
    private long eventStartDate;
    private long eventEndDate;
    private long registrationOpenDate;
    private long registrationCloseDate;
    private int capacity;
    private int maxWaitingList; // 0 = unlimited
    private double price;
    private String posterUrl;
    private boolean privateEvent;
    private boolean requireGeolocation;
    private String status; // "open", "closed", "drawn", "completed"
    private String qrCodeContent; // deep link encoded in QR
    private int waitingListCount;
    private int acceptedCount;
    private long createdAt;
    private List<String> coOrganizerIds;

    // Status constants
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_DRAWN = "drawn";
    public static final String STATUS_COMPLETED = "completed";

    /**
     * Default constructor required for Firebase Firestore deserialization.
     * Initializes lists and sets default status and creation timestamp.
     */
    public Event() {
        this.tags = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.status = STATUS_OPEN;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Constructs a new Event with full configuration details.
     *
     * @param title                  The event title.
     * @param description            Detailed event description.
     * @param location               Human-readable location name.
     * @param organizerId            ID of the creating user.
     * @param organizerName          Name of the creating user.
     * @param eventStartDate         Start timestamp of the event.
     * @param eventEndDate           End timestamp of the event.
     * @param registrationOpenDate   Timestamp when registration begins.
     * @param registrationCloseDate  Timestamp when registration ends.
     * @param capacity               Max number of selected participants.
     * @param maxWaitingList         Max size of the waiting list.
     * @param price                  Cost of the event.
     * @param posterUrl              URL to the event image.
     * @param isPrivate              Whether the event is hidden from public lists.
     * @param requireGeolocation     Whether location is required to join.
     */
    public Event(String title, String description, String location, String organizerId,
                 String organizerName, long eventStartDate, long eventEndDate,
                 long registrationOpenDate, long registrationCloseDate,
                 int capacity, int maxWaitingList, double price, String posterUrl,
                 boolean isPrivate, boolean requireGeolocation) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.registrationOpenDate = registrationOpenDate;
        this.registrationCloseDate = registrationCloseDate;
        this.capacity = capacity;
        this.maxWaitingList = maxWaitingList;
        this.price = price;
        this.posterUrl = posterUrl;
        this.privateEvent = isPrivate;
        this.requireGeolocation = requireGeolocation;
        this.tags = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
        this.status = STATUS_OPEN;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Checks if the event is currently in the registration phase.
     *
     * @return True if current time is within registration bounds and status is open.
     */
    // Computed helpers
    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= registrationOpenDate && now <= registrationCloseDate
                && STATUS_OPEN.equals(status);
    }

    /**
     * Calculates time remaining until registration closes.
     *
     * @return Number of full days remaining.
     */
    public long getDaysLeftToRegister() {
        long now = System.currentTimeMillis();
        if (now > registrationCloseDate) return 0;
        return (registrationCloseDate - now) / (1000 * 60 * 60 * 24);
    }

    /**
     * Formats the price for UI display.
     *
     * @return "Free" if price is 0, otherwise the price prefixed with $.
     */
    public String getFormattedPrice() {
        if (price <= 0) return "Free";
        return "$" + String.format("%.0f", price);
    }

    // Getters and Setters
    /** @return Firestore document ID. */
    public String getId() { return id; }
    /** @param id Firestore document ID. */
    public void setId(String id) { this.id = id; }

    /** @return The title of the event. */
    public String getTitle() { return title; }
    /** @param title The event title. */
    public void setTitle(String title) { this.title = title; }

    /** @return Event description. */
    public String getDescription() { return description; }
    /** @param description Event description. */
    public void setDescription(String description) { this.description = description; }

    /** @return Physical location name. */
    public String getLocation() { return location; }
    /** @param location Physical location name. */
    public void setLocation(String location) { this.location = location; }

    /** @return Latitude coordinate. */
    public double getLatitude() { return latitude; }
    /** @param latitude Latitude coordinate. */
    public void setLatitude(double latitude) { this.latitude = latitude; }

    /** @return Longitude coordinate. */
    public double getLongitude() { return longitude; }
    /** @param longitude Longitude coordinate. */
    public void setLongitude(double longitude) { this.longitude = longitude; }

    /** @return ID of the organizer. */
    public String getOrganizerId() { return organizerId; }
    /** @param organizerId ID of the organizer. */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** @return Name of the organizer. */
    public String getOrganizerName() { return organizerName; }
    /** @param organizerName Name of the organizer. */
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    /** @return List of categorization tags. */
    public List<String> getTags() { return tags; }
    /** @param tags List of categorization tags. */
    public void setTags(List<String> tags) { this.tags = tags; }

    /** @return Start timestamp. */
    public long getEventStartDate() { return eventStartDate; }
    /** @param eventStartDate Start timestamp. */
    public void setEventStartDate(long eventStartDate) { this.eventStartDate = eventStartDate; }

    /** @return End timestamp. */
    public long getEventEndDate() { return eventEndDate; }
    /** @param eventEndDate End timestamp. */
    public void setEventEndDate(long eventEndDate) { this.eventEndDate = eventEndDate; }

    /** @return Registration opening timestamp. */
    public long getRegistrationOpenDate() { return registrationOpenDate; }
    /** @param registrationOpenDate Registration opening timestamp. */
    public void setRegistrationOpenDate(long registrationOpenDate) {
        this.registrationOpenDate = registrationOpenDate;
    }

    /** @return Registration closing timestamp. */
    public long getRegistrationCloseDate() { return registrationCloseDate; }
    /** @param registrationCloseDate Registration closing timestamp. */
    public void setRegistrationCloseDate(long registrationCloseDate) {
        this.registrationCloseDate = registrationCloseDate;
    }

    /** @return Maximum participant capacity. */
    public int getCapacity() { return capacity; }
    /** @param capacity Maximum participant capacity. */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /** @return Maximum waiting list size. */
    public int getMaxWaitingList() { return maxWaitingList; }
    /** @param maxWaitingList Maximum waiting list size. */
    public void setMaxWaitingList(int maxWaitingList) { this.maxWaitingList = maxWaitingList; }

    /** @return Event price. */
    public double getPrice() { return price; }
    /** @param price Event price. */
    public void setPrice(double price) { this.price = price; }

    /** @return URL to the poster image. */
    public String getPosterUrl() { return posterUrl; }
    /** @param posterUrl URL to the poster image. */
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    /**
     * @return True if the event is private.
     * Annotated with PropertyName to map "private" in Firestore to privateEvent in Java.
     */
    @PropertyName("private")
    public boolean isPrivate() { return privateEvent; }
    /** @param aPrivate Privacy status. */
    @PropertyName("private")
    public void setPrivate(boolean aPrivate) { this.privateEvent = aPrivate; }

    /** @return True if geolocation is required for registration. */
    public boolean isRequireGeolocation() { return requireGeolocation; }
    /** @param requireGeolocation Geolocation requirement status. */
    public void setRequireGeolocation(boolean requireGeolocation) {
        this.requireGeolocation = requireGeolocation;
    }

    /** @return Current event status (open, closed, drawn, completed). */
    public String getStatus() { return status; }
    /** @param status Event status. */
    public void setStatus(String status) { this.status = status; }

    /** @return The content encoded in the QR code. */
    public String getQrCodeContent() { return qrCodeContent; }
    /** @param qrCodeContent The content to be encoded. */
    public void setQrCodeContent(String qrCodeContent) { this.qrCodeContent = qrCodeContent; }

    /** @return Current count of users on the waiting list. */
    public int getWaitingListCount() { return waitingListCount; }
    /** @param waitingListCount Count of users on the waiting list. */
    public void setWaitingListCount(int waitingListCount) { this.waitingListCount = waitingListCount; }

    /** @return Count of users who have accepted the invitation. */
    public int getAcceptedCount() { return acceptedCount; }
    /** @param acceptedCount Count of accepted users. */
    public void setAcceptedCount(int acceptedCount) { this.acceptedCount = acceptedCount; }

    /** @return Creation timestamp. */
    public long getCreatedAt() { return createdAt; }
    /** @param createdAt Creation timestamp. */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /** @return List of IDs for co-organizers. */
    public List<String> getCoOrganizerIds() { return coOrganizerIds; }
    /** @param coOrganizerIds List of IDs for co-organizers. */
    public void setCoOrganizerIds(List<String> coOrganizerIds) {
        this.coOrganizerIds = coOrganizerIds;
    }
}
