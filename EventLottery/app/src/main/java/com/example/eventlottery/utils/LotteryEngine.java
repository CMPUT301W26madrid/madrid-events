package com.example.eventlottery.utils;

import com.example.eventlottery.models.AppNotification;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LotteryEngine handles:
 *  1. Running the initial lottery draw (randomly selecting N winners)
 *  2. Drawing replacements when selected entrants decline
 *  3. Sending win/loss notifications to all entrants
 */
public class LotteryEngine {

    private final RegistrationRepository regRepo;
    private final NotificationRepository notifRepo;

    public LotteryEngine() {
        regRepo = new RegistrationRepository();
        notifRepo = new NotificationRepository();
    }

    public interface LotteryCallback {
        void onSuccess(int selected, int notSelected);
        void onFailure(Exception e);
    }

    /**
     * Run the lottery for the given event.
     * @param event        The event to draw for
     * @param sampleSize   How many to select (usually event.getCapacity())
     * @param callback     Result callback
     */
    public void runLottery(Event event, int sampleSize, LotteryCallback callback) {
        regRepo.getRegistrationsByStatus(event.getId(), Registration.STATUS_WAITING)
                .addOnSuccessListener(querySnapshot -> {
                    List<Registration> waitingList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Registration r = doc.toObject(Registration.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            waitingList.add(r);
                        }
                    }

                    if (waitingList.isEmpty()) {
                        callback.onSuccess(0, 0);
                        return;
                    }

                    // Shuffle for fairness
                    Collections.shuffle(waitingList);

                    int actualSample = Math.min(sampleSize, waitingList.size());
                    List<Registration> winners = new ArrayList<>(waitingList.subList(0, actualSample));
                    List<Registration> losers  = new ArrayList<>(waitingList.subList(actualSample, waitingList.size()));

                    List<Task<Void>> tasks = new ArrayList<>();

                    // Mark winners as SELECTED and notify
                    for (Registration r : winners) {
                        tasks.add(regRepo.updateStatus(event.getId(), r.getUserId(),
                                Registration.STATUS_SELECTED));
                        AppNotification notif = new AppNotification(
                                r.getUserId(), event.getId(), event.getTitle(),
                                AppNotification.TYPE_WIN,
                                "🎉 You've been selected!",
                                "Congratulations! You were selected for \"" + event.getTitle()
                                        + "\". Please accept or decline your spot.",
                                true
                        );
                        notif.setSenderName(event.getOrganizerName());
                        tasks.add(notifRepo.createNotification(notif));
                    }

                    // Notify losers
                    for (Registration r : losers) {
                        AppNotification notif = new AppNotification(
                                r.getUserId(), event.getId(), event.getTitle(),
                                AppNotification.TYPE_LOSS,
                                "Lottery Result",
                                "You were not selected in the first draw for \""
                                        + event.getTitle()
                                        + "\". Don't worry — if someone declines, "
                                        + "you may still be drawn as a replacement.",
                                false
                        );
                        notif.setSenderName(event.getOrganizerName());
                        tasks.add(notifRepo.createNotification(notif));
                    }

                    // Also notify the organizer that the draw is complete
                    AppNotification orgNotif = new AppNotification(
                            event.getOrganizerId(), event.getId(), event.getTitle(),
                            AppNotification.TYPE_UPDATE,
                            "Lottery Complete: " + event.getTitle(),
                            "The lottery draw for \"" + event.getTitle() + "\" has been completed. " +
                                    winners.size() + " winners selected, " + losers.size() + " waitlisted.",
                            false
                    );
                    orgNotif.setSenderName("System");
                    tasks.add(notifRepo.createNotification(orgNotif));

                    Tasks.whenAll(tasks)
                            .addOnSuccessListener(v -> callback.onSuccess(winners.size(), losers.size()))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Draw a single replacement from the remaining waiting list.
     */
    public void drawReplacement(Event event, LotteryCallback callback) {
        regRepo.getRegistrationsByStatus(event.getId(), Registration.STATUS_WAITING)
                .addOnSuccessListener(querySnapshot -> {
                    List<Registration> remaining = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Registration r = doc.toObject(Registration.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            remaining.add(r);
                        }
                    }

                    if (remaining.isEmpty()) {
                        callback.onSuccess(0, 0);
                        return;
                    }

                    Collections.shuffle(remaining);
                    Registration winner = remaining.get(0);

                    List<Task<Void>> tasks = new ArrayList<>();
                    tasks.add(regRepo.updateStatus(event.getId(), winner.getUserId(),
                            Registration.STATUS_SELECTED));

                    AppNotification notif = new AppNotification(
                            winner.getUserId(), event.getId(), event.getTitle(),
                            AppNotification.TYPE_WIN,
                            "🎉 You've been selected as a replacement!",
                            "Great news! A spot opened up in \"" + event.getTitle()
                                    + "\". Please accept or decline your spot.",
                            true
                    );
                    notif.setSenderName(event.getOrganizerName());
                    tasks.add(notifRepo.createNotification(notif));

                    // Notify organizer
                    AppNotification orgNotif = new AppNotification(
                            event.getOrganizerId(), event.getId(), event.getTitle(),
                            AppNotification.TYPE_UPDATE,
                            "Replacement Drawn: " + event.getTitle(),
                            "A replacement entrant has been drawn and notified for \"" + event.getTitle() + "\".",
                            false
                    );
                    orgNotif.setSenderName("System");
                    tasks.add(notifRepo.createNotification(orgNotif));

                    Tasks.whenAll(tasks)
                            .addOnSuccessListener(v -> callback.onSuccess(1, 0))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Send a bulk notification to a group of registrations.
     */
    public void sendBulkNotification(Event event, String audience, String message,
                                     String organizerName, LotteryCallback callback) {
        Task<QuerySnapshot> task;
        if ("selected".equals(audience)) {
            task = regRepo.getRegistrationsByStatus(event.getId(), Registration.STATUS_SELECTED);
        } else if ("cancelled".equals(audience)) {
            task = regRepo.getRegistrationsByStatus(event.getId(), Registration.STATUS_CANCELLED);
        } else {
            task = regRepo.getRegistrationsForEvent(event.getId());
        }

        task.addOnSuccessListener(querySnapshot -> {
            List<Task<Void>> tasks = new ArrayList<>();
            int count = 0;
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Registration r = doc.toObject(Registration.class);
                if (r == null) continue;
                count++;
                AppNotification notif = new AppNotification(
                        r.getUserId(), event.getId(), event.getTitle(),
                        AppNotification.TYPE_UPDATE,
                        "Update: " + event.getTitle(),
                        message,
                        false
                );
                notif.setSenderName(organizerName);
                notif.setRecipientCount(querySnapshot.size());
                tasks.add(notifRepo.createNotification(notif));
            }

            // Also create a copy for the organizer themselves
            AppNotification orgCopy = new AppNotification(
                    event.getOrganizerId(), event.getId(), event.getTitle(),
                    AppNotification.TYPE_UPDATE,
                    "Notification Sent: " + event.getTitle(),
                    "To " + audience + " entrants: " + message,
                    false
            );
            orgCopy.setSenderName("You");
            tasks.add(notifRepo.createNotification(orgCopy));

            Tasks.whenAll(tasks)
                    .addOnSuccessListener(v -> {
                        callback.onSuccess(tasks.size() - 1, 0); // Subtract 1 to reflect entrants count
                    })
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }
}
