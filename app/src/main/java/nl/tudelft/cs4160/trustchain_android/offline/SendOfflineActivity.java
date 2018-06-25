package nl.tudelft.cs4160.trustchain_android.offline;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.funds.qr.QRGenerator;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static android.nfc.NdefRecord.createMime;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class SendOfflineActivity extends AppCompatActivity implements OnNdefPushCompleteCallback, NfcAdapter.CreateBeamUrisCallback {

    public static final int MAX_QR_SIZE_BYTES = 2953;
    private static final String TAG = SendOfflineActivity.class.toString();
    private NfcAdapter mNfcAdapter;
    private ImageView QRImage;
    private ProgressBar QRProgress;
    private Button sendQR;
    private Button sendBeam;
    private Button receiveCompleted;
    private Button returnHome;

    private MessageProto.TrustChainBlock block;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_offline);

        block = (MessageProto.TrustChainBlock) getIntent().getSerializableExtra("block");

        View layout = findViewById(R.id.layout_send_claim);
        QRImage = findViewById(R.id.qr_image);
        QRProgress = findViewById(R.id.qr_progress);
        sendQR = findViewById(R.id.send_qr);
        sendBeam = findViewById(R.id.send_beam);
        receiveCompleted = findViewById(R.id.receive_completed);
        returnHome = findViewById(R.id.return_home);
        returnHome.setOnClickListener(view -> {
            Intent i = new Intent(this, OverviewConnectionsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        if (getIntent().getBooleanExtra("return", true)) {
            receiveCompleted.setOnClickListener(view -> {
                Intent intent = new Intent(this, ReceiveOfflineActivity.class);
                intent.putExtra("return", false);
                startActivity(intent);
            });
        } else {
            receiveCompleted.setVisibility(View.GONE);
        }

        // NFC isn't available on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC isn't supported
            showNotsupportedMessage();
            sendBeam.setEnabled(false);
            Toast.makeText(this, "NFC is not supported on your device.", Toast.LENGTH_LONG).show();
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
                sendBeam.requestFocus();
                sendBeam.setEnabled(false);
                sendBeam.setFocusableInTouchMode(true);
                sendBeam.setError(getString(R.string.enable_nfc));
            }
        }

        setTitle(getString(R.string.title_activity_send_offline));
        // Register callback
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        mNfcAdapter.setNdefPushMessage(createNdefMessage(), this);

        if (block.toByteArray().length > MAX_QR_SIZE_BYTES) {
            sendQR.requestFocus();
            sendQR.setFocusableInTouchMode(true);
            sendQR.setError(getString(R.string.too_big_for_qr, block.toByteArray().length, MAX_QR_SIZE_BYTES));
            sendQR.setEnabled(false);
        }
    }

    public void invokeBeam(View v) {
        boolean success = mNfcAdapter.invokeBeam(this);
        Log.v(TAG, "beam transfer invoke succes: " + success);
    }

    public void showQRCode(View v) {
        QRProgress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            QRCodeWriter writer = new QRCodeWriter();
            HashMap hints = new HashMap();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int size = metrics.widthPixels;

            Log.e(TAG, block.toByteArray().length + " bytes");
            try {
                BitMatrix matrix = writer.encode(new String(block.toByteArray(), ISO_8859_1), BarcodeFormat.QR_CODE, size, size, hints);
                final Bitmap image = QRGenerator.GenerateQRCode(size, matrix);
                runOnUiThread(() -> QRImage.setImageBitmap(image));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runOnUiThread(() -> {
                    QRProgress.setVisibility(View.GONE);
                    if (getIntent().getBooleanExtra("return", true)) {
                        receiveCompleted.setVisibility(View.VISIBLE);
                    } else {
                        returnHome.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
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
                .setNeutralButton(android.R.string.ok, (dialog, which) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public NdefMessage createNdefMessage() {
        MessageProto.TrustChainBlock block = (MessageProto.TrustChainBlock) getIntent().getSerializableExtra("block");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[]{createMime(
                        "application/nl.tudelft.cs4160.trustchain_android", block.toByteArray())
                        // Including the AAR guarantees that the specified application runs when it
                        // receives a push.
                        , NdefRecord.createApplicationRecord(getApplicationContext().getPackageName())
                });
        return msg;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Log.d(TAG, "Beam transfer complete");
        Activity thisActivity = this;
        runOnUiThread(() -> {
            if (getIntent().getBooleanExtra("return", true)) {
                Intent intent = new Intent(thisActivity, ReceiveOfflineActivity.class);
                intent.putExtra("return", false);
                startActivity(intent);
            } else {
                returnHome.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public Uri[] createBeamUris(NfcEvent nfcEvent) {
        return new Uri[0];
    }
}
