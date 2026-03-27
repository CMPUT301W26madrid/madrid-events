package com.example.eventlottery.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.QRCodeHelper;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {

    private SessionManager session;
    private EventRepository eventRepo;
    private UserRepository userRepo;

    private TextInputLayout tilTitle, tilDescription, tilLocation, tilCapacity;
    private TextInputEditText etTitle, etDescription, etLocation, etTags;
    private TextInputEditText etEventStart, etEventEnd, etRegOpen, etRegClose;
    private TextInputEditText etCapacity, etMaxWl, etPrice, etPoster;
    private SwitchMaterial swPrivate, swGeolocation;
    private MaterialButton btnCreate;

    private final Calendar calEventStart = Calendar.getInstance();
    private final Calendar calEventEnd   = Calendar.getInstance();
    private final Calendar calRegOpen    = Calendar.getInstance();
    private final Calendar calRegClose   = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        session   = new SessionManager(this);
        eventRepo = new EventRepository();
        userRepo  = new UserRepository();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        bindViews();
        setupDatePickers();

        btnCreate.setOnClickListener(v -> validateAndCreate());
    }

    private void bindViews() {
        tilTitle       = findViewById(R.id.til_title);
        tilDescription = findViewById(R.id.til_description);
        tilLocation    = findViewById(R.id.til_location);
        tilCapacity    = findViewById(R.id.til_capacity);

        etTitle       = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etLocation    = findViewById(R.id.et_location);
        etTags        = findViewById(R.id.et_tags);
        etEventStart  = findViewById(R.id.et_event_start);
        etEventEnd    = findViewById(R.id.et_event_end);
        etRegOpen     = findViewById(R.id.et_reg_open);
        etRegClose    = findViewById(R.id.et_reg_close);
        etCapacity    = findViewById(R.id.et_capacity);
        etMaxWl       = findViewById(R.id.et_max_wl);
        etPrice       = findViewById(R.id.et_price);
        etPoster      = findViewById(R.id.et_poster);
        swPrivate     = findViewById(R.id.sw_private);
        swGeolocation = findViewById(R.id.sw_geolocation);
        btnCreate     = findViewById(R.id.btn_create_event);
    }

    private void setupDatePickers() {
        etEventStart.setOnClickListener(v -> pickDateTime(calEventStart, etEventStart));
        etEventEnd.setOnClickListener(v -> pickDateTime(calEventEnd, etEventEnd));
        etRegOpen.setOnClickListener(v -> pickDateTime(calRegOpen, etRegOpen));
        etRegClose.setOnClickListener(v -> pickDateTime(calRegClose, etRegClose));
    }

    private void pickDateTime(Calendar cal, TextInputEditText target) {
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    new TimePickerDialog(this,
                            (tp, hour, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, minute);
                                target.setText(sdf.format(cal.getTime()));
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE), false).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void validateAndCreate() {
        String title = getText(etTitle);
        String desc  = getText(etDescription);
        String loc   = getText(etLocation);
        String cap   = getText(etCapacity);

        boolean valid = true;
        if (TextUtils.isEmpty(title)) { tilTitle.setError(getString(R.string.error_title_required)); valid = false; }
        else tilTitle.setError(null);
        if (TextUtils.isEmpty(loc))   { tilLocation.setError(getString(R.string.error_location_required)); valid = false; }
        else tilLocation.setError(null);
        if (TextUtils.isEmpty(cap))   { tilCapacity.setError(getString(R.string.error_capacity_required)); valid = false; }
        else tilCapacity.setError(null);
        if (getText(etEventStart).isEmpty() || getText(etEventEnd).isEmpty() ||
            getText(etRegOpen).isEmpty()    || getText(etRegClose).isEmpty()) {
            Toast.makeText(this, R.string.error_dates_required, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!valid) return;

        int capacity   = safeInt(cap, 10);
        int maxWl      = safeInt(getText(etMaxWl), 0);
        double price   = safeDouble(getText(etPrice), 0);
        String poster  = getText(etPoster);
        boolean priv   = swPrivate.isChecked();
        boolean geo    = swGeolocation.isChecked();

        String currentUserId = session.getUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Session error — please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreate.setEnabled(false);
        btnCreate.setText("Creating…");

        // Fetch organizer name
        userRepo.getUserById(currentUserId).addOnSuccessListener(doc -> {
            com.example.eventlottery.models.User user =
                    doc.toObject(com.example.eventlottery.models.User.class);
            String orgName = user != null ? user.getName() : "Organizer";

            Event event = new Event(title, desc, loc,
                    currentUserId, orgName,
                    calEventStart.getTimeInMillis(), calEventEnd.getTimeInMillis(),
                    calRegOpen.getTimeInMillis(), calRegClose.getTimeInMillis(),
                    capacity, maxWl, price,
                    poster.isEmpty() ? null : poster,
                    priv, geo);

            // Handle tags
            String tagsStr = getText(etTags);
            if (!tagsStr.isEmpty()) {
                event.setTags(Arrays.asList(tagsStr.split(",\\s*")));
            }

            eventRepo.createEvent(event).addOnSuccessListener(ref -> {
                String eventId = ref.getId();
                String deepLink = QRCodeHelper.buildEventDeepLink(eventId);
                eventRepo.setQrCodeContent(eventId, deepLink)
                        .addOnSuccessListener(v -> {
                            Toast.makeText(this, R.string.event_created_success, Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }).addOnFailureListener(e -> {
                btnCreate.setEnabled(true);
                btnCreate.setText(R.string.create_event);
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private double safeDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
