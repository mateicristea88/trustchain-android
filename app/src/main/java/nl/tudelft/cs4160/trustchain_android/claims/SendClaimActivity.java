package nl.tudelft.cs4160.trustchain_android.claims;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import nl.tudelft.cs4160.trustchain_android.R;

import static android.nfc.NdefRecord.createMime;

public class SendClaimActivity extends AppCompatActivity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {

    private static final String TAG = SendClaimActivity.class.toString();
    private static final String TITLE = "Send claim";
    NfcAdapter mNfcAdapter;
    // Flag to indicate that Android Beam is available
    boolean mAndroidBeamAvailable  = false;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_send_claim);
        // NFC isn't available on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC isn't supported
           showNotsupportedMessage();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Android Beam isn't available.
            mAndroidBeamAvailable = false;
            showNotsupportedMessage();
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }

        if (!mNfcAdapter.isNdefPushEnabled()) {
            Log.e(TAG, "NDEF push not enabled");
        }
        setTitle(TITLE);
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
//        mNfcAdapter.invokeBeam(this);
   }

    private void showNotsupportedMessage() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("NFC not supported")
                .setMessage("NFC or Android Beam is not supported on this device, claims cannot be sent")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
       Log.e(TAG, getApplicationContext().getPackageName());
        String text = getIntent().getStringExtra("claim");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.com.example.android.beam", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                        */
                        , NdefRecord.createApplicationRecord(getApplicationContext().getPackageName())
                });
        return msg;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Log.e(TAG, "Beam transfer complete");
   }
}
