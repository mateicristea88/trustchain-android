package nl.tudelft.cs4160.trustchain_android.main;


import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.ui.userconfiguration.UserConfigurationActivity;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SentBlockAndInsertInChainTest {
    private static void emptyUserNamePreferences() {
        // Check whether it is empty
        // If not, put null in it
        if (UserNameStorage.getUserName(getInstrumentation().getTargetContext()) != null) {
            UserNameStorage.setUserName(getInstrumentation().getTargetContext(), null);
        }
    }

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityTestRule = new ActivityTestRule<>(UserConfigurationActivity.class);

    @BeforeClass
    public static void setup() {
        emptyUserNamePreferences();
    }

    @Test
    public void sentBlockAndInsertInChainTest() throws InterruptedException {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("ghhhg"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.confirm_button), withText("Confirm"), isDisplayed()));
        appCompatButton.perform(click());

        Thread.sleep(5000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.open_inbox_item), withText("Open Inbox")));
        appCompatButton2.perform(click());

        ViewInteraction appCompatButton3 = onView(
                withId(R.id.userButton));
        appCompatButton3.perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.new_peers_list_view))
                .atPosition(0)
                .perform(click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.open_inbox_item), withText("Open Inbox")));
        appCompatButton4.perform(click());

        onView(withId(R.id.my_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        ViewInteraction appCompatEditText2 = onView(withId(R.id.message_edit_text));
        appCompatEditText2.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                withId(R.id.message_edit_text));
        appCompatEditText3.perform(click());

        ViewInteraction appCompatEditText4 = onView(withId(R.id.message_edit_text));
        appCompatEditText4.perform(replaceText("hey"), closeSoftKeyboard());

        ViewInteraction appCompatButton5 = onView(withId(R.id.send_button));
        appCompatButton5.perform(click());

        ViewInteraction appCompatButton6 = onView(withId(R.id.view_chain_button));
        appCompatButton6.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.top_item),
                                childAtPosition(
                                        withId(R.id.blocks_list),
                                        1)),
                        0),
                        isDisplayed()));
        linearLayout.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.transaction), withText("hey"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                        0),
                                2),
                        isDisplayed()));
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
