package com.example.eventlottery.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.eventlottery.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantMainActivityTest {

    @Rule
    public ActivityScenarioRule<EntrantMainActivity> activityRule =
            new ActivityScenarioRule<>(EntrantMainActivity.class);

    @Test
    public void testNavigationElements() {
        // Check bottom navigation visibility
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()));
        
        // Check header elements
        // Use withEffectiveVisibility for the spinner because it might have 0 width if no items are loaded yet
        onView(withId(R.id.spinner_role)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        onView(withId(R.id.iv_bell)).check(matches(isDisplayed()));
    }
}
