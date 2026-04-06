package com.example.eventlottery.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.adapters.CommentAdapter;
import com.example.eventlottery.models.AppNotification;
import com.example.eventlottery.models.Comment;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.repositories.CommentRepository;
import com.example.eventlottery.repositories.EventRepository;
import com.example.eventlottery.repositories.NotificationRepository;
import com.example.eventlottery.repositories.RegistrationRepository;
import com.example.eventlottery.utils.DateUtils;
import com.example.eventlottery.utils.QRCodeHelper;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
/**
 * Activity that presents the full details for a single event.
 *
 * <p>Role in application: shows event metadata, waitlist state, QR access, comments,
 * map preview, and entrant actions such as joining, leaving, accepting, or declining.</p>
 *
 * <p>Outstanding issues: this screen coordinates several responsibilities directly in
 * the activity and could be refactored further to reduce UI-data coupling.</p>
 */
public class EventDetailActivity extends AppCompatActivity {

    private static final int LOCATION_PERM_CODE = 101;

    private SessionManager session;
    private EventRepository eventRepo;
    private RegistrationRepository regRepo;
    private CommentRepository commentRepo;
    private NotificationRepository notifRepo;
    private FusedLocationProviderClient locationClient;

    private Event currentEvent;
    private Registration currentRegistration;
    private String userId;
    private boolean pendingJoin = false;
    private boolean isCoOrganizer = false;

    // Views
    private TextView tvTitle, tvOrganizer, tvStartDate, tvStartTime, tvEndDate, tvEndTime, tvLocation,
            tvPrice, tvWaitingCount, tvSpots, tvRegPeriod, tvDescription;
    private ImageView ivPoster, ivQrBtn;
    private View cardSelected, cardMap;
    private MapView mapView;
    private MaterialButton btnJoinLeave, btnAccept, btnDecline;
    private RecyclerView rvComments;
    private TextInputEditText etComment;
    private CommentAdapter commentAdapter;
    /**
     * Initializes the event detail screen and starts loading the requested event.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        
        setContentView(R.layout.activity_event_detail);

        session      = new SessionManager(this);
        userId       = session.getUserId();
        eventRepo    = new EventRepository();
        regRepo      = new RegistrationRepository();
        commentRepo  = new CommentRepository();
        notifRepo    = new NotificationRepository();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        bindViews();

        String eventId = getIntent().getStringExtra("event_id");
        if (eventId == null) { finish(); return; }

        loadEvent(eventId);
    }
    /**
     * Binds layout views, initializes adapters, and attaches click listeners.
     */
    private void bindViews() {
        tvTitle        = findViewById(R.id.tv_title);
        tvOrganizer    = findViewById(R.id.tv_organizer);
        tvStartDate    = findViewById(R.id.tv_start_date);
        tvStartTime    = findViewById(R.id.tv_start_time);
        tvEndDate      = findViewById(R.id.tv_end_date);
        tvEndTime      = findViewById(R.id.tv_end_time);
        tvLocation     = findViewById(R.id.tv_location);
        tvPrice        = findViewById(R.id.tv_price);
        tvWaitingCount = findViewById(R.id.tv_waiting_count);
        tvSpots        = findViewById(R.id.tv_spots);
        tvRegPeriod    = findViewById(R.id.tv_reg_period);
        tvDescription  = findViewById(R.id.tv_description);
        ivPoster       = findViewById(R.id.iv_poster);
        ivQrBtn        = findViewById(R.id.iv_qr_btn);
        cardSelected   = findViewById(R.id.card_selected);
        cardMap        = findViewById(R.id.card_map);
        mapView        = findViewById(R.id.map_view);
        btnJoinLeave   = findViewById(R.id.btn_join_leave);
        btnAccept      = findViewById(R.id.btn_accept);
        btnDecline     = findViewById(R.id.btn_decline);
        rvComments     = findViewById(R.id.rv_comments);
        etComment      = findViewById(R.id.et_comment);

        // Map setup
        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(false); // Static-ish preview
        }

