package nl.tudelft.cs4160.trustchain_android.claims;

import android.content.DialogInterface;
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
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.funds.qr.QRGenerator;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static android.nfc.NdefRecord.createMime;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SendClaimActivity extends AppCompatActivity implements OnNdefPushCompleteCallback, NfcAdapter.CreateBeamUrisCallback {

    private static final String TAG = SendClaimActivity.class.toString();
    private static final String TITLE = "Send claim";
    NfcAdapter mNfcAdapter;

    private TextView sendClaimText;
    private ImageView QRImage;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_send_claim);

        View layout = findViewById(R.id.layout_send_claim);
        sendClaimText = findViewById(R.id.send_claim_text);
        QRImage = findViewById(R.id.qr_image);

        // NFC isn't available on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC isn't supported
           showNotsupportedMessage();
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
            }
        }

        setTitle(TITLE);
        // Register callback
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        mNfcAdapter.setNdefPushMessage(createNdefMessage(), this);

        sendClaimText.setText(R.string.sending_claim);

        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        invokeBeam();
//                    }
//                }, 10);
            }
        });

//     QR Code generation -> move out of oncreate!
       QRCodeWriter writer = new QRCodeWriter();
       HashMap hints = new HashMap();
       hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
       hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
       DisplayMetrics metrics = new DisplayMetrics();
       getWindowManager().getDefaultDisplay().getMetrics(metrics);
       int size = metrics.widthPixels;

       MessageProto.TrustChainBlock block = (MessageProto.TrustChainBlock) getIntent().getSerializableExtra("claimBlock");
       try {
           Log.e(TAG, block.toByteArray().length + " bytes");
           BitMatrix matrix = writer.encode(new String(block.toByteArray(), UTF_8), BarcodeFormat.QR_CODE, size, size, hints);
           Bitmap image = QRGenerator.GenerateQRCode(size, matrix);
           QRImage.setImageBitmap(image);
       } catch (WriterException e) {
           e.printStackTrace();
       }
   }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        findViewById(R.id.layout_send_claim).postDelayed(new Runnable() {
            @Override
            public void run() {
                invokeBeam();
            }
        }, 100);
    }

    private void invokeBeam() {
        boolean success = mNfcAdapter.invokeBeam(this);
        Log.i(TAG, "beam transfer invoke succes: " + success);
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

    public NdefMessage createNdefMessage() {
        MessageProto.TrustChainBlock block = (MessageProto.TrustChainBlock) getIntent().getSerializableExtra("claimBlock");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendClaimText.setText(R.string.sending_claim_complete);
            }
        });
    }

    @Override
    public Uri[] createBeamUris(NfcEvent nfcEvent) {
       return new Uri[0];
    }
}
