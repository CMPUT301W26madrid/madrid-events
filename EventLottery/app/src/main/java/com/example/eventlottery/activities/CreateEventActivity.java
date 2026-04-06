package com.example.eventlottery.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.DateUtils;
import com.example.eventlottery.utils.QRCodeHelper;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Activity for creating and editing events.
 *
 * <p>Role in application: collects event metadata, validates registration and event
 * dates, geocodes the selected location, optionally attaches a poster image, and
 * persists the event through the repository layer.</p>
 *
 * <p>Outstanding issues: location verification depends on geocoder availability and
 * poster handling is still implemented locally rather than through a dedicated media pipeline.</p>
 */
public class CreateEventActivity extends AppCompatActivity {

    private SessionManager session;
    private EventRepository eventRepo;
    private UserRepository userRepo;

    private TextInputLayout tilTitle, tilDescription, tilLocation, tilCapacity;
    private TextInputEditText etTitle, etDescription, etTags;
    private MaterialAutoCompleteTextView etLocation;
    private TextInputEditText etEventStart, etEventEnd, etRegOpen, etRegClose;
    private TextInputEditText etCapacity, etMaxWl, etPrice;
    private TextView tvPosterName;
    private MaterialButton btnUploadPoster;
    private SwitchMaterial swPrivate, swGeolocation;
    private MaterialButton btnSave;

    private String base64Poster = null;
    private String eventId = null;
    private Event existingEvent = null;
    private double selectedLat = 0;
    private double selectedLng = 0;

