package nl.tudelft.cs4160.trustchain_android.passport.ocr;

import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.tudelft.cs4160.trustchain_android.R;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class TestManualInputActivity {
    /**
     * Start up the main activity for each test.
     */
    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<ManualInputActivity>(
            ManualInputActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent result = new Intent(targetContext, ManualInputActivity.class);
            return result;
        }
    };

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void destroy() {
        Intents.release();
    }

    @Test
    public void testInvalidData() {
        closeSoftKeyboard();
        onView(withId(R.id.submit_button)).perform(click());
        onView(withId(R.id.doc_num))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasErrorText(
                                activityRule.getActivity().getString(R.string.errInputDocNum))));
    }

}