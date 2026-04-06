package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.eventlottery.R;
import com.example.eventlottery.fragments.AdminDashboardFragment;
import com.example.eventlottery.fragments.DiscoverEventsFragment;
import com.example.eventlottery.fragments.NotificationsFragment;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
/**
 * Main entry activity for administrators.
 *
 * <p>Role in application: hosts admin-facing navigation for moderation tasks,
 * event browsing, notifications, and profile access, while also supporting
 * role switching for users who hold multiple roles.</p>
 *
 * <p>Outstanding issues: unread notification counts are refreshed on resume but
 * are not subscribed to live updates, so the badge may lag behind real-time changes.</p>
 */
public class AdminMainActivity extends AppCompatActivity {

    private SessionManager session;
    private UserRepository userRepo;
    private TextView tvNotifBadge;
    /**
     * Initializes the admin home screen and configures navigation.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        session  = new SessionManager(this);
        userRepo = new UserRepository();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        tvNotifBadge = findViewById(R.id.tv_notif_badge); // May be null if not in layout yet, let's check activity_admin_main
        View ivBell = findViewById(R.id.iv_bell);

        setupRoleSwitcher();

        // Default fragment
        loadFragment(new AdminDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_admin)   return loadFragment(new AdminDashboardFragment());
            if (id == R.id.nav_events)  return loadFragment(new DiscoverEventsFragment());
            if (id == R.id.nav_alerts)  return loadFragment(new NotificationsFragment());
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        if (ivBell != null) {
            ivBell.setOnClickListener(v -> bottomNav.setSelectedItemId(R.id.nav_alerts));
        }

        loadUnreadBadge();
    }


    /**
     * Loads the current user's roles and configures the role-switch spinner.
     */
    private void setupRoleSwitcher() {
        Spinner spinner = findViewById(R.id.spinner_role);
        String userId = session.getUserId();
        if (userId == null) return;

        userRepo.getUserById(userId).addOnSuccessListener(doc -> {
            User user = doc.toObject(User.class);
            if (user == null) return;
            List<String> roles = user.getRoles();
            List<String> display = new ArrayList<>();
            for (String r : roles) display.add(capitalize(r));

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, display);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            int idx = roles.indexOf("admin");
            if (idx >= 0) spinner.setSelection(idx);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                    String chosen = roles.get(pos);
                    if (!chosen.equals(session.getActiveRole())) {
                        session.saveActiveRole(chosen);
                        switchRole(chosen);
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> p) {}
            });
        });
    }
    /**
     * Switches from the admin shell to another role-specific main activity.
     *
     * @param role target role selected by the user
     */
    private void switchRole(String role) {
        Intent intent;
        switch (role) {
            case "organizer":
                intent = new Intent(this, OrganizerMainActivity.class); break;
            case "entrant":
                intent = new Intent(this, EntrantMainActivity.class); break;
            default:
                return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Replaces the main container with the supplied fragment.
     *
     * @param fragment fragment to display
     * @return {@code true} after the fragment transaction is requested
     */
    private boolean loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }
    /**
     * Loads the unread notification count and updates the badge view.
     */
    private void loadUnreadBadge() {
        String userId = session.getUserId();
        if (userId == null || tvNotifBadge == null) return;
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
    /**
     * Refreshes the unread notification badge when the activity returns to the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadBadge();
    }

    /**
     * Capitalizes the first character of a role label for display purposes.
     *
     * @param s raw role string
     * @return capitalized role label, or the original value when empty
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
