package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.content.Intent;
import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.ui.userconfiguration.UserConfigurationActivity;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class UserConfigurationActivityTest {
    private String user = "New User";

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityRule = new ActivityTestRule<>(
            UserConfigurationActivity.class,
            true,
            false);

    @Test

    public void makeNewUsername() throws InterruptedException{
        emptyUserNamePreferences();
        mActivityRule.launchActivity(new Intent());
        Thread.sleep(1000);
        //enter the username
        onView(withId(R.id.username)).perform(replaceText(user));
        // press the login button
        Espresso.closeSoftKeyboard();
        Thread.sleep(1000);
        onView(withId(R.id.confirm_button)).perform(click());
        // look whether the ID is correctly displayed in the OverviewConnections window.
        onView(allOf(withId(R.id.peer_id), withText(user))).check(matches(isDisplayed()));
    }

    @Test
    public void usernameAlreadyStored(){
        setUsernameInPref();
        mActivityRule.launchActivity(new Intent());

        // look whether the ID is correctly displayed in the OverviewConnections window.
        onView(allOf(withId(R.id.peer_id), withText(user))).check(matches(isDisplayed()));
    }

    private void emptyUserNamePreferences(){
        // Check whether it is empty
        // If not, put null in it
        if(UserNameStorage.getUserName(getInstrumentation().getTargetContext()) != null) {
            UserNameStorage.setUserName(getInstrumentation().getTargetContext(), null);
        }
    }

    private void setUsernameInPref(){
        // Set SharedPreferences data
        UserNameStorage.setUserName(getInstrumentation().getTargetContext(), user);
    }
}
