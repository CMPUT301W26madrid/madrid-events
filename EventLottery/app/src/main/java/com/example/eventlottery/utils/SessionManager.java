/**
 * Role: Utility / Session Manager
 * Purpose: Manages local user session state using SharedPreferences,
 * including user ID persistence, active role tracking, and stable
 * device identification via Android ID.
 *
 * Design Pattern: Static Utility Class. It wraps SharedPreferences access
 * behind a clean interface, allowing Activities and Services to read and
 * write session data without directly coupling to Android storage APIs.
 */

package com.example.eventlottery.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

public class SessionManager {
    private static final String PREF_NAME   = "EventLotterySession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE    = "activeRole";

    private final SharedPreferences prefs;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Stable device identifier (Android ID) */
    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return getUserId() != null;
    }

    public void saveActiveRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public String getActiveRole() {
        return prefs.getString(KEY_ROLE, "entrant");
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
