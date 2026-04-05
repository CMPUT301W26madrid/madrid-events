package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.RemoteException;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
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
public class EntrantMainActivityTest {

    @Rule
    public ActivityScenarioRule<EntrantMainActivity> activityRule =
            new ActivityScenarioRule<>(EntrantMainActivity.class);

    @Before
    public void setUp() throws RemoteException, IOException {
        // Unlock and wake up the device if needed
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        if (!device.isScreenOn()) {
            device.wakeUp();
        }
        device.executeShellCommand("wm dismiss-keyguard");

        // Dismiss system dialogs (like the 16KB compatibility warning) using UI Automator
        dismissSystemDialogs();

        // Mock a logged-in user to prevent redirection to login
        SessionManager session = new SessionManager(ApplicationProvider.getApplicationContext());
        session.saveUserId("test_user_id");
        session.saveActiveRole("entrant");
    }

    private void dismissSystemDialogs() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        String[] buttons = {"OK", "Don't Show Again", "Allow", "Dismiss"};
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
    public void testNavigationElements() {
        // Check bottom navigation visibility
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
        
        // Check header elements
        // Spinner might be populated asynchronously, so use withEffectiveVisibility
        onView(withId(R.id.spinner_role)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(R.id.iv_bell)).check(matches(isDisplayed()));
    }
}
