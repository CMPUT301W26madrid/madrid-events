package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.eventlottery.R;
import com.example.eventlottery.activities.EventDetailActivity;
import com.example.eventlottery.activities.QRScanActivity;
import com.example.eventlottery.adapters.EventAdapter;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DiscoverEventsFragment extends Fragment {

    private EventRepository eventRepo;
    private EventAdapter adapter;
    private RecyclerView rvEvents;
    private View llEmpty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;
    private String currentStatusFilter = "all";

    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    String eventId = result.getData().getStringExtra("event_id");
                    if (eventId != null) openEventDetail(eventId);
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventRepo = new EventRepository();

        rvEvents     = view.findViewById(R.id.rv_events);
        llEmpty      = view.findViewById(R.id.ll_empty);
        progress     = view.findViewById(R.id.progress);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        EditText etSearch = view.findViewById(R.id.et_search);
        View ivFilter     = view.findViewById(R.id.iv_filter);
        View llFilters    = view.findViewById(R.id.ll_filters);
        ChipGroup cgStatus = view.findViewById(R.id.cg_status);
        FloatingActionButton fabScan = view.findViewById(R.id.fab_scan);

        adapter = new EventAdapter(event -> openEventDetail(event.getId()));
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(adapter);

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filter(s.toString(), currentStatusFilter);
                updateEmpty();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter toggle
        ivFilter.setOnClickListener(v -> {
            llFilters.setVisibility(llFilters.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // Status chips
        cgStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_open)   currentStatusFilter = "open";
            else if (id == R.id.chip_closed) currentStatusFilter = "closed";
            else if (id == R.id.chip_drawn)  currentStatusFilter = "drawn";
            else currentStatusFilter = "all";
            adapter.filter(etSearch.getText().toString(), currentStatusFilter);
            updateEmpty();
        });

        swipeRefresh.setOnRefreshListener(this::loadEvents);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        fabScan.setOnClickListener(v -> qrLauncher.launch(
                new Intent(getContext(), QRScanActivity.class)));

        loadEvents();
    }

    private void loadEvents() {
        if (!isAdded()) return;
        progress.setVisibility(View.VISIBLE);
        eventRepo.getAllPublicEvents().addOnSuccessListener(qs -> {
            if (!isAdded()) return;
            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Event e = doc.toObject(Event.class);
                if (e != null) {
                    e.setId(doc.getId());
                    events.add(e);
                }
            }
            // Client-side sort by createdAt descending
            events.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

            adapter.setEvents(events);
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            updateEmpty();
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void openEventDetail(String eventId) {
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
    }

    private void updateEmpty() {
        boolean empty = adapter.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override public void onResume() { super.onResume(); loadEvents(); }
}
