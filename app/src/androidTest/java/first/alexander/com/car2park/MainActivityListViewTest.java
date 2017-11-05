package first.alexander.com.car2park;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityListViewTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityListViewTest() throws InterruptedException {

        Thread.sleep(10000);

        ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.parking_listView),
                                withParent(withId(R.id.swipe_refresh_layout_parking_list))),
                        0),
                        isDisplayed()));
        relativeLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.location_name), withText("UBC 6191 Agronomy Rd (Enter from laneway)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("UBC 6191 Agronomy Rd (Enter from laneway)")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.coordinates), withText("Coordinates: 49.26188, -123.24648"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        0),
                                2),
                        isDisplayed()));
        textView2.check(matches(withText("Coordinates: 49.26188, -123.24648")));

        /*ViewInteraction imageView = onView(
                allOf(withId(R.id.item_image),
                        childAtPosition(
                                allOf(withId(R.id.list_image),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.RelativeLayout.class),
                                                0)),
                                0),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));*/

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.location_name), withText("UBC Thunderbird Parkade (ROOFTOP LEVEL)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        1),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText("UBC Thunderbird Parkade (ROOFTOP LEVEL)")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.coordinates), withText("Coordinates: 49.26108, -123.24335"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        1),
                                2),
                        isDisplayed()));
        textView4.check(matches(withText("Coordinates: 49.26108, -123.24335")));

       /* ViewInteraction textView5 = onView(
                allOf(withId(R.id.location_name), withText("UBC Lot C2 (2446 Health Sciences Mall - 4 cars)"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        2),
                                1),
                        isDisplayed()));
        textView5.check(matches(withText("UBC Lot C2 (2446 Health Sciences Mall - 4 cars)")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.coordinates), withText("Coordinates: 49.26054, -123.24599"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        2),
                                2),
                        isDisplayed()));
        textView6.check(matches(withText("Coordinates: 49.26054, -123.24599")));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.location_name), withText("UBC - 2500 West Mall, on street"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        3),
                                1),
                        isDisplayed()));
        textView7.check(matches(withText("UBC - 2500 West Mall, on street")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.coordinates), withText("Coordinates: 49.25956, -123.2515"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        3),
                                2),
                        isDisplayed()));
        textView8.check(matches(withText("Coordinates: 49.25956, -123.2515")));

        ViewInteraction textView9 = onView(
                allOf(withId(R.id.location_name), withText("2166 East Mall UBC"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        4),
                                1),
                        isDisplayed()));
        textView9.check(matches(withText("2166 East Mall UBC")));

        ViewInteraction textView10 = onView(
                allOf(withId(R.id.coordinates), withText("Coordinates: 49.2646, -123.2494"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        4),
                                2),
                        isDisplayed()));
        textView10.check(matches(withText("Coordinates: 49.2646, -123.2494")));

        ViewInteraction textView11 = onView(
                allOf(withId(R.id.coordinates), withText("Coordinates: 49.2646, -123.2494"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.parking_listView),
                                        4),
                                2),
                        isDisplayed()));
        textView11.check(matches(withText("Coordinates: 49.2646, -123.2494")));



*/



    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
