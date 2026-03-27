package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.adapters.AdminEventAdapter;
import com.example.eventlottery.adapters.AdminUserAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private SessionManager session;
    private UserRepository userRepo;
    private EventRepository eventRepo;
    private RegistrationRepository regRepo;

    private AdminUserAdapter userAdapter;
    private AdminEventAdapter eventAdapter;

    private TextView tvStatUsers, tvStatEvents, tvStatImages;
    private RecyclerView rvAdmin;
    private View llEmpty;
    private ProgressBar progress;
    private TabLayout tabLayout;
    private EditText etSearch;

    private int currentTab = 0; // 0=users, 1=events, 2=images, 3=logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        session  = new SessionManager(this);
        userRepo = new UserRepository();
        eventRepo = new EventRepository();
        regRepo   = new RegistrationRepository();

        tvStatUsers  = findViewById(R.id.tv_stat_users);
        tvStatEvents = findViewById(R.id.tv_stat_events);
        tvStatImages = findViewById(R.id.tv_stat_images);
        rvAdmin      = findViewById(R.id.rv_admin);
        llEmpty      = findViewById(R.id.ll_empty);
        progress     = findViewById(R.id.progress);
        tabLayout    = findViewById(R.id.tab_layout);
        etSearch     = findViewById(R.id.et_search);

        setupRoleSwitcher();
        setupAdapters();
        setupTabs();
        setupSearch();

        loadStats();
        loadUsers();
    }

    private void setupRoleSwitcher() {
        Spinner spinner = findViewById(R.id.spinner_role);
        userRepo.getUserById(session.getUserId()).addOnSuccessListener(doc -> {
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
                        switch (chosen) {
                            case "organizer":
                                startActivity(new Intent(AdminMainActivity.this, OrganizerMainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                finish(); break;
                            case "entrant":
                                startActivity(new Intent(AdminMainActivity.this, EntrantMainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                finish(); break;
                        }
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> p) {}
            });
        });
    }

    private void setupAdapters() {
        userAdapter = new AdminUserAdapter();
        userAdapter.setDeleteListener(u -> confirmDeleteUser(u));
        eventAdapter = new AdminEventAdapter();
        eventAdapter.setDeleteListener(e -> confirmDeleteEvent(e));

        rvAdmin.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_users));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_events));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_images));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_logs));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                etSearch.setText("");
                switch (currentTab) {
                    case 0: loadUsers();  break;
                    case 1: loadEvents(); break;
                    case 2: showImagesTab(); break;
                    case 3: showLogsTab(); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String q = s.toString().trim();
                if (currentTab == 0) userAdapter.filter(q);
                else if (currentTab == 1) eventAdapter.filter(q);
                updateEmpty();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadStats() {
        userRepo.getAllUsers().addOnSuccessListener(qs -> tvStatUsers.setText(String.valueOf(qs.size())));
        eventRepo.getAllEvents().addOnSuccessListener(qs -> {
            tvStatEvents.setText(String.valueOf(qs.size()));
            long images = 0;
            for (DocumentSnapshot d : qs.getDocuments()) {
                Event e = d.toObject(Event.class);
                if (e != null && e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) images++;
            }
            tvStatImages.setText(String.valueOf(images));
        });
    }

    private void loadUsers() {
        progress.setVisibility(View.VISIBLE);
        rvAdmin.setAdapter(userAdapter);
        userRepo.getAllUsers().addOnSuccessListener(qs -> {
            progress.setVisibility(View.GONE);
            List<User> users = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                User u = doc.toObject(User.class);
                if (u != null) { u.setId(doc.getId()); users.add(u); }
            }
            userAdapter.setUsers(users);
            updateEmpty();
        }).addOnFailureListener(e -> progress.setVisibility(View.GONE));
    }

    private void loadEvents() {
        progress.setVisibility(View.VISIBLE);
        rvAdmin.setAdapter(eventAdapter);
        eventRepo.getAllEvents().addOnSuccessListener(qs -> {
            progress.setVisibility(View.GONE);
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e != null) { e.setId(doc.getId()); events.add(e); }
            }
            eventAdapter.setEvents(events);
            updateEmpty();
        }).addOnFailureListener(e -> progress.setVisibility(View.GONE));
    }

    private void showImagesTab() {
        // Images tab: reuse event adapter filtered to those with posters
        progress.setVisibility(View.VISIBLE);
        rvAdmin.setAdapter(eventAdapter);
        eventRepo.getAllEvents().addOnSuccessListener(qs -> {
            progress.setVisibility(View.GONE);
            List<Event> withImages = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e != null && e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) {
                    e.setId(doc.getId());
                    withImages.add(e);
                }
            }
            eventAdapter.setEvents(withImages);
            updateEmpty();
        });
    }

    private void showLogsTab() {
        // Logs tab: show notification logs
        progress.setVisibility(View.VISIBLE);
        new com.example.eventlottery.repositories.NotificationRepository()
                .getAllNotificationLogs()
                .addOnSuccessListener(qs -> {
                    progress.setVisibility(View.GONE);
                    // Build a simple text list using a basic adapter
                    List<String> logLines = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        com.example.eventlottery.models.AppNotification n =
                                doc.toObject(com.example.eventlottery.models.AppNotification.class);
                        if (n == null) continue;
                        logLines.add(String.format("[%s] %s → %s (%d recipients)",
                                com.example.eventlottery.utils.DateUtils.formatDateTime(n.getCreatedAt()),
                                n.getSenderName() != null ? n.getSenderName() : "System",
                                n.getTitle(),
                                n.getRecipientCount()));
                    }
                    ArrayAdapter<String> logAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, logLines);
                    if (logLines.isEmpty()) {
                        llEmpty.setVisibility(View.VISIBLE);
                        rvAdmin.setVisibility(View.GONE);
                    } else {
                        llEmpty.setVisibility(View.GONE);
                        rvAdmin.setVisibility(View.VISIBLE);
                        // Re-use a simple string adapter via a quick wrapper
                        rvAdmin.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                            @Override public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup p, int t) {
                                android.widget.TextView tv = new android.widget.TextView(AdminMainActivity.this);
                                tv.setPadding(dp(16), dp(12), dp(16), dp(12));
                                tv.setTextColor(0xFF374151);
                                tv.setTextSize(13f);
                                return new RecyclerView.ViewHolder(tv) {};
                            }
                            @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
                                ((android.widget.TextView) h.itemView).setText(logLines.get(pos));
                            }
                            @Override public int getItemCount() { return logLines.size(); }
                        });
                    }
                });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_profile)
                .setMessage(R.string.delete_user_confirm)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    userRepo.deleteUser(user.getId()).addOnSuccessListener(v -> {
                        Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                        loadUsers();
                        loadStats();
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage(R.string.delete_event_confirm)
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    eventRepo.deleteEvent(event.getId()).addOnSuccessListener(v -> {
                        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                        loadEvents();
                        loadStats();
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void updateEmpty() {
        boolean empty = (currentTab == 0 && userAdapter.isEmpty()) ||
                        (currentTab == 1 && eventAdapter.isEmpty());
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvAdmin.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(int val) {
        return Math.round(val * getResources().getDisplayMetrics().density);
    }
}
