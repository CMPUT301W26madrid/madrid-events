package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
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
public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void setUp() throws RemoteException, IOException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        if (!device.isScreenOn()) {
            device.wakeUp();
        }
        int width = device.getDisplayWidth();
        int height = device.getDisplayHeight();
        device.swipe(width / 2, height - 100, width / 2, 100, 10);
        device.executeShellCommand("wm dismiss-keyguard");

        dismissSystemDialogs();

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
    public void testProfileViews() {
        onView(withId(R.id.et_name)).check(matches(isDisplayed()));
        onView(withId(R.id.et_email)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_avatar)).check(matches(isDisplayed()));
    }
}
