package com.example.eventlottery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
/**
 * Fragment that lets entrants discover, search, filter, and scan into public events.
 *
 * <p>Role in application: loads the public event catalogue, applies search and dropdown
 * filters, launches QR scanning, and opens event details for selected results.</p>
 *
 * <p>Outstanding issues: filtering is performed client-side on the currently loaded event
 * list, which is appropriate for the project scope but may require pagination or server-side
 * querying at larger scale.</p>
 */
public class DiscoverEventsFragment extends Fragment {

    private EventRepository eventRepo;
    private EventAdapter adapter;
    private RecyclerView rvEvents;
    private View llEmpty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;
    
    private EditText etSearch;
    private AutoCompleteTextView atvStatus, atvSize;
    private View llFilterOptions;

    private final ActivityResultLauncher<Intent> qrLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    String eventId = result.getData().getStringExtra("event_id");
                    if (eventId != null) openEventDetail(eventId);
                }
            });
    /**
     * Inflates the discover-events layout.
     *
     * @param inflater the layout inflater used to create the fragment view
     * @param container the parent view that the fragment UI will attach to
     * @param savedInstanceState previously saved fragment state, if any
     * @return the inflated discover-events fragment view
     */
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover_events, container, false);
    }
    /**
     * Binds the public event list, filter controls, and QR scanner entry point.
     *
     * @param view the fragment root view
     * @param savedInstanceState previously saved fragment state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventRepo = new EventRepository();

        rvEvents     = view.findViewById(R.id.rv_events);
        llEmpty      = view.findViewById(R.id.ll_empty);
        progress     = view.findViewById(R.id.progress);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        etSearch     = view.findViewById(R.id.et_search);
        
        View cvFilterToggle = view.findViewById(R.id.cv_filter_toggle);
        llFilterOptions     = view.findViewById(R.id.ll_filter_options);
        atvStatus           = view.findViewById(R.id.atv_status);
        atvSize             = view.findViewById(R.id.atv_size);
        
        FloatingActionButton fabScan = view.findViewById(R.id.fab_scan);

        adapter = new EventAdapter(event -> openEventDetail(event.getId()));
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEvents.setAdapter(adapter);

        setupFilters();

        swipeRefresh.setOnRefreshListener(this::loadEvents);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        fabScan.setOnClickListener(v -> qrLauncher.launch(
                new Intent(getContext(), QRScanActivity.class)));

        cvFilterToggle.setOnClickListener(v -> {
            boolean isVisible = llFilterOptions.getVisibility() == View.VISIBLE;
            llFilterOptions.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        loadEvents();
    }
    /**
     * Configures search text and dropdown filters for status and event size.
     */
    private void setupFilters() {
        // Status options
        String[] statuses = {"All Status", "Open Now", "Closed", "Drawn", "Completed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), 
                R.layout.item_dropdown_filter, statuses);
        atvStatus.setAdapter(statusAdapter);
        atvStatus.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        // Size options
        String[] sizes = {"Any Size", "Small (\u226420)", "Medium (21-50)", "Large (50+)"};
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(requireContext(), 
                R.layout.item_dropdown_filter, sizes);
        atvSize.setAdapter(sizeAdapter);
        atvSize.setOnItemClickListener((parent, view, position, id) -> applyFilters());

        // Search text
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    /**
     * Applies the current search query and dropdown selections to the event adapter.
     */
    private void applyFilters() {
        String query = etSearch.getText().toString();
        String status = atvStatus.getText().toString();
        String size = atvSize.getText().toString();
        
        adapter.filter(query, status, size);
        updateEmpty();
    }
    /**
     * Loads all public events, sorts them by creation time, and refreshes the filtered list.
     */
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
            events.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

            adapter.setEvents(events);
            applyFilters();
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
    /**
     * Opens the detail screen for a selected event.
     *
     * @param eventId the identifier of the event to display
     */
    private void openEventDetail(String eventId) {
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("event_id", eventId);
        startActivity(intent);
    }
    /**
     * Updates empty-state visibility based on the filtered adapter contents.
     */
    private void updateEmpty() {
        boolean empty = adapter.isEmpty();
        llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
    /**
     * Reloads public events whenever the fragment becomes active again.
     */
    @Override public void onResume() { super.onResume(); loadEvents(); }
}
