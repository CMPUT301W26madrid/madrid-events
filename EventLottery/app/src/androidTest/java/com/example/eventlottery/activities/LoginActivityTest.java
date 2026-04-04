package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.R;
import com.example.eventlottery.utils.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Before
    public void setUp() {
        // Clear session to ensure we are on the login screen and not redirected
        SessionManager session = new SessionManager(ApplicationProvider.getApplicationContext());
        session.clearSession();
    }

    @Test
    public void testLoginScreenViews() {
        // Manually launch activity after clearing session
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            // The container for profiles should be visible (even if empty)
            onView(withId(R.id.rv_profiles)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            
            // btn_create_profile is inside a ScrollView, so use scrollTo()
            onView(withId(R.id.btn_create_profile)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }
}