    private final Calendar calEventStart = Calendar.getInstance();
    private final Calendar calEventEnd   = Calendar.getInstance();
    private final Calendar calRegOpen    = Calendar.getInstance();
    private final Calendar calRegClose   = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ArrayAdapter<String> locationAdapter;
    private boolean isLocationSelectedFromDropdown = false;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processImage(uri);
                }
            });
    /**
     * Initializes the event creation or editing form.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
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
        setupLocationAutocomplete();

        eventId = getIntent().getStringExtra("event_id");
        if (eventId != null) {
            setTitle("Edit Event");
            btnSave.setText("Save Changes");
            loadExistingEvent();
        } else {
            setTitle("Create Event");
            btnSave.setText("Create Event");
        }

        btnUploadPoster.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> validateAndSave());
    }
    /**
     * Loads an existing event when the screen is opened in edit mode.
     */
    private void loadExistingEvent() {
        eventRepo.getEventById(eventId).addOnSuccessListener(doc -> {
            existingEvent = doc.toObject(Event.class);
            if (existingEvent != null) {
                existingEvent.setId(doc.getId());
                populateFields();
            }
        });
    }
    /**
     * Populates the form fields with the currently loaded event values.
     */
    private void populateFields() {
        etTitle.setText(existingEvent.getTitle());
        etDescription.setText(existingEvent.getDescription());
        etLocation.setText(existingEvent.getLocation(), false);
        selectedLat = existingEvent.getLatitude();
        selectedLng = existingEvent.getLongitude();
        isLocationSelectedFromDropdown = true; 
        if (existingEvent.getTags() != null) {
            etTags.setText(TextUtils.join(", ", existingEvent.getTags()));
        }
        
        calEventStart.setTimeInMillis(existingEvent.getEventStartDate());
        etEventStart.setText(sdf.format(calEventStart.getTime()));
        
        calEventEnd.setTimeInMillis(existingEvent.getEventEndDate());
        etEventEnd.setText(sdf.format(calEventEnd.getTime()));
        
        calRegOpen.setTimeInMillis(existingEvent.getRegistrationOpenDate());
        etRegOpen.setText(sdf.format(calRegOpen.getTime()));
        
        calRegClose.setTimeInMillis(existingEvent.getRegistrationCloseDate());
        etRegClose.setText(sdf.format(calRegClose.getTime()));
        
        etCapacity.setText(String.valueOf(existingEvent.getCapacity()));
        etMaxWl.setText(String.valueOf(existingEvent.getMaxWaitingList()));
        etPrice.setText(String.valueOf(existingEvent.getPrice()));
        
        swPrivate.setChecked(existingEvent.isPrivate());
        swGeolocation.setChecked(existingEvent.isRequireGeolocation());
        
        if (existingEvent.getPosterUrl() != null && !existingEvent.getPosterUrl().isEmpty()) {
            base64Poster = existingEvent.getPosterUrl();
            tvPosterName.setText("Current poster kept");
        }
    }
    /**
     * Reads, scales, and encodes a selected poster image for storage.
     *
     * @param uri content URI of the selected image
     */
    private void processImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            base64Poster = Base64.encodeToString(bytes, Base64.DEFAULT);
            tvPosterName.setText("New image selected");
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Binds all form views from the layout to local activity fields.
     */
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
        
        btnUploadPoster = findViewById(R.id.btn_upload_poster);
        tvPosterName    = findViewById(R.id.tv_poster_name);
        
        swPrivate     = findViewById(R.id.sw_private);
        swGeolocation = findViewById(R.id.sw_geolocation);
        btnSave       = findViewById(R.id.btn_create_event);
    }
    /**
     * Attaches date-time picker dialogs to the event and registration fields.
     */
    private void setupDatePickers() {
        etEventStart.setOnClickListener(v -> {
            long max = getText(etEventEnd).isEmpty() ? 0 : calEventEnd.getTimeInMillis() - 60000;
            pickDateTime(calEventStart, etEventStart, 0, max);
        });
        etEventEnd.setOnClickListener(v -> {
            long min = getText(etEventStart).isEmpty() ? 0 : calEventStart.getTimeInMillis() + 60000;
            pickDateTime(calEventEnd, etEventEnd, min, 0);
        });
        etRegOpen.setOnClickListener(v -> {
            long max = getText(etEventStart).isEmpty() ? 0 : calEventStart.getTimeInMillis() - 60000;
            pickDateTime(calRegOpen, etRegOpen, 0, max);
        });
        etRegClose.setOnClickListener(v -> {
            long min = getText(etRegOpen).isEmpty() ? 0 : calRegOpen.getTimeInMillis() + 60000;
            long max = getText(etEventEnd).isEmpty() ? 0 : calEventEnd.getTimeInMillis() - 60000;
            pickDateTime(calRegClose, etRegClose, min, max);
        });
    }
    /**
     * Configures location autocomplete and geocoder-backed suggestion handling.
     */
    private void setupLocationAutocomplete() {
        locationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        if (isLocationSelectedFromDropdown) return results;

                        if (constraint != null && constraint.length() >= 2) {
                            List<String> suggestions = fetchGeocoderSuggestions(constraint.toString());
                            results.values = suggestions;
                            results.count = suggestions.size();
                        }
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results != null && results.count > 0) {
                            addAll((List<String>) results.values);
                            notifyDataSetChanged();
                        } else {
                            notifyDataSetInvalidated();
                        }
                    }
                };
            }
        };
        etLocation.setAdapter(locationAdapter);
        etLocation.setThreshold(1);

        etLocation.setOnItemClickListener((parent, view, position, id) -> {
            isLocationSelectedFromDropdown = true;
            String selected = locationAdapter.getItem(position);
            etLocation.setText(selected, false); // false prevents re-triggering the filter
            tilLocation.setError(null);
            verifyAndSetLatLng(selected);
        });

        etLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!etLocation.isPerformingCompletion()) {
                    isLocationSelectedFromDropdown = false;
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    /**
     * Fetches geocoder suggestions for the supplied location query.
     *
     * @param query partial user-entered location text
     * @return a list of formatted address suggestions
     */
    private List<String> fetchGeocoderSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        if (!Geocoder.isPresent()) return suggestions;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            if (addresses != null) {
                for (Address addr : addresses) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(addr.getAddressLine(i));
                    }
                    suggestions.add(sb.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return suggestions;
    }
    /**
     * Resolves the selected location text into latitude and longitude coordinates.
     *
     * @param locName selected location string
     */
    private void verifyAndSetLatLng(String locName) {
        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(locName, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    selectedLat = addresses.get(0).getLatitude();
                    selectedLng = addresses.get(0).getLongitude();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * Opens a date picker followed by a time picker and writes the result to the target field.
     *
     * @param cal calendar instance to update
     * @param target text field that should display the chosen date and time
     * @param minMillis optional minimum allowed timestamp, or {@code 0} for none
     * @param maxMillis optional maximum allowed timestamp, or {@code 0} for none
     */
    private void pickDateTime(Calendar cal, TextInputEditText target, long minMillis, long maxMillis) {
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    new TimePickerDialog(this,
                            (tp, hour, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, minute);
                                if (minMillis > 0 && cal.getTimeInMillis() < minMillis) {
                                    cal.setTimeInMillis(minMillis);
                                    Toast.makeText(this, "Time adjusted to satisfy minimum requirements", Toast.LENGTH_SHORT).show();
                                } else if (maxMillis > 0 && cal.getTimeInMillis() > maxMillis) {
                                    cal.setTimeInMillis(maxMillis);
                                    Toast.makeText(this, "Time adjusted to satisfy maximum requirements", Toast.LENGTH_SHORT).show();
                                }
                                target.setText(sdf.format(cal.getTime()));
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE), false).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        if (minMillis > 0) dpd.getDatePicker().setMinDate(minMillis);
        if (maxMillis > 0 && maxMillis >= minMillis) dpd.getDatePicker().setMaxDate(maxMillis);
        dpd.show();
    }
    /**
     * Validates the form contents and dispatches either a create or update request.
     */
    private void validateAndSave() {
        String title = getText(etTitle);
        String desc  = getText(etDescription);
        String loc   = etLocation.getText().toString().trim();
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

        // Final Geocoder Check to ensure address is real and get Lat/Lng
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(loc, 1);
            if (addresses == null || addresses.isEmpty()) {
                tilLocation.setError("Invalid address. Please enter a real-world location.");
                return;
            } else {
                selectedLat = addresses.get(0).getLatitude();
                selectedLng = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            if (!isLocationSelectedFromDropdown && selectedLat == 0) {
                Toast.makeText(this, "Cannot verify location. Please pick from the dropdown suggestions.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        int capacity   = safeInt(cap, 10);
        int maxWl      = safeInt(getText(etMaxWl), 0);
        double price   = safeDouble(getText(etPrice), 0);
        boolean priv   = swPrivate.isChecked();
        boolean geo    = swGeolocation.isChecked();

        String currentUserId = session.getUserId();
        if (currentUserId == null) {
            Toast.makeText(this, "Session error — please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving…");

        if (eventId != null) {
            updateEvent(title, desc, loc, capacity, maxWl, price, priv, geo);
        } else {
            createNewEvent(title, desc, loc, currentUserId, capacity, maxWl, price, priv, geo);
        }
    }
    /**
     * Applies the edited form values to the existing event and saves the update.
     *
     * @param title event title
     * @param desc event description
     * @param loc event location string
     * @param capacity event capacity
     * @param maxWl maximum waiting-list size
     * @param price event price
     * @param priv whether the event is private
     * @param geo whether geolocation is required to join
     */
    private void updateEvent(String title, String desc, String loc, int capacity, int maxWl, double price, boolean priv, boolean geo) {
        existingEvent.setTitle(title);
        existingEvent.setDescription(desc);
        existingEvent.setLocation(loc);
        existingEvent.setLatitude(selectedLat);
        existingEvent.setLongitude(selectedLng);
        existingEvent.setEventStartDate(calEventStart.getTimeInMillis());
        existingEvent.setEventEndDate(calEventEnd.getTimeInMillis());
        existingEvent.setRegistrationOpenDate(calRegOpen.getTimeInMillis());
        existingEvent.setRegistrationCloseDate(calRegClose.getTimeInMillis());
        existingEvent.setCapacity(capacity);
        existingEvent.setMaxWaitingList(maxWl);
        existingEvent.setPrice(price);
        existingEvent.setPrivate(priv);
        existingEvent.setRequireGeolocation(geo);
        existingEvent.setPosterUrl(base64Poster);

        String tagsStr = getText(etTags);
        if (!tagsStr.isEmpty()) {
            existingEvent.setTags(Arrays.asList(tagsStr.split(",\\s*")));
        } else {
            existingEvent.setTags(Arrays.asList());
        }

        eventRepo.updateEvent(existingEvent).addOnSuccessListener(v -> {
            Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        });
    }
    /**
     * Builds and persists a new event from the validated form data.
     *
     * @param title event title
     * @param desc event description
     * @param loc event location string
     * @param currentUserId organizer creating the event
     * @param capacity event capacity
     * @param maxWl maximum waiting-list size
     * @param price event price
     * @param priv whether the event is private
     * @param geo whether geolocation is required to join
     */
    private void createNewEvent(String title, String desc, String loc, String currentUserId, int capacity, int maxWl, double price, boolean priv, boolean geo) {
        userRepo.getUserById(currentUserId).addOnSuccessListener(doc -> {
            com.example.eventlottery.models.User user =
                    doc.toObject(com.example.eventlottery.models.User.class);
            String orgName = user != null ? user.getName() : "Organizer";

            Event event = new Event(title, desc, loc,
                    currentUserId, orgName,
                    calEventStart.getTimeInMillis(), calEventEnd.getTimeInMillis(),
                    calRegOpen.getTimeInMillis(), calRegClose.getTimeInMillis(),
                    capacity, maxWl, price,
                    base64Poster,
                    priv, geo);
            
            event.setLatitude(selectedLat);
            event.setLongitude(selectedLng);

            String tagsStr = getText(etTags);
            if (!tagsStr.isEmpty()) {
                event.setTags(Arrays.asList(tagsStr.split(",\\s*")));
            }

            eventRepo.createEvent(event).addOnSuccessListener(ref -> {
                String newEventId = ref.getId();
                String deepLink = QRCodeHelper.buildEventDeepLink(newEventId);
                eventRepo.setQrCodeContent(newEventId, deepLink)
                        .addOnSuccessListener(v -> {
                            Toast.makeText(this, R.string.event_created_success, Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }).addOnFailureListener(e -> {
                btnSave.setEnabled(true);
                btnSave.setText(R.string.create_event);
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            });
        });
    }
    /**
     * Returns trimmed text from a text input field.
     *
     * @param et source input field
     * @return trimmed text, or an empty string when no value is present
     */
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
    /**
     * Parses an integer value while falling back to a default on failure.
     *
     * @param s raw string value
     * @param def fallback value
     * @return parsed integer or the fallback value
     */
    private int safeInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    /**
     * Parses a decimal value while falling back to a default on failure.
     *
     * @param s raw string value
     * @param def fallback value
     * @return parsed decimal or the fallback value
     */
    private double safeDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
