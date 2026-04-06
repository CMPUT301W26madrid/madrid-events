package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.RemoteException;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.eventlottery.R;
import com.example.eventlottery.utils.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
/**
 * Instrumented UI tests for {@link LoginActivity}.
 *
 * <p>Purpose:
 * Verifies that the login screen loads correctly and that the screen can toggle
 * between sign-in and sign-up modes during entrant authentication flows.</p>
 *
 * <p>Role in application:
 * Supports regression testing for the app entry point by preparing a clean session,
 * dismissing disruptive system dialogs, and asserting core authentication UI elements.</p>
 *
 * <p>Outstanding issues:
 * The tests still rely on fixed sleep intervals and device-specific dialog dismissal,
 * which may make them less stable on slower emulators or devices.</p>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {
    /** UI Automator device handle used for wake, unlock, and dialog dismissal steps. */

    private UiDevice device;
    /** Scenario used to launch and close the login activity explicitly for each test. */
    private ActivityScenario<LoginActivity> scenario;
    /**
     * Clears any existing session, wakes and unlocks the test device, launches the activity,
     * and dismisses common system dialogs that may steal focus from the UI under test.
     *
     * @throws RemoteException if a shell command used to unlock the device fails
     * @throws IOException if the underlying device command infrastructure fails
     */
    @Before
    public void setUp() throws RemoteException, IOException {
        // 1. Clear session BEFORE launching the activity to prevent redirection
        SessionManager session = new SessionManager(ApplicationProvider.getApplicationContext());
        session.clearSession();

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        
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
        
        // 2. Launch activity manually
        scenario = ActivityScenario.launch(LoginActivity.class);

        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        
        dismissSystemDialogs();
    }
    /**
     * Closes the launched activity scenario after each test to release resources cleanly.
     */
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }
    /**
     * Attempts to dismiss a set of common system popups that may block test interactions.
     *
     * <p>This helper looks for known button labels such as permission prompts or warning
     * dialogs and clicks them when present.</p>
     */
    private void dismissSystemDialogs() {
        // Try to dismiss various common system dialogs
        String[] buttons = {"OK", "Don't Show Again", "Allow", "Dismiss", "Close", "Wait"};
        for (String text : buttons) {
            UiObject button = device.findObject(new UiSelector().text(text));
            if (button.exists()) {
                try {
                    button.click();
                } catch (UiObjectNotFoundException e) {
                    // Ignore
                }
            }
        }
    }
    /**
     * Verifies that the essential login screen widgets are displayed in sign-in mode.
     */
    @Test
    public void testLoginScreenViews() {
        // Wait for UI to settle and dismiss any late dialogs
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        dismissSystemDialogs();
        
        onView(withId(R.id.tv_login_title)).perform(closeSoftKeyboard());

        onView(withId(R.id.tv_login_title)).check(matches(withText("Sign In")));
        onView(withId(R.id.til_login_id)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_login)).check(matches(withText("Sign In")));
        
        onView(withId(R.id.tv_switch_to_signup)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_continue_device)).perform(scrollTo()).check(matches(isDisplayed()));
    }
    /**
     * Verifies that the login screen can toggle to sign-up mode and then back to sign-in mode.
     */
    @Test
    public void testToggleSignupMode() {
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        dismissSystemDialogs();
        
        onView(withId(R.id.tv_login_title)).perform(closeSoftKeyboard());

        // Initially in Sign In mode
        onView(withId(R.id.tv_login_title)).check(matches(withText("Sign In")));

        // Switch to Sign Up
        onView(withId(R.id.tv_switch_to_signup)).perform(scrollTo(), click());

        // Verify Sign Up mode
        onView(withId(R.id.tv_login_title)).check(matches(withText("Sign Up")));
        onView(withId(R.id.btn_login)).check(matches(withText("Sign Up")));
        onView(withId(R.id.tv_switch_to_signup)).perform(scrollTo()).check(matches(withText("Already have an account? Sign in")));

        // Switch back to Sign In
        onView(withId(R.id.tv_switch_to_signup)).perform(scrollTo(), click());

        // Verify Sign In mode
        onView(withId(R.id.tv_login_title)).check(matches(withText("Sign In")));
        onView(withId(R.id.btn_login)).check(matches(withText("Sign In")));
    }
}
