package nl.tudelft.cs4160.trustchain_android.claims;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.createBlock;

public class CreateClaimActivity extends AppCompatActivity {

    private static final String TAG = CreateClaimActivity.class.getName();
    private EditText messageEditText;
    private InboxItem inboxItemOtherPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_claim);

        messageEditText = findViewById(R.id.message_edit_text);
        inboxItemOtherPeer = (InboxItem) getIntent().getSerializableExtra("peer");
    }

    /**
     * Creates and returns a TrustChainBlock containing a claim consisting of the text in the message field.
     * Currently sets dummy values for name, proofformat and validity term.
     * @return TrustChainBlock signed block proposal
     */
    public MessageProto.TrustChainBlock createClaimBlock() {
        byte[] publicKey = Key.loadKeys(this).getPublicKeyPair().toBytes();
        byte[] transactionData = messageEditText.getText().toString().getBytes(UTF_8);

        MessageProto.TrustChainBlock.Claim.Builder claimBuilder = MessageProto.TrustChainBlock.Claim.newBuilder();
        claimBuilder.setName(ByteString.copyFromUtf8("claim"));
        claimBuilder.setTimestamp(Timestamp.getDefaultInstance());
        claimBuilder.setProofFormat(ByteString.copyFromUtf8("1"));
        claimBuilder.setValidityTerm(0);

        byte[] linkpk;
        if (inboxItemOtherPeer != null) {
            linkpk = inboxItemOtherPeer.getPeer().getPublicKeyPair().toBytes();
        } else {
            linkpk = new byte[32];
        }

        final MessageProto.TrustChainBlock block = createBlock(
                transactionData,
                "",
                new TrustChainDBHelper(this),
                publicKey,
                null,
                linkpk,
                claimBuilder.build());
        final MessageProto.TrustChainBlock signedBlock = TrustChainBlockHelper.sign(block, Key.loadKeys(getApplicationContext()).getSigningKey());

        messageEditText.setText("");
        messageEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        // insert the half block in your own chain
        return signedBlock;
    }

    /**
     * Called when the user presses the Send button.
     * Creates the new block, inserts it in the database and starts the sending activity.
     * @param view unused
     */
    public void onClickSend(View view) {
        MessageProto.TrustChainBlock signedBlock = createClaimBlock();
        if (signedBlock == null) {
            return;
        }
        new TrustChainDBHelper(this).insertInDB(signedBlock);
        Intent claimIntent = new Intent(this, SendOfflineActivity.class);
        claimIntent.putExtra("claimBlock", signedBlock);
        startActivity(claimIntent);
    }
}
