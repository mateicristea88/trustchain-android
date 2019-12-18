package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.ui.main.OverviewConnectionsActivity;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by Laurens on 12/18/2017.
 */

public class OverviewConnectionsActivityTest {

    @Rule
    public ActivityTestRule<OverviewConnectionsActivity> mActivityRule = new ActivityTestRule<>(
            OverviewConnectionsActivity.class);

    @Test
    public void gotoBootstrapActivity(){
        // Open the ActionBar
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // Why not able to find by: withId(R.id.find_peer)
        onView(withText("Change bootstrap"))   // withId(R.id.my_view) is a ViewMatcher
                .perform(click());            // click() is a ViewAction
        onView(withId(R.id.bootstrap_IP)).check(matches(isDisplayed()));
    }

    @Test
    public void gotoChainExplorerActivity() {
        // Open the ActionBar
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // Click on the menu item
        onView(withText("My Chain"))
                .perform(click());
        // Show the chain on the screen.
        onView(withId(R.id.blocks_list)).check(matches(isDisplayed()));
    }

    @Test
    public void gotoConnectionExplanationActivity() {
        // Open the ActionBar
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        // Click on the menu item
        onView(withText("Info")).perform(click());
        // Go to connection explanations.
        onView(withId(R.id.connectionColorExplanationList)).check(matches(isDisplayed()));
    }
}
