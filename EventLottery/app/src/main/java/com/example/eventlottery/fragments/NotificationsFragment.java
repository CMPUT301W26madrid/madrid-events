package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.adapters.NotificationAdapter;
import com.example.eventlottery.models.AppNotification;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
/**
 * Fragment that displays the current user's in-app notifications and related actions.
 *
 * <p>Role in application: supports entrant-facing and co-organizer notification workflows,
 * including marking notifications as read, accepting or declining action-required items,
 * navigating to related events, and clearing processed notifications.</p>
 *
 * <p>Outstanding issues: some repository failure paths intentionally fall back to reloading
 * the list without surfacing detailed error messages to the user.</p>
 */
public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationActionListener {

    private SessionManager session;
    private NotificationRepository notifRepo;
    private EventRepository eventRepo;
    private RegistrationRepository regRepo;
    private NotificationAdapter adapter;
    private View llEmpty;
    private ProgressBar progress;
    /**
     * Inflates the notification list layout for this fragment.
     *
     * @param inflater the layout inflater used to create the view hierarchy
     * @param container the parent view that the fragment UI will attach to
     * @param savedInstanceState previously saved fragment state, if any
     * @return the inflated notification fragment view
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }
    /**
     * Binds views, initializes repositories, and loads the user's notifications.
     *
     * @param view the fragment root view
     * @param savedInstanceState previously saved fragment state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session   = new SessionManager(requireContext());
        notifRepo = new NotificationRepository();
        eventRepo = new EventRepository();
        regRepo   = new RegistrationRepository();

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        llEmpty  = view.findViewById(R.id.ll_empty);
        progress = view.findViewById(R.id.progress);
        TextView tvMarkRead = view.findViewById(R.id.tv_mark_read);

        adapter = new NotificationAdapter();
        adapter.setActionListener(this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        tvMarkRead.setOnClickListener(v -> markAllRead());
        loadNotifications();
    }
    /**
     * Retrieves notifications for the active user, sorts them by most recent first,
     * and updates the empty/loading states of the UI.
     */
    private void loadNotifications() {
        String userId = session.getUserId();
        if (userId == null) return;
        if (!isAdded()) return;
        progress.setVisibility(View.VISIBLE);

        notifRepo.getNotificationsForUser(userId).addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            List<AppNotification> list = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                AppNotification n = doc.toObject(AppNotification.class);
                if (n != null) { n.setId(doc.getId()); list.add(n); }
            }
            list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            
            progress.setVisibility(View.GONE);
            adapter.setNotifications(list);
            llEmpty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            if (isAdded()) progress.setVisibility(View.GONE);
        });
    }
    /**
     * Marks every notification for the active user as read and refreshes the list.
     */
    private void markAllRead() {
        String userId = session.getUserId();
        if (userId == null) return;
        notifRepo.markAllReadForUser(userId)
                .addOnSuccessListener(v -> loadNotifications())
                .addOnFailureListener(e -> loadNotifications());
    }
    /**
     * Handles acceptance of an actionable notification.
     *
     * <p>Depending on the notification type, this may assign the user as a co-organizer,
     * add the user to a waiting list for a private event, or open the related event while
     * clearing the action-required flag.</p>
     *
     * @param notification the notification that the user accepted
     */
    @Override
    public void onAccept(AppNotification notification) {
        String userId = session.getUserId();
        if (userId == null) return;

        if (AppNotification.TYPE_CO_ORGANIZER.equals(notification.getType())) {
            eventRepo.addCoOrganizer(notification.getEventId(), userId)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(getContext(), "You are now a co-organizer!", Toast.LENGTH_SHORT).show();
                        notifRepo.deleteNotification(notification.getId()).addOnSuccessListener(v2 -> loadNotifications());
                    });
        } else if (AppNotification.TYPE_INVITATION.equals(notification.getType())) {
            // Entrant accepting private event invitation -> join waiting list
            regRepo.updateStatus(notification.getEventId(), userId, Registration.STATUS_WAITING)
                    .addOnSuccessListener(v -> {
                        eventRepo.incrementWaitingListCount(notification.getEventId(), 1);
                        Toast.makeText(getContext(), "Joined waiting list!", Toast.LENGTH_SHORT).show();
                        notifRepo.deleteNotification(notification.getId()).addOnSuccessListener(v2 -> loadNotifications());
                        
                        // Open event detail
                        Intent i = new Intent(getContext(), EventDetailActivity.class);
                        i.putExtra("event_id", notification.getEventId());
                        startActivity(i);
                    });
        } else if (notification.getEventId() != null) {
            // For lottery wins or other action-required notifications
            Intent i = new Intent(getContext(), EventDetailActivity.class);
            i.putExtra("event_id", notification.getEventId());
            startActivity(i);
            
            notifRepo.markAsRead(notification.getId());
            notifRepo.updateActionRequired(notification.getId(), false).addOnSuccessListener(v -> loadNotifications());
        }
    }
    /**
     * Handles rejection of an actionable notification.
     *
     * @param notification the notification that the user declined
     */
    @Override
    public void onDecline(AppNotification notification) {
        String userId = session.getUserId();
        if (userId == null) return;

        if (AppNotification.TYPE_INVITATION.equals(notification.getType())) {
            regRepo.updateStatus(notification.getEventId(), userId, Registration.STATUS_DECLINED)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                        notifRepo.deleteNotification(notification.getId()).addOnSuccessListener(v2 -> loadNotifications());
                    });
        } else {
            notifRepo.updateActionRequired(notification.getId(), false).addOnSuccessListener(v -> {
                Toast.makeText(getContext(), "Action declined", Toast.LENGTH_SHORT).show();
                loadNotifications();
            });
        }
    }
    /**
     * Permanently deletes a notification and refreshes the displayed list.
     *
     * @param notification the notification selected for deletion
     */
    @Override
    public void onDelete(AppNotification notification) {
        notifRepo.deleteNotification(notification.getId()).addOnSuccessListener(v -> loadNotifications());
    }
    /**
     * Reloads notifications whenever the fragment returns to the foreground.
     */
    @Override public void onResume() { super.onResume(); loadNotifications(); }
}
