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
/**
 * Instrumented UI tests for {@link CreateEventActivity}.
 *
 * <p>Purpose:
 * Confirms that organizers can reach the event creation screen and that the
 * primary form fields needed to define a new event are visible.</p>
 *
 * <p>Role in application:
 * Helps protect the organizer event-management workflow by checking the basic
 * structure of the event creation interface used for lottery-enabled events.</p>
 *
 * <p>Outstanding issues:
 * The test only validates view presence and still depends on fixed wait times
 * and simple session mocking rather than full end-to-end organizer setup.</p>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {
    /**
     * Rule that launches the event creation activity for each test run.
     */
    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);
    /**
     * Wakes and unlocks the device, dismisses common blocking dialogs, and prepares
     * a mocked organizer session so the test stays on the create-event screen.
     *
     * @throws RemoteException if device shell commands fail while unlocking
     * @throws IOException if the test environment cannot execute the device command
     */
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
    /**
     * Attempts to close common system dialogs that may block Espresso interactions.
     */
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
    /**
     * Verifies that the key input fields and submit button on the create-event form are visible.
     */
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
