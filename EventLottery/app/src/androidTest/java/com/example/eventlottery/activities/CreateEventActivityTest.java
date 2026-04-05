package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.RemoteException;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.example.eventlottery.R;
import com.example.eventlottery.utils.SessionManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);

    @Before
    public void setUp() throws RemoteException, IOException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        
        // Ensure device is awake and unlocked
        if (!device.isScreenOn()) {
            device.wakeUp();
        }
        device.pressMenu(); 
        
        // Swipe up to dismiss lock screen
        int width = device.getDisplayWidth();
        int height = device.getDisplayHeight();
        device.swipe(width / 2, height - 200, width / 2, 200, 20);
        
        device.executeShellCommand("wm dismiss-keyguard");

        // Dismiss system dialogs that might steal focus
        dismissSystemDialogs();

        // Mock a logged-in user to prevent redirection to login
        SessionManager session = new SessionManager(ApplicationProvider.getApplicationContext());
        session.saveUserId("test_organizer_id");
        session.saveActiveRole("organizer");
    }

    private void dismissSystemDialogs() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String[] buttons = {"OK", "Don't Show Again", "Allow", "Dismiss", "Close"};
        for (String text : buttons) {
            UiObject button = device.findObject(new UiSelector().text(text));
            if (button.exists()) {
                try {
                    button.click();
                } catch (Exception e) {
                    // ignored
                }
            }
        }
    }

    @Test
    public void testCreateEventViews() {
        // Wait a bit for the activity to settle
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        dismissSystemDialogs();

        onView(withId(R.id.et_title)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_description)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_location)).perform(scrollTo()).check(matches(isDisplayed()));
        
        onView(withId(R.id.et_capacity)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create_event)).perform(scrollTo()).check(matches(isDisplayed()));
    }
}
