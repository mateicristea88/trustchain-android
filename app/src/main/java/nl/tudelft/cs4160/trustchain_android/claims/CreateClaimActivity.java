package nl.tudelft.cs4160.trustchain_android.claims;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.util.FileDialog;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.createBlock;

public class CreateClaimActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSIONS = 1;
    private static final String TAG = CreateClaimActivity.class.getName();
    private EditText messageEditText;
    private InboxItem inboxItemOtherPeer;
    private File claimFile;
    private TextView selectedFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_claim);

        messageEditText = findViewById(R.id.message_edit_text);
        inboxItemOtherPeer = (InboxItem) getIntent().getSerializableExtra("peer");
        selectedFilePath = findViewById(R.id.selected_path);
    }

    public MessageProto.TrustChainBlock createClaimBlock() throws UnsupportedEncodingException {
        byte[] publicKey = Key.loadKeys(this).getPublicKeyPair().toBytes();
        byte[] transactionData;

        if (claimFile != null) {
            int size = (int) claimFile.length();
            transactionData = new byte[size];

            BufferedInputStream inputstream;
            try {
                inputstream = new BufferedInputStream(new FileInputStream(claimFile));
                inputstream.read(transactionData, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            transactionData = messageEditText.getText().toString().getBytes("UTF-8");
        }
        MessageProto.TrustChainBlock.Claim.Builder claimBuilder = MessageProto.TrustChainBlock.Claim.newBuilder();
        claimBuilder.setName(ByteString.copyFromUtf8("claim"));
        claimBuilder.setTimestamp(Timestamp.getDefaultInstance());
        claimBuilder.setProofFormat(ByteString.copyFromUtf8("1"));
        claimBuilder.setValidityTerm(0);
        if (claimFile != null) {
            claimBuilder.setPayloadType(claimFile.getName().substring(claimFile.getName().lastIndexOf('.')));
        } else {
            claimBuilder.setPayloadType("rawtxt");
        }

        final MessageProto.TrustChainBlock block = createBlock(
                transactionData,
                new TrustChainDBHelper(this),
                publicKey,
                null,
                inboxItemOtherPeer.getPublicKeyPair().toBytes(),
                claimBuilder.build());
        final MessageProto.TrustChainBlock signedBlock = TrustChainBlockHelper.sign(block, Key.loadKeys(getApplicationContext()).getSigningKey());

        messageEditText.setText("");
        messageEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        // insert the half block in your own chain
        return signedBlock;
    }

    public void onClickChooseFile(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
//        FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(getApplicationContext());
//        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
//            @Override
//            public void onFileSelected(File file, String path) {
//                claimFile = file;
//                selectedFilePath.setText(path);
//            }
//        });
////        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.IMAGE_ONLY);
//        fileListerDialog.show();

        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                claimFile = file;
                selectedFilePath.setText(file.getPath());
            }
        });
        //fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
        //  public void directorySelected(File directory) {
        //      Log.d(getClass().getName(), "selected dir " + directory.toString());
        //  }
        //});
        //fileDialog.setSelectDirectoryOption(false);
        fileDialog.showDialog();
    }

    public void onClickSend(View view) throws UnsupportedEncodingException {
        MessageProto.TrustChainBlock signedBlock = createClaimBlock();
        if (signedBlock == null) {
            return;
        }
        if (signedBlock.toByteArray().length > 1048576) {
            // If signed block is bigger than 1MB its too big for intent extras.
            selectedFilePath.setError("Too big!");
            return;
        } else {
            selectedFilePath.setError(null);
        }
        new TrustChainDBHelper(this).insertInDB(signedBlock);
        Intent claimIntent = new Intent(this, SendClaimActivity.class);
        claimIntent.putExtra("claimBlock", signedBlock);
        startActivity(claimIntent);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            // If API 23 or up onRequestPermissionsResult is handled by this fragment, otherwise this method will be called in the Activity
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            finish();
                        } else {
                            onClickChooseFile(null);
                        }
                        break;
                    default:
                        Log.w(TAG, "Callback for unknown permission: " + permissions[i]);
                        break;

                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
