package nl.tudelft.cs4160.trustchain_android.claims;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AttestClaimsActivity extends AppCompatActivity {
    //TODO check for NFC availability, show settings snackbar

    private static final String TAG = AttestClaimsActivity.class.toString();
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private MessageProto.TrustChainBlock receivedBlock;

    private TextView textView;
    private Button signButton;
    public static final int SCAN_QR = 1;

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
            def.addDataType("application/nl.tudelft.cs4160.trustchain_android");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        intentFiltersArray = new IntentFilter[] { def };

        signButton = findViewById(R.id.sign_button);
        textView = findViewById(R.id.textView);
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

    /**
     * Receives data from Android Beam.
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "Tag received");
        processIntent(intent);
    }

    /**
     * Processes a received Intent from Android Beam to get a block.
     * @param intent
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        byte[] payload = msg.getRecords()[0].getPayload();

        try {
            receivedBlock = MessageProto.TrustChainBlock.newBuilder().mergeFrom(payload).build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not parse received block!");
            return;
        }
        String claimText = "name: " + new String(receivedBlock.getTransaction().getClaim().getName().toByteArray(), UTF_8) + "\n" +
                "data: " + new String(receivedBlock.getTransaction().getUnformatted().toByteArray(), UTF_8);
        textView.setText(claimText);
        signButton.setVisibility(View.VISIBLE);
    }

    /**
     * Completes the block proposal and inserts it into the database.
     * @param v - unused
     */
    public void onClickSign(View v) {
        //TODO what do we do here exactly, sending back, generating proof?
        TrustChainDBHelper DBHelper = new TrustChainDBHelper(this);
        DualSecret keyPair = Key.loadKeys(this);
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(null, DBHelper,
                keyPair.getPublicKeyPair().toBytes(),
                receivedBlock, receivedBlock.getPublicKey().toByteArray(), null);

        final MessageProto.TrustChainBlock signedBlock = TrustChainBlockHelper.sign(receivedBlock, keyPair.getSigningKey());
        DBHelper.insertInDB(signedBlock);
    }

    /**
     * Called when the user clicks the Scan QR code button.
     * @param v
     */
    public void onClickScanQR (View v) {
        startQRScanner();
    }

    /**
     * Starts ScanQRActivity to scan a QR code from another device.
     */
    private void startQRScanner () {
        startActivityForResult(new Intent(this, ScanQRActivity.class), SCAN_QR);
    }

    /**
     * Receives data from ScanQRActivity containing the block scanned from a QR code.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                byte[] bytes = result.getBytes(ISO_8859_1);
                try {
                    receivedBlock = MessageProto.TrustChainBlock.newBuilder().mergeFrom(bytes).build();
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Could not parse received block!");
                    return;
                }
                String claimText = "name: " + new String(receivedBlock.getTransaction().getClaim().getName().toByteArray(), UTF_8) + "\n" +
                        "data: " + new String(receivedBlock.getTransaction().getUnformatted().toByteArray(), UTF_8);
                textView.setText(claimText);
                signButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