        // Initially placeholder adapter, re-initialized when event loads
        commentAdapter = new CommentAdapter(userId, false);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        btnJoinLeave.setOnClickListener(v -> handleJoinLeave());
        btnAccept.setOnClickListener(v -> handleAccept());
        btnDecline.setOnClickListener(v -> handleDecline());
        findViewById(R.id.btn_post_comment).setOnClickListener(v -> postComment());
        ivQrBtn.setOnClickListener(v -> showQrDialog());
    }
    /**
     * Loads the target event and configures moderator privileges for the current user.
     *
     * @param eventId identifier of the event to display
     */
    private void loadEvent(String eventId) {
        eventRepo.getEventById(eventId).addOnSuccessListener(doc -> {
            currentEvent = doc.toObject(Event.class);
            if (currentEvent == null) { finish(); return; }
            currentEvent.setId(doc.getId());
            
            // Determine if current user is the organizer, a co-organizer, or an admin
            boolean isOrganizer = userId != null && userId.equals(currentEvent.getOrganizerId());
            isCoOrganizer = userId != null && currentEvent.getCoOrganizerIds() != null 
                    && currentEvent.getCoOrganizerIds().contains(userId);
            boolean isAdmin = "admin".equals(session.getActiveRole());
            
            // Re-initialize adapter with correct moderator privilege (Organizer, Co-Organizer, or Admin)
            commentAdapter = new CommentAdapter(userId, isOrganizer || isCoOrganizer || isAdmin);
            commentAdapter.setDeleteListener(c -> confirmDeleteComment(c));
            commentAdapter.setEditListener(c -> showEditCommentDialog(c));
            rvComments.setAdapter(commentAdapter);

            populateUI();
            loadRegistration();
            loadComments();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    /**
     * Populates the detail UI with the currently loaded event data.
     */
    private void populateUI() {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(currentEvent.getTitle());
        tvTitle.setText(currentEvent.getTitle());
        tvOrganizer.setText(getString(R.string.organized_by, currentEvent.getOrganizerName()));
        
        tvStartDate.setText(DateUtils.formatDate(currentEvent.getEventStartDate()));
        tvStartTime.setText(DateUtils.formatTime(currentEvent.getEventStartDate()));
        tvEndDate.setText(DateUtils.formatDate(currentEvent.getEventEndDate()));
        tvEndTime.setText(DateUtils.formatTime(currentEvent.getEventEndDate()));
        
        tvLocation.setText(currentEvent.getLocation());
        tvPrice.setText(currentEvent.getFormattedPrice());
        tvWaitingCount.setText(currentEvent.getWaitingListCount() + " on waiting list");
        tvSpots.setText(currentEvent.getCapacity() + " spots");
        tvRegPeriod.setText(DateUtils.formatDateShort(currentEvent.getRegistrationOpenDate())
                + " — " + DateUtils.formatDateShort(currentEvent.getRegistrationCloseDate()));
        tvDescription.setText(currentEvent.getDescription());

        if (currentEvent.getPosterUrl() != null && !currentEvent.getPosterUrl().isEmpty()) {
            ivPoster.setVisibility(View.VISIBLE);
            String posterStr = currentEvent.getPosterUrl();
            if (posterStr.startsWith("http")) {
                Glide.with(this).load(posterStr).into(ivPoster);
            } else {
                try {
                    byte[] decodedString = Base64.decode(posterStr, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivPoster.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    ivPoster.setVisibility(View.GONE);
                }
            }
        }

        if (currentEvent.isPrivate()) {
            ivQrBtn.setVisibility(View.GONE);
        } else {
            ivQrBtn.setVisibility(View.VISIBLE);
        }

        // Setup Map Preview
        if (currentEvent.getLatitude() != 0 || currentEvent.getLongitude() != 0) {
            cardMap.setVisibility(View.VISIBLE);
            GeoPoint startPoint = new GeoPoint(currentEvent.getLatitude(), currentEvent.getLongitude());
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(startPoint);

            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle(currentEvent.getLocation());
            mapView.getOverlays().clear();
            mapView.getOverlays().add(startMarker);
            mapView.invalidate();
        } else {
            cardMap.setVisibility(View.GONE);
        }
    }
    /**
     * Loads the active user's registration state for the current event.
     */
    private void loadRegistration() {
        if (userId == null) {
            btnJoinLeave.setVisibility(View.GONE);
            return;
        }
        regRepo.getRegistration(currentEvent.getId(), userId)
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentRegistration = doc.toObject(Registration.class);
                        if (currentRegistration != null) currentRegistration.setId(doc.getId());
                    }
                    updateActionButtons();
                });
    }
    /**
     * Updates the visible action buttons to reflect the current registration state.
     */
    private void updateActionButtons() {
        if (isCoOrganizer || (userId != null && userId.equals(currentEvent.getOrganizerId()))) {
            // User is an organizer/co-organizer -> prevent joining the entrant pool
            btnJoinLeave.setText("Managing Event");
            btnJoinLeave.setEnabled(false);
            btnJoinLeave.setVisibility(View.VISIBLE);
            cardSelected.setVisibility(View.GONE);
            return;
        }

        if (currentRegistration == null) {
            if (currentEvent.isPrivate()) {
                btnJoinLeave.setText("Private Event");
                btnJoinLeave.setEnabled(false);
            } else {
                btnJoinLeave.setText(R.string.join_waiting_list);
                btnJoinLeave.setEnabled(currentEvent.isRegistrationOpen());
            }
            btnJoinLeave.setVisibility(View.VISIBLE);
            cardSelected.setVisibility(View.GONE);
        } else {
            switch (currentRegistration.getStatus()) {
                case Registration.STATUS_INVITED:
                    btnJoinLeave.setVisibility(View.GONE);
                    cardSelected.setVisibility(View.VISIBLE);
                    ((TextView)cardSelected.findViewById(R.id.tv_selected_title)).setText("You've been invited!");
                    ((TextView)cardSelected.findViewById(R.id.tv_selected_msg)).setText("Would you like to join the waiting list for this private event?");
                    break;
                case Registration.STATUS_WAITING:
                    btnJoinLeave.setText(R.string.leave_waiting_list);
                    btnJoinLeave.setVisibility(View.VISIBLE);
                    btnJoinLeave.setEnabled(true);
                    cardSelected.setVisibility(View.GONE);
                    break;
                case Registration.STATUS_SELECTED:
                    btnJoinLeave.setVisibility(View.GONE);
                    cardSelected.setVisibility(View.VISIBLE);
                    ((TextView)cardSelected.findViewById(R.id.tv_selected_title)).setText(R.string.you_selected);
                    ((TextView)cardSelected.findViewById(R.id.tv_selected_msg)).setText(R.string.selected_message);
                    break;
                case Registration.STATUS_ACCEPTED:
                    btnJoinLeave.setText("✅ Accepted");
                    btnJoinLeave.setEnabled(false);
                    btnJoinLeave.setVisibility(View.VISIBLE);
                    cardSelected.setVisibility(View.GONE);
                    break;
                case Registration.STATUS_DECLINED:
                case Registration.STATUS_CANCELLED:
                    btnJoinLeave.setText("Not attending");
                    btnJoinLeave.setEnabled(false);
                    btnJoinLeave.setVisibility(View.VISIBLE);
                    cardSelected.setVisibility(View.GONE);
                    break;
            }
        }
    }
    /**
     * Handles the primary waitlist action button for joining or leaving the event.
     */
    private void handleJoinLeave() {
        if (currentRegistration == null) {
            requestGeolocationThenJoin();
        } else if (Registration.STATUS_WAITING.equals(currentRegistration.getStatus())) {
            new AlertDialog.Builder(this)
                    .setTitle("Leave Waiting List")
                    .setMessage("Are you sure you want to leave the waiting list for \"" + currentEvent.getTitle() + "\"?")
                    .setPositiveButton("Leave", (d, w) -> leaveWaitingList())
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        }
    }
    /**
     * Requests location permission when needed before attempting to join the waitlist.
     */
    private void requestGeolocationThenJoin() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            
            if (currentEvent.isRequireGeolocation()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.geolocation_required_title)
                        .setMessage(R.string.geolocation_required_msg)
                        .setPositiveButton(R.string.allow_location, (d, w) -> {
                            pendingJoin = true;
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERM_CODE);
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            } else {
                pendingJoin = true;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERM_CODE);
            }
        } else {
            getLocationAndJoin();
        }
    }
    /**
     * Retrieves the user's last known location and continues the join workflow.
     */
    private void getLocationAndJoin() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) joinWaitingList(loc.getLatitude(), loc.getLongitude(), true);
                else joinWaitingList(0, 0, false);
            }).addOnFailureListener(e -> {
                joinWaitingList(0, 0, false);
            });
        } else {
            joinWaitingList(0, 0, false);
        }
    }
    /**
     * Handles the result of the location permission request used by the join workflow.
     *
     * @param requestCode application request code
     * @param permissions requested permission names
     * @param grantResults permission grant results aligned with {@code permissions}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERM_CODE && pendingJoin) {
            pendingJoin = false;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndJoin();
            } else {
                if (currentEvent.isRequireGeolocation()) {
                    Toast.makeText(this, "Location permission denied. This event requires location to join.", Toast.LENGTH_SHORT).show();
                } else {
                    joinWaitingList(0, 0, false);
                }
            }
        }
    }
    /**
     * Creates a registration and joins the current user to the event waiting list.
     *
     * @param lat captured latitude, or {@code 0} when unavailable
     * @param lng captured longitude, or {@code 0} when unavailable
     * @param hasGeo whether location data was successfully captured
     */
    private void joinWaitingList(double lat, double lng, boolean hasGeo) {
        if (userId == null) return;

        if (currentEvent.getMaxWaitingList() > 0 &&
                currentEvent.getWaitingListCount() >= currentEvent.getMaxWaitingList()) {
            Toast.makeText(this, R.string.error_waitlist_full, Toast.LENGTH_SHORT).show();
            return;
        }

        btnJoinLeave.setEnabled(false);
        Registration reg = new Registration(currentEvent.getId(), userId, "", "");
        reg.setLatitude(lat);
        reg.setLongitude(lng);
        reg.setHasGeolocation(hasGeo);
        if (hasGeo) reg.setGeoVerified(true);

        new com.example.eventlottery.repositories.UserRepository()
                .getUserById(userId)
                .addOnSuccessListener(userDoc -> {
                    com.example.eventlottery.models.User user =
                            userDoc.toObject(com.example.eventlottery.models.User.class);
                    if (user != null) {
                        reg.setUserName(user.getName());
                        reg.setUserEmail(user.getEmail());
                        reg.setUserPhone(user.getPhone());
                    }
                    reg.setUserId(userId);
                    regRepo.createRegistration(reg).addOnSuccessListener(v -> {
                        eventRepo.incrementWaitingListCount(currentEvent.getId(), 1);
                        int newCount = currentEvent.getWaitingListCount() + 1;
                        currentEvent.setWaitingListCount(newCount);
                        tvWaitingCount.setText(newCount + " on waiting list");
                        currentRegistration = reg;
                        updateActionButtons();
                        Toast.makeText(this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        btnJoinLeave.setEnabled(true);
                        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    });
                });
    }

    /**
     * Removes the current user's waiting-list registration for the event.
     */
    private void leaveWaitingList() {
        regRepo.deleteRegistration(currentEvent.getId(), userId)
                .addOnSuccessListener(v -> {
                    eventRepo.incrementWaitingListCount(currentEvent.getId(), -1);
                    int newCount = Math.max(0, currentEvent.getWaitingListCount() - 1);
                    currentEvent.setWaitingListCount(newCount);
                    tvWaitingCount.setText(newCount + " on waiting list");
                    currentRegistration = null;
                    updateActionButtons();
                    Toast.makeText(this, "Left waiting list", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Accepts the current invitation or private-event waitlist invite.
     */
    private void handleAccept() {
        if (currentRegistration == null) return;
        
        String newStatus;
        if (Registration.STATUS_INVITED.equals(currentRegistration.getStatus())) {
            newStatus = Registration.STATUS_WAITING;
        } else {
            newStatus = Registration.STATUS_ACCEPTED;
        }

        regRepo.updateStatus(currentEvent.getId(), userId, newStatus)
                .addOnSuccessListener(v -> {
                    currentRegistration.setStatus(newStatus);
                    if (Registration.STATUS_ACCEPTED.equals(newStatus)) {
                        eventRepo.incrementAcceptedCount(currentEvent.getId(), 1);
                        currentEvent.setAcceptedCount(currentEvent.getAcceptedCount() + 1);
                        Toast.makeText(this, "🎉 Spot accepted!", Toast.LENGTH_SHORT).show();
                    } else {
                        eventRepo.incrementWaitingListCount(currentEvent.getId(), 1);
                        currentEvent.setWaitingListCount(currentEvent.getWaitingListCount() + 1);
                        tvWaitingCount.setText(currentEvent.getWaitingListCount() + " on waiting list");
                        Toast.makeText(this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
                    }
                    updateActionButtons();
                });
    }
    /**
     * Confirms and processes a decline action for the current registration.
     */
    private void handleDecline() {
        if (currentRegistration == null) return;
        
        String msg = Registration.STATUS_INVITED.equals(currentRegistration.getStatus())
                ? "Are you sure you want to decline this invitation?"
                : "Are you sure? Declining will allow someone else to take your spot.";

        new AlertDialog.Builder(this)
                .setTitle("Decline")
                .setMessage(msg)
                .setPositiveButton("Decline", (d, w) -> {
                    regRepo.updateStatus(currentEvent.getId(), userId, Registration.STATUS_DECLINED)
                            .addOnSuccessListener(v -> {
                                boolean wasSelected = Registration.STATUS_SELECTED.equals(currentRegistration.getStatus());
                                currentRegistration.setStatus(Registration.STATUS_DECLINED);
                                
                                if (wasSelected) {
                                    AppNotification notif = new AppNotification(
                                            currentEvent.getOrganizerId(),
                                            currentEvent.getId(),
                                            currentEvent.getTitle(),
                                            AppNotification.TYPE_UPDATE,
                                            "Invitation Declined",
                                            "An entrant declined their spot for \"" + currentEvent.getTitle() + "\". You can draw a replacement.",
                                            true
                                    );
                                    notifRepo.createNotification(notif);
                                }

                                updateActionButtons();
                                Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
    /**
     * Posts a new comment for the current event using the active user's identity.
     */
    private void postComment() {
        if (userId == null) {
            Toast.makeText(this, "Please log in to comment", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;

        new com.example.eventlottery.repositories.UserRepository()
                .getUserById(userId)
                .addOnSuccessListener(doc -> {
                    com.example.eventlottery.models.User user =
                            doc.toObject(com.example.eventlottery.models.User.class);
                    String name = user != null ? user.getName() : "Anonymous";
                    Comment comment = new Comment(currentEvent.getId(), userId, name, text);
                    commentRepo.addComment(comment).addOnSuccessListener(v -> {
                        etComment.setText("");
                        loadComments();
                    });
                });
    }
    /**
     * Loads and sorts comments associated with the current event.
     */
    private void loadComments() {
        if (currentEvent == null) return;
        commentRepo.getCommentsForEvent(currentEvent.getId())
                .addOnSuccessListener(qs -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) { c.setId(doc.getId()); comments.add(c); }
                    }
                    comments.sort((a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt()));
                    commentAdapter.setComments(comments);
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing()) Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Opens a dialog that allows a comment to be edited.
     *
     * @param comment comment selected for editing
     */
    private void showEditCommentDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Comment");

        final EditText input = new EditText(this);
        input.setText(comment.getText());
        input.setSelection(comment.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty() && !newText.equals(comment.getText())) {
                comment.setText(newText);
                comment.setEdited(true);
                commentRepo.updateComment(comment).addOnSuccessListener(v -> {
                    Toast.makeText(this, "Comment updated", Toast.LENGTH_SHORT).show();
                    loadComments();
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    /**
     * Confirms deletion of the supplied comment and removes it if accepted.
     *
     * @param comment comment selected for deletion
     */
    private void confirmDeleteComment(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (d, w) -> {
                    commentRepo.deleteComment(comment.getId())
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
                                loadComments();
                            });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
    /**
     * Displays the event QR code in a dialog for sharing or scanning.
     */
    private void showQrDialog() {
        if (currentEvent.getQrCodeContent() == null) return;
        Bitmap qr = QRCodeHelper.generateQRCode(currentEvent.getQrCodeContent(), 600, 600);
        if (qr == null) return;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qr_code);

        ImageView ivQr   = dialog.findViewById(R.id.iv_qr);
        TextView tvTitle = dialog.findViewById(R.id.tv_qr_title);
        View btnClose    = dialog.findViewById(R.id.btn_close_qr);

        ivQr.setImageBitmap(qr);
        tvTitle.setText(currentEvent.getTitle());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    /**
     * Resumes the embedded map preview when the activity enters the foreground.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }
    /**
     * Pauses the embedded map preview while the activity is not visible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
