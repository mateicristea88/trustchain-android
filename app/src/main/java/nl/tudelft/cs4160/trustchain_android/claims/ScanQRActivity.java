package nl.tudelft.cs4160.trustchain_android.claims;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import nl.tudelft.cs4160.trustchain_android.R;

/**
 * Class that scans a QR code, then exits providing the result as bytes in an intent.
 * This is a refactor of ScanQRActivity in funds.qr package to be more generic.
 */
public class ScanQRActivity extends AppCompatActivity {
    public static final int PERMISSIONS_REQUEST_CAMERA = 0;
    public static final String TAG = "ScanQRActivity";

    private Vibrator vibrator;
    private ZXingScannerView scannerView;
    private boolean showingPermissionExplanation = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        scannerView = findViewById(R.id.scanner_view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Camera request permission handling
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            startCamera();
        }
    }

    private void requestCameraPermission() {
        if (!showingPermissionExplanation)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                showPermissionExplanation();
            }
        }
    }

    private void showPermissionExplanation() {
        showingPermissionExplanation = true;
        new android.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.qr_camera_permission_explanation))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showingPermissionExplanation = false;
                        requestCameraPermission();
                    }
                })
                .setNegativeButton(R.string.deny_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showingPermissionExplanation = false;
                        finish();
                    }
                })
                .show();
    }


    private ZXingScannerView.ResultHandler scanResultHandler = new ZXingScannerView.ResultHandler() {
        public void handleResult(Result result) {
            vibrator.vibrate(100);
            Intent resultIntent = new Intent();
            String text = result.getText();
            byte[] b = result.getRawBytes();
            resultIntent.putExtra("result", result.getText());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    };

    private void startCamera() {
        scannerView.setResultHandler(scanResultHandler);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }
}

