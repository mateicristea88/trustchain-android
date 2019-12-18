package nl.tudelft.cs4160.trustchain_android.ui.userconfiguration;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.ui.main.OverviewConnectionsActivity;

/**
 * The user is able to set his/her own username in this class.
 * It will be saved locally and used as identifier when looking for other peers.
 */
public class UserConfigurationActivity extends AppCompatActivity {
    public final static String VERSION_NAME_KEY = "VERSION_NAME_KEY:";
    public final static String TAG = UserConfigurationActivity.class.getName();

    /**
     * Checks if there is already a username set in the past.
     * If there is one, it should be stored in the preferences.
     * Go directly to the next activity when there is one already.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserNameStorage.getUserName(this) == null) {
            setContentView(R.layout.user_configuration);
            EditText userNameInput = findViewById(R.id.username);
            userNameInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            });
        } else {
            Intent myIntent = new Intent(UserConfigurationActivity.this, OverviewConnectionsActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            UserConfigurationActivity.this.startActivity(myIntent);
        }
    }

    /**
     * When clicking the confirm button check if the user name is not empty
     * if so then store the username and continue to the next activity.
     */
    public void onClickConfirm(View view) {
        EditText userNameInput = findViewById(R.id.username);
        if (!userNameInput.getText().toString().matches("")) {
            Intent myIntent = new Intent(UserConfigurationActivity.this, OverviewConnectionsActivity.class);
            UserNameStorage.setUserName(this, userNameInput.getText().toString());
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(myIntent);
        } else {
            TextView userNot = findViewById(R.id.user_notification);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                userNot.setTextColor(getResources().getColor(R.color.colorStatusCantConnect, null));
            } else {
                userNot.setTextColor(getResources().getColor(R.color.colorStatusCantConnect));
            }
            userNot.setText("Please fill in a username first!");
        }
    }

    /**
     * Hide the keyboard when the focus is not on the input field.
     *
     * @param view the view that contains the input field.
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
