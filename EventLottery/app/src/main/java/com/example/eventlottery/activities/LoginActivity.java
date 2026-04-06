package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.adapters.ProfileAdapter;
import com.example.eventlottery.models.User;
import com.example.eventlottery.repositories.UserRepository;
import com.example.eventlottery.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private SessionManager session;
    private UserRepository userRepo;
    private ProfileAdapter profileAdapter;
    private RecyclerView rvProfiles;
    private View llQuickLogin;
    
    // Login Views
    private View llLoginFields;
    private TextInputLayout tilLoginId, tilLoginPassword;
    private TextInputEditText etLoginId, etLoginPassword;
    
    // Signup Views
    private View llSignupFields;
    private TextInputLayout tilSignupName, tilSignupEmail, tilSignupPhone, tilSignupPassword, tilSignupConfirmPassword;
    private TextInputEditText etSignupName, etSignupEmail, etSignupPhone, etSignupPassword, etSignupConfirmPassword;
    private CheckBox cbSignupEntrant, cbSignupOrganizer;

    private MaterialButton btnLogin, btnContinueDevice;
    private TextView tvSwitchToSignup, tvTitle, tvSubtitle;

    private boolean isSignupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session  = new SessionManager(this);
        userRepo = new UserRepository();

        if (session.isLoggedIn()) {
            navigateToRole(session.getActiveRole());
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();

        profileAdapter = new ProfileAdapter(this::onProfileSelected);
        rvProfiles.setLayoutManager(new LinearLayoutManager(this));
        rvProfiles.setAdapter(profileAdapter);

        btnLogin.setOnClickListener(v -> handleLoginOrSignup());
        tvSwitchToSignup.setOnClickListener(v -> toggleSignupMode());
        btnContinueDevice.setOnClickListener(v -> continueWithoutAccount());

        loadProfiles();
    }

    private void bindViews() {
        llQuickLogin = findViewById(R.id.ll_quick_login);
        rvProfiles = findViewById(R.id.rv_profiles);
        
        // Login bindings
        llLoginFields = findViewById(R.id.ll_login_fields);
        tilLoginId = findViewById(R.id.til_login_id);
        etLoginId = findViewById(R.id.et_login_id);
        tilLoginPassword = findViewById(R.id.til_login_password);
        etLoginPassword = findViewById(R.id.et_login_password);
        
        // Signup bindings
        llSignupFields = findViewById(R.id.ll_signup_fields);
        tilSignupName = findViewById(R.id.til_signup_name);
        etSignupName = findViewById(R.id.et_signup_name);
        tilSignupEmail = findViewById(R.id.til_signup_email);
        etSignupEmail = findViewById(R.id.et_signup_email);
        tilSignupPhone = findViewById(R.id.til_signup_phone);
        etSignupPhone = findViewById(R.id.et_signup_phone);
        tilSignupPassword = findViewById(R.id.til_signup_password);
        etSignupPassword = findViewById(R.id.et_signup_password);
        tilSignupConfirmPassword = findViewById(R.id.til_signup_confirm_password);
        etSignupConfirmPassword = findViewById(R.id.et_signup_confirm_password);
        cbSignupEntrant = findViewById(R.id.cb_signup_entrant);
        cbSignupOrganizer = findViewById(R.id.cb_signup_organizer);

        btnLogin = findViewById(R.id.btn_login);
        btnContinueDevice = findViewById(R.id.btn_continue_device);
        tvSwitchToSignup = findViewById(R.id.tv_switch_to_signup);
        tvTitle = findViewById(R.id.tv_login_title);
        tvSubtitle = findViewById(R.id.tv_login_subtitle);
    }

    private void toggleSignupMode() {
        isSignupMode = !isSignupMode;
        if (isSignupMode) {
            tvTitle.setText("Sign Up");
            tvSubtitle.setText("Create an account to join or organize events.");
            btnLogin.setText("Sign Up");
            tvSwitchToSignup.setText("Already have an account? Sign in");
            llLoginFields.setVisibility(View.GONE);
            llSignupFields.setVisibility(View.VISIBLE);
            llQuickLogin.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Sign In");
            tvSubtitle.setText("Sign in to access events as an entrant or organizer.");
            btnLogin.setText("Sign In");
            tvSwitchToSignup.setText("Don't have an account? Sign up");
            llLoginFields.setVisibility(View.VISIBLE);
            llSignupFields.setVisibility(View.GONE);
            loadProfiles();
        }
    }

    private void handleLoginOrSignup() {
        if (isSignupMode) {
            performSignup();
        } else {
            performLogin();
        }
    }

    private void performSignup() {
        String name = etSignupName.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String phone = etSignupPhone.getText().toString().trim();
        String pass = etSignupPassword.getText().toString().trim();
        String confirmPass = etSignupConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { tilSignupName.setError("Name required"); return; }
        if (TextUtils.isEmpty(email)) { tilSignupEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(pass)) { tilSignupPassword.setError("Password required"); return; }
        if (!pass.equals(confirmPass)) {
            tilSignupConfirmPassword.setError("Passwords do not match");
            return;
        }

        List<String> roles = new ArrayList<>();
        if (cbSignupEntrant.isChecked()) roles.add("entrant");
        if (cbSignupOrganizer.isChecked()) roles.add("organizer");

        if (roles.isEmpty()) {
            Toast.makeText(this, "Select at least one role", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Creating account...");

        userRepo.checkUserExists(email, phone.isEmpty() ? null : phone).addOnSuccessListener(exists -> {
            if (exists) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign Up");
                Toast.makeText(this, "User already exists with this email or phone", Toast.LENGTH_LONG).show();
            } else {
                User newUser = new User(name, email, phone, roles, session.getDeviceId());
                newUser.setPassword(pass);
                userRepo.addUser(newUser).addOnSuccessListener(ref -> {
                    newUser.setId(ref.getId());
                    completeLogin(newUser);
                }).addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign Up");
                });
            }
        });
    }

    private void performLogin() {
        String loginId = etLoginId.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(loginId)) {
            tilLoginId.setError("Email or Phone Number is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilLoginPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Checking…");

        userRepo.getUserByEmail(loginId).addOnSuccessListener(qs -> {
            if (qs != null && !qs.isEmpty()) {
                checkPasswordAndLogin(qs.getDocuments().get(0).toObject(User.class), password);
            } else {
                userRepo.getUserByPhone(loginId).addOnSuccessListener(qs2 -> {
                    if (qs2 != null && !qs2.isEmpty()) {
                        checkPasswordAndLogin(qs2.getDocuments().get(0).toObject(User.class), password);
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign In");
                        Toast.makeText(this, "Account not found. Please sign up.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
        });
    }

    private void checkPasswordAndLogin(User user, String password) {
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            completeLogin(user);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
        }
    }

    private void completeLogin(User user) {
        if (user == null) return;

        // Ensure this user profile is associated with the current device for Quick Login
        String currentDeviceId = session.getDeviceId();
        if (!currentDeviceId.equals(user.getDeviceId())) {
            user.setDeviceId(currentDeviceId);
            userRepo.updateUser(user.getId(), user);
        }

        session.saveUserId(user.getId());
        String role = "entrant";
        if (user.hasRole("admin")) role = "admin";
        else if (user.hasRole("organizer")) role = "organizer";
        session.saveActiveRole(role);
        navigateToRole(role);
    }

    private void continueWithoutAccount() {
        userRepo.getUserByDeviceId(session.getDeviceId()).addOnSuccessListener(qs -> {
            if (qs != null && !qs.isEmpty()) {
                User u = qs.getDocuments().get(0).toObject(User.class);
                if (u != null) {
                    u.setId(qs.getDocuments().get(0).getId());
                    completeLogin(u);
                }
            } else {
                User guest = new User("Guest User", "", "", List.of("entrant"), session.getDeviceId());
                userRepo.addUser(guest).addOnSuccessListener(ref -> {
                    guest.setId(ref.getId());
                    completeLogin(guest);
                });
            }
        });
    }

    private void loadProfiles() {
        userRepo.getUserByDeviceId(session.getDeviceId()).addOnSuccessListener(qs -> {
            if (qs == null) return;
            List<User> profiles = new ArrayList<>();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                User u = doc.toObject(User.class);
                if (u != null) { u.setId(doc.getId()); profiles.add(u); }
            }
            if (!profiles.isEmpty()) {
                llQuickLogin.setVisibility(isSignupMode ? View.GONE : View.VISIBLE);
                profileAdapter.setProfiles(profiles);
            } else {
                llQuickLogin.setVisibility(View.GONE);
            }
        });
    }

    private void onProfileSelected(User user) {
        if (user == null) return;
        
        // If in signup mode, switch back to login mode
        if (isSignupMode) {
            toggleSignupMode();
        }

        // If the user has no password (guest), log in immediately
        if (TextUtils.isEmpty(user.getPassword())) {
            completeLogin(user);
            return;
        }

        // For users with passwords, populate ID and focus password field
        etLoginId.setText(user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : user.getPhone());
        etLoginPassword.setText(""); // Clear password field
        etLoginPassword.requestFocus();
        
        Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
    }

    private void navigateToRole(String role) {
        Intent intent;
        switch (role) {
            case "admin": intent = new Intent(this, AdminMainActivity.class); break;
            case "organizer": intent = new Intent(this, OrganizerMainActivity.class); break;
            default: intent = new Intent(this, EntrantMainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
