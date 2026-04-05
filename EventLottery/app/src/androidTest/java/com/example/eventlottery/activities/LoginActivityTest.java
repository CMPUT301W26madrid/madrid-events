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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    private UiDevice device;
    private ActivityScenario<LoginActivity> scenario;

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

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

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
