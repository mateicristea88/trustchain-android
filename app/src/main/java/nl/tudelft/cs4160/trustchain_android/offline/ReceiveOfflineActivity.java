package nl.tudelft.cs4160.trustchain_android.offline;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.ui.chainexplorer.ChainColor;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.ui.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.util.OpenFileClickListener;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class ReceiveOfflineActivity extends AppCompatActivity {
    //TODO check for NFC availability, show settings snackbar

    private static final String TAG = ReceiveOfflineActivity.class.toString();
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private MessageProto.TrustChainBlock receivedBlock;

    private TextView blockSigned;
    private Button signButton;
    private Button returnHome;
    public static final int SCAN_QR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_offline);
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
        blockSigned = findViewById(R.id.block_signed);
        returnHome = findViewById(R.id.return_home_button);
        returnHome.setOnClickListener(view -> {
            Intent i = new Intent(this, OverviewConnectionsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // It's important, that the activity is in the foreground (resumed). Otherwise an IllegalStateException is thrown.
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
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
        blockReceived(receivedBlock);
    }

    /**
     * This methods is mostly a direct copy from ChainExplorerAdapter#getView, re-using the block layout used in ChainExplorer
     * @param block
     */
    public void blockReceived(MessageProto.TrustChainBlock block) {
        View convertView = findViewById(R.id.block_layout);

        try {
            ValidationResult validationResult = TrustChainBlockHelper.validate(block, new TrustChainDBHelper(this));
            if(validationResult.getStatus() == ValidationResult.INVALID) {
                signButton.setEnabled(false);
                signButton.setFocusableInTouchMode(true);
                signButton.setError(getString(R.string.invalid_block));
                signButton.requestFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
            signButton.setEnabled(false);
            signButton.setFocusableInTouchMode(true);
            signButton.setError(getString(R.string.invalid_block));
            signButton.requestFocus();
        }

        showBlockLayout(convertView);

        try {
            new TrustChainDBHelper(this).insertInDB(receivedBlock);
        } catch (Exception ignored) {
        }

        if (getIntent().getBooleanExtra("return", true)) {
            signButton.setVisibility(View.VISIBLE);
        } else {
            //TODO possible re-verification
            signButton.setVisibility(View.GONE);
            blockSigned.setVisibility(View.VISIBLE);
            returnHome.setVisibility(View.VISIBLE);
        }
    }

    private String getPeerAlias(PublicKeyPair keyPair) {
        String alias = UserNameStorage.getPeerByPublicKey(getApplicationContext(), keyPair);
        if (alias == null)
            alias = "unknown";
        if (Key.loadKeys(getApplicationContext()).getPublicKeyPair().equals(keyPair))
            alias = "me";
        return alias;
    }

    private void showBlockLayout(View convertView) {
        convertView.setVisibility(View.VISIBLE);
        setExpandArrowBehaviour(convertView);
        // Check if we already know the peer, otherwise add it to the peerList
        ByteString pubKeyByteStr = receivedBlock.getPublicKey();
        ByteString linkPubKeyByteStr = receivedBlock.getLinkPublicKey();
        String peerAlias = getPeerAlias(new PublicKeyPair(pubKeyByteStr.toByteArray()));
        String linkPeerAlias = getPeerAlias(new PublicKeyPair(linkPubKeyByteStr.toByteArray()));

        // Check if the sequence numbers are 0, which would mean that they are unknown
        String seqNumStr;
        String linkSeqNumStr;
        if (receivedBlock.getSequenceNumber() == 0) {
            seqNumStr = "Genesis Block";
        } else {
            seqNumStr = "seq: " + String.valueOf(receivedBlock.getSequenceNumber());
        }

        if (receivedBlock.getLinkSequenceNumber() == 0) {
            linkSeqNumStr = "";
        } else {
            linkSeqNumStr = "seq: " + String.valueOf(receivedBlock.getLinkSequenceNumber());
        }

        // collapsed view
        TextView peer = convertView.findViewById(R.id.peer);
        TextView seqNum = convertView.findViewById(R.id.sequence_number);
        TextView linkPeer = convertView.findViewById(R.id.link_peer);
        TextView linkSeqNum = convertView.findViewById(R.id.link_sequence_number);
        TextView transaction = convertView.findViewById(R.id.transaction);
        View ownChainIndicator = convertView.findViewById(R.id.own_chain_indicator);
        View linkChainIndicator = convertView.findViewById(R.id.link_chain_indicator);

        // For the collapsed view, set the public keys to the aliases we gave them.
        peer.setText(peerAlias);
        seqNum.setText(seqNumStr);
        linkPeer.setText(linkPeerAlias);
        linkSeqNum.setText(linkSeqNumStr);

        // expanded view
        TextView pubKey = convertView.findViewById(R.id.pub_key);
        TextView linkPubKey = convertView.findViewById(R.id.link_pub_key);
        TextView prevHash = convertView.findViewById(R.id.prev_hash);
        TextView signature = convertView.findViewById(R.id.signature);
        TextView expTransaction = convertView.findViewById(R.id.expanded_transaction);

        pubKey.setText(ByteArrayConverter.bytesToHexString(pubKeyByteStr.toByteArray()));
        linkPubKey.setText(ByteArrayConverter.bytesToHexString(linkPubKeyByteStr.toByteArray()));
        prevHash.setText(ByteArrayConverter.bytesToHexString(receivedBlock.getPreviousHash().toByteArray()));

        signature.setText(ByteArrayConverter.bytesToHexString(receivedBlock.getSignature().toByteArray()));

        if (TrustChainBlockHelper.containsBinaryFile(receivedBlock)) {
            // If the block contains a file show the 'click to open' text
            transaction.setText(getString(R.string.click_to_open_file, receivedBlock.getTransaction().getFormat()));
            setOpenFileClickListener(transaction, receivedBlock);

            expTransaction.setText(getString(R.string.click_to_open_file, receivedBlock.getTransaction().getFormat()));
            setOpenFileClickListener(expTransaction, receivedBlock);
        } else {
            transaction.setText(receivedBlock.getTransaction().getUnformatted().toStringUtf8());
            expTransaction.setText(receivedBlock.getTransaction().getUnformatted().toStringUtf8());
        }

        if (peerAlias.equals("me")) {
            ownChainIndicator.setBackgroundColor(ChainColor.getMyColor(this));
        } else {
            ownChainIndicator.setBackgroundColor(ChainColor.getColor(this,ByteArrayConverter.bytesToHexString(pubKeyByteStr.toByteArray())));
        }
        if (linkPeerAlias.equals("me")) {
            linkChainIndicator.setBackgroundColor(ChainColor.getMyColor(this));
        } else {
            linkChainIndicator.setBackgroundColor(ChainColor.getColor(this,ByteArrayConverter.bytesToHexString(pubKeyByteStr.toByteArray())));
        }
    }

    private void setExpandArrowBehaviour(View layout) {
        LinearLayout expandedItem = layout.findViewById(R.id.expanded_item);
        ImageView expandArrow = layout.findViewById(R.id.expand_arrow);
        layout.setOnClickListener(view -> {
            // Expand the item when it is clicked
            if (expandedItem.getVisibility() == View.GONE) {
                expandedItem.setVisibility(View.VISIBLE);
                Log.v(TAG, "Item height: " + expandedItem.getHeight());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    expandArrow.setImageDrawable(getDrawable(R.drawable.ic_expand_less_black_24dp));
                } else {
                    expandArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                }
            } else {
                expandedItem.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    expandArrow.setImageDrawable(getDrawable(R.drawable.ic_expand_more_black_24dp));
                } else {
                    expandArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
                }
            }
        });
    }

    private void setOpenFileClickListener(View view, final MessageProto.TrustChainBlock block) {
        view.setOnClickListener(new OpenFileClickListener(this, block));
    }

    /**
     * Completes the block proposal and inserts it into the database.
     * @param v - unused
     */
    public void onClickSign(View v) {
        signButton.setEnabled(false);
        TrustChainDBHelper DBHelper = new TrustChainDBHelper(this);
        DualSecret keyPair = Key.loadKeys(this);
        MessageProto.TrustChainBlock block = TrustChainBlockHelper.createBlock(null, null, DBHelper,
                keyPair.getPublicKeyPair().toBytes(),
                receivedBlock, receivedBlock.getPublicKey().toByteArray());

        final MessageProto.TrustChainBlock signedBlock = TrustChainBlockHelper.sign(block, keyPair.getSigningKey());
        DBHelper.insertInDB(signedBlock);

        Intent intent = new Intent(this, SendOfflineActivity.class);
        intent.putExtra("block", signedBlock);
        intent.putExtra("return", false);
        startActivity(intent);
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
                    blockReceived(receivedBlock);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Could not parse received block!");
                }
            }
        }
    }
}
