package nl.tudelft.cs4160.trustchain_android.chainExplorer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.TrustChainActivity;

public class SendClaimActivity extends AppCompatActivity {

    private static final String TAG = SendClaimActivity.class.toString();
    private static final String TITLE = "Send claim";
    NfcAdapter mNfcAdapter;
    // Flag to indicate that Android Beam is available
    boolean mAndroidBeamAvailable  = false;
    // List of URIs to provide to Android Beam
    private Uri[] mFileUris = new Uri[10];
    // Instance that returns available files from this app
    private FileUriCallback mFileUriCallback;

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
        setTitle(TITLE);
       /*
         * Instantiate a new FileUriCallback to handle requests for
         * URIs
         */
//       mFileUriCallback = new FileUriCallback();
//       // Set the dynamic callback for URI requests.
//       mNfcAdapter.setBeamPushUrisCallback(mFileUriCallback,this);
       final Activity thisActivity = this;
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   NdefMessage message = new NdefMessage("hello testing".getBytes());
                   Log.e(TAG, "starting beam send");
//                   Uri dummydata = Uri.parse("content:///" + "hello testing");
//                   mNfcAdapter.setBeamPushUris(new Uri[]{ dummydata }, thisActivity);

               } catch (Exception e) {
                    e.printStackTrace();
               }
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
                .setMessage("NFC is not supported on this device, claims cannot be sent")
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    /**
     * Callback that Android Beam file transfer calls to get
     * files to share
     */
    private class FileUriCallback implements
            NfcAdapter.CreateBeamUrisCallback {
        public FileUriCallback() {
        }

        /**
         * Create content URIs as needed to share with another device
         */
        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            return mFileUris;
        }
    }
}
