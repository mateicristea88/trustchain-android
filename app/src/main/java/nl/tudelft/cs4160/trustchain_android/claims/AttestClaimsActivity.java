package nl.tudelft.cs4160.trustchain_android.claims;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import nl.tudelft.cs4160.trustchain_android.R;

public class AttestClaimsActivity extends AppCompatActivity {

    private static final String TAG = AttestClaimsActivity.class.toString();
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attest_claims);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null){
            Toast.makeText(this, "No NFC on this device", Toast.LENGTH_LONG).show();
        }
        final Intent intent = new Intent(getApplicationContext(), getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create a PendingIntent object so the Android system can populate it with the details of the tag when it is scanned.
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // Declare intent filters to handle the intents that the developer wants to intercept.
        IntentFilter def = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        def.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            def.addDataType("application/com.sample.mime/string");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
//
//        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        ndef.addCategory(Intent.CATEGORY_DEFAULT);
//
//        IntentFilter ndef2 = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
//        ndef.addCategory(Intent.CATEGORY_DEFAULT);
//        try {
//            ndef.addDataType("application/com.sample.mime");
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            Log.wtf("mimeexception", e);
//            e.printStackTrace();
//        }
        intentFiltersArray = new IntentFilter[] { def };
//        handleViewIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // It's important, that the activity is in the foreground (resumed). Otherwise an IllegalStateException is thrown.
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
//            processIntent(getIntent());
//        }
    }

    @Override
    protected void onPause() {
        // Call this before super.onPause, otherwise an IllegalArgumentException is thrown as well.
        mNfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "TAG RECEIVED");
        processIntent(intent);
    }

    void processIntent(Intent intent) {
        textView = (TextView) findViewById(R.id.textView);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        String s = new String(msg.getRecords()[0].getPayload());
        textView.setText(s);
    }
}
