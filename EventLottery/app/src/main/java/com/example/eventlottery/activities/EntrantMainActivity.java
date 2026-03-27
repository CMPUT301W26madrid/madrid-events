package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.eventlottery.R;
import com.example.eventlottery.fragments.CalendarFragment;
import com.example.eventlottery.fragments.DiscoverEventsFragment;
import com.example.eventlottery.fragments.MyEventsFragment;
import com.example.eventlottery.fragments.NotificationsFragment;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

public class EntrantMainActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView tvNotifBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_main);

        session      = new SessionManager(this);
        tvNotifBadge = findViewById(R.id.tv_notif_badge);
        View ivBell  = findViewById(R.id.iv_bell);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Default fragment
        loadFragment(new DiscoverEventsFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_events)    return loadFragment(new DiscoverEventsFragment());
            if (id == R.id.nav_my_events) return loadFragment(new MyEventsFragment());
            if (id == R.id.nav_calendar)  return loadFragment(new CalendarFragment());
            if (id == R.id.nav_alerts)    return loadFragment(new NotificationsFragment());
            if (id == R.id.nav_profile)   {
                startActivity(new Intent(this, ProfileActivity.class)); return true;
            }
            return false;
        });

        ivBell.setOnClickListener(v -> {
            bottomNav.setSelectedItemId(R.id.nav_alerts);
        });

        // Handle deep link event open
        String openEventId = getIntent().getStringExtra("open_event_id");
        if (openEventId != null) openEventDetail(openEventId);

        loadUnreadBadge();
    }

    private boolean loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }

    private void openEventDetail(String eventId) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
    }

    private void loadUnreadBadge() {
        String userId = session.getUserId();
        if (userId == null) return;
        new NotificationRepository().getUnreadNotifications(userId)
                .addOnSuccessListener(qs -> {
                    int count = qs.size();
                    if (count > 0) {
                        tvNotifBadge.setVisibility(View.VISIBLE);
                        tvNotifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
                    } else {
                        tvNotifBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadBadge();
    }
}
