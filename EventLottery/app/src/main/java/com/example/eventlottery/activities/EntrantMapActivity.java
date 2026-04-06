package com.example.eventlottery.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
/**
 * Map activity for viewing event and entrant join locations.
 *
 * <p>Role in application: displays the event location together with geolocation-verified
 * entrant markers so organizers can review where registrations were submitted from.</p>
 *
 * <p>Outstanding issues: older events without saved coordinates only fall back to a
 * limited participant-only view instead of a full location recovery workflow.</p>
 */
public class EntrantMapActivity extends AppCompatActivity {

    private String eventId;
    private RegistrationRepository regRepo;
    private EventRepository eventRepo;
    private MapView map;
    private MyLocationNewOverlay myLocationOverlay;
    private boolean hasZoomedToTarget = false;
    /**
     * Initializes the map screen and loads map markers for the selected event.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_map_view);

        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) { finish(); return; }

        regRepo = new RegistrationRepository();
        eventRepo = new EventRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(4.0);

        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.myLocationOverlay.enableMyLocation();
        
        // Only zoom to user location if we don't have an event/entrant target
        this.myLocationOverlay.runOnFirstFix(() -> {
            runOnUiThread(() -> {
                if (!hasZoomedToTarget && myLocationOverlay.getMyLocation() != null) {
                    map.getController().animateTo(myLocationOverlay.getMyLocation());
                    map.getController().setZoom(15.0);
                    hasZoomedToTarget = true;
                }
            });
        });
        
        map.getOverlays().add(this.myLocationOverlay);

        FloatingActionButton fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> {
            GeoPoint myLocation = myLocationOverlay.getMyLocation();
            if (myLocation != null) {
                map.getController().animateTo(myLocation);
                map.getController().setZoom(17.0);
            } else {
                Toast.makeText(this, "Determining your location...", Toast.LENGTH_SHORT).show();
            }
        });

        loadEventAndMarkers();
    }
    /**
     * Loads the event itself, places the event marker, and then loads entrant markers.
     */
    private void loadEventAndMarkers() {
        eventRepo.getEventById(eventId).addOnSuccessListener(doc -> {
            Event event = doc.toObject(Event.class);
            if (event != null && event.getLatitude() != 0) {
                GeoPoint eventPos = new GeoPoint(event.getLatitude(), event.getLongitude());
                
                Marker eventMarker = new Marker(map);
                eventMarker.setPosition(eventPos);
                eventMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                eventMarker.setTitle("📍 EVENT: " + event.getTitle());
                eventMarker.setSnippet(event.getLocation());
                
                // Stand out with a different marker if available, otherwise default
                map.getOverlays().add(eventMarker);

                // Take map there and zoom in
                map.getController().animateTo(eventPos);
                map.getController().setZoom(18.5); 
                hasZoomedToTarget = true;
                
                // Show the info window (the "drop down") immediately
                eventMarker.showInfoWindow();
            } else {
                // If this is an old event without coordinates, try to geocode the string as fallback
                Toast.makeText(this, "Event coordinates missing. Showing participants...", Toast.LENGTH_SHORT).show();
            }
            
            loadEntrantLocations();
        }).addOnFailureListener(e -> loadEntrantLocations());
    }
    /**
     * Loads geolocation-verified registrations and adds entrant markers to the map.
     */
    private void loadEntrantLocations() {
        regRepo.getRegistrationsForEvent(eventId).addOnSuccessListener(qs -> {
            List<GeoPoint> points = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                Registration reg = doc.toObject(Registration.class);
                if (reg != null && reg.isGeoVerified()) {
                    GeoPoint pos = new GeoPoint(reg.getLatitude(), reg.getLongitude());
                    Marker marker = new Marker(map);
                    marker.setPosition(pos);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle(reg.getUserName());
                    marker.setSnippet("Entrant Status: " + reg.getStatus());
                    map.getOverlays().add(marker);
                    points.add(pos);
                }
            }
            
            // If no event location was found but we have entrants, show them
            if (!hasZoomedToTarget && !points.isEmpty()) {
                if (points.size() == 1) {
                    map.getController().animateTo(points.get(0));
                    map.getController().setZoom(15.0);
                } else {
                    BoundingBox bbox = BoundingBox.fromGeoPoints(points);
                    map.zoomToBoundingBox(bbox, true, 100);
                }
                hasZoomedToTarget = true;
            }

            map.invalidate();
        });
    }
    /**
     * Resumes the map and re-enables the current-location overlay when visible.
     */
    @Override public void onResume() { super.onResume(); map.onResume(); if (myLocationOverlay != null) myLocationOverlay.enableMyLocation(); }
    /**
     * Pauses the map and disables live location updates while the activity is not visible.
     */
    @Override public void onPause() { super.onPause(); map.onPause(); if (myLocationOverlay != null) myLocationOverlay.disableMyLocation(); }
}
