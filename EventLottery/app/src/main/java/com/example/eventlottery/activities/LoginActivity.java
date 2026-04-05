package com.example.eventlottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
    private TextInputLayout tilLoginId;
    private TextInputEditText etLoginId;
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
        tilLoginId = findViewById(R.id.til_login_id);
        etLoginId = findViewById(R.id.et_login_id);
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
            llQuickLogin.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Sign In");
            tvSubtitle.setText("Sign in to access events as an entrant or organizer.");
            btnLogin.setText("Sign In");
            tvSwitchToSignup.setText("Don't have an account? Sign up");
            loadProfiles();
        }
    }

    private void handleLoginOrSignup() {
        String input = etLoginId.getText() != null ? etLoginId.getText().toString().trim() : "";
        if (TextUtils.isEmpty(input)) {
            tilLoginId.setError("Email or Phone Number is required");
            return;
        }

        if (isSignupMode) {
            checkUniquenessAndSignup(input);
        } else {
            performLogin(input);
        }
    }

    private void checkUniquenessAndSignup(String input) {
        String email = input.contains("@") ? input : null;
        String phone = !input.contains("@") ? input : null;

        userRepo.checkUserExists(email, phone).addOnSuccessListener(exists -> {
            if (exists) {
                Toast.makeText(this, "User already exists with this email or phone", Toast.LENGTH_LONG).show();
            } else {
                showSignupDetailsDialog(input);
            }
        });
    }

    private void performLogin(String loginId) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Checking…");

        userRepo.getUserByEmail(loginId).addOnSuccessListener(qs -> {
            if (qs != null && !qs.isEmpty()) {
                verifyPasswordThenLogin(qs.getDocuments().get(0).toObject(User.class));
            } else {
                userRepo.getUserByPhone(loginId).addOnSuccessListener(qs2 -> {
                    if (qs2 != null && !qs2.isEmpty()) {
                        verifyPasswordThenLogin(qs2.getDocuments().get(0).toObject(User.class));
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

    private void verifyPasswordThenLogin(User user) {
        if (user == null) {
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verification Required");
        builder.setMessage("Please enter your password to continue.");

        final EditText input = new EditText(this);
        input.setHint("Password");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String pass = input.getText().toString();
            if (user.getPassword() != null && user.getPassword().equals(pass)) {
                completeLogin(user);
            } else {
                btnLogin.setEnabled(true);
                btnLogin.setText("Sign In");
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign In");
            dialog.cancel();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void completeLogin(User user) {
        if (user == null) return;
        session.saveUserId(user.getId());
        String role = "entrant";
        if (user.hasRole("admin")) role = "admin";
        else if (user.hasRole("organizer")) role = "organizer";
        session.saveActiveRole(role);
        navigateToRole(role);
    }

    private void showSignupDetailsDialog(String loginId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);
        TextInputEditText etPass = dialogView.findViewById(R.id.et_password);
        CheckBox cbAdmin = dialogView.findViewById(R.id.cb_admin);
        
        cbAdmin.setVisibility(View.GONE);
        if (loginId.contains("@")) { 
            etEmail.setText(loginId); 
            etEmail.setEnabled(false);
        } else { 
            etPhone.setText(loginId); 
            etPhone.setEnabled(false);
        }

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        dialogView.findViewById(R.id.btn_create).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            
            if (TextUtils.isEmpty(name)) { etName.setError("Name required"); return; }
            if (TextUtils.isEmpty(pass)) { etPass.setError("Password required"); return; }
            
            List<String> roles = new ArrayList<>();
            if (((CheckBox)dialogView.findViewById(R.id.cb_entrant)).isChecked()) roles.add("entrant");
            if (((CheckBox)dialogView.findViewById(R.id.cb_organizer)).isChecked()) roles.add("organizer");

            if (roles.isEmpty()) {
                Toast.makeText(this, "Select at least one role", Toast.LENGTH_SHORT).show(); return;
            }

            // Final check to ensure uniqueness before adding
            userRepo.checkUserExists(email, phone).addOnSuccessListener(exists -> {
                if (exists) {
                    Toast.makeText(this, "A user with this email or phone already signed up.", Toast.LENGTH_LONG).show();
                } else {
                    User newUser = new User(name, email, phone, roles, session.getDeviceId());
                    newUser.setPassword(pass);
                    userRepo.addUser(newUser).addOnSuccessListener(ref -> {
                        newUser.setId(ref.getId());
                        dialog.dismiss();
                        completeLogin(newUser);
                    });
                }
            });
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void continueWithoutAccount() {
        userRepo.getUserByDeviceId(session.getDeviceId()).addOnSuccessListener(qs -> {
            if (qs != null && !qs.isEmpty()) {
                verifyPasswordThenLogin(qs.getDocuments().get(0).toObject(User.class));
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
                llQuickLogin.setVisibility(View.VISIBLE);
                profileAdapter.setProfiles(profiles);
            } else {
                llQuickLogin.setVisibility(View.GONE);
            }
        });
    }

    private void onProfileSelected(User user) {
        if (user == null) return;
        verifyPasswordThenLogin(user);
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
