package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.adapters.NotificationAdapter;
import com.example.eventlottery.models.AppNotification;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private SessionManager session;
    private NotificationRepository notifRepo;
    private NotificationAdapter adapter;
    private View llEmpty;
    private ProgressBar progress;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session   = new SessionManager(requireContext());
        notifRepo = new NotificationRepository();

        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        llEmpty  = view.findViewById(R.id.ll_empty);
        progress = view.findViewById(R.id.progress);
        TextView tvMarkRead = view.findViewById(R.id.tv_mark_read);

        adapter = new NotificationAdapter(notif -> {
            // Mark as read then open event
            notifRepo.markAsRead(notif.getId());
            if (notif.getEventId() != null && !notif.getEventId().isEmpty()) {
                Intent i = new Intent(getContext(), EventDetailActivity.class);
                i.putExtra("event_id", notif.getEventId());
                startActivity(i);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        tvMarkRead.setOnClickListener(v -> markAllRead());
        loadNotifications();
    }

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
            progress.setVisibility(View.GONE);
            adapter.setNotifications(list);
            llEmpty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            if (isAdded()) progress.setVisibility(View.GONE);
        });
    }

    private void markAllRead() {
        String userId = session.getUserId();
        if (userId == null) return;
        // Use the fixed markAllReadForUser() that never returns null
        notifRepo.markAllReadForUser(userId)
                .addOnSuccessListener(v -> loadNotifications())
                .addOnFailureListener(e -> loadNotifications());
    }

    @Override public void onResume() { super.onResume(); loadNotifications(); }
}
