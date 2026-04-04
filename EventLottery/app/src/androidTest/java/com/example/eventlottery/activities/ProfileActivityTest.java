package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.R;
import com.example.eventlottery.utils.SessionManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void setUp() {
        // Mock a logged-in user to prevent redirection to login
        SessionManager session = new SessionManager(ApplicationProvider.getApplicationContext());
        session.saveUserId("test_user_id");
        session.saveActiveRole("entrant");
    }

    @Test
    public void testProfileViews() {
        // Since we are in a NestedScrollView, it is safer to scroll to views before checking isDisplayed()
        onView(withId(R.id.et_name)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_email)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_phone)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.sw_notifications)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save)).perform(scrollTo()).check(matches(isDisplayed()));
    }
}
