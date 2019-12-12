package nl.tudelft.cs4160.trustchain_android.main;


import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.database.dao.PeerDao;
import nl.tudelft.cs4160.trustchain_android.storage.repository.PeerRepository;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.ui.userconfiguration.UserConfigurationActivity;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ClearInboxTest {
    private static void emptyUserNamePreferences() {
        // Check whether it is empty
        // If not, put null in it
        if (UserNameStorage.getUserName(getInstrumentation().getTargetContext()) != null) {
            UserNameStorage.setUserName(getInstrumentation().getTargetContext(), null);
        }
    }

    private static void clearInbox() {
        Context context = getInstrumentation().getTargetContext();
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        PeerDao peerDao = appDatabase.peerDao();
        PeerRepository peerRepository = new PeerRepository(peerDao);
        peerRepository.deleteAllPeers();
    }

    @BeforeClass
    public static void setup() {
        emptyUserNamePreferences();
        clearInbox();
    }

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityTestRule =
            new ActivityTestRule<>(UserConfigurationActivity.class);

    @Test
    public void clearInboxTest() throws InterruptedException {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("sdgs"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.confirm_button), withText("Confirm"), isDisplayed()));
        appCompatButton.perform(click());

        Thread.sleep(5000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.open_inbox_item), withText("Open Inbox"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.userButton), withText("+ Find new users"),
                        childAtPosition(
                                allOf(withId(R.id.wrapperLinearLayout),
                                        childAtPosition(
                                                withId(R.id.my_recycler_view),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatButton3.perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.new_peers_list_view))
                .atPosition(0)
                .perform(click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.open_inbox_item), withText("Open Inbox"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        appCompatButton4.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(withText("Clear Entire Inbox"));
        appCompatTextView.perform(click());

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
