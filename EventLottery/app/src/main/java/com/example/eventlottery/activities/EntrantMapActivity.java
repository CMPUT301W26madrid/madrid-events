package com.example.eventlottery.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class EntrantMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String eventId;
    private RegistrationRepository regRepo;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) {
            finish();
            return;
        }

        regRepo = new RegistrationRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadEntrantLocations();
    }

    private void loadEntrantLocations() {
        regRepo.getRegistrationsForEvent(eventId).addOnSuccessListener(qs -> {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasMarkers = false;

            for (DocumentSnapshot doc : qs.getDocuments()) {
                Registration reg = doc.toObject(Registration.class);
                if (reg != null && reg.isGeoVerified()) {
                    LatLng pos = new LatLng(reg.getLatitude(), reg.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(reg.getUserName())
                            .snippet(reg.getStatus()));
                    builder.include(pos);
                    hasMarkers = true;
                }
            }

            if (hasMarkers) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } else {
                Toast.makeText(this, "No geolocation data available for entrants", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading locations", Toast.LENGTH_SHORT).show();
        });
    }
}
