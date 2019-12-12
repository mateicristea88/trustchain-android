package nl.tudelft.cs4160.trustchain_android.ui.torrent;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.AlertType;
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert;
import com.frostwire.jlibtorrent.alerts.PieceFinishedAlert;
import com.masterwok.simpletorrentandroid.TorrentSession;
import com.masterwok.simpletorrentandroid.TorrentSessionOptions;
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener;
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;

public class TorrentActivity extends AppCompatActivity {
    private Handler uiHandler;

    private View metadataProgressBar;
    private ProgressBar downloadProgressBar;
    private EditText magnetLinkField;
    private Button downloadButton;
    private TextView progressText;
    private TextView piecesText;
    private TextView pieceSizeText;
    private View downloadProgressInfo;
    private RecyclerView recyclerView;

    private File downloadLocation;
    private TorrentFilesAdapter adapter = new TorrentFilesAdapter();

    private TorrentSession torrentSession;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_torrent);

        metadataProgressBar = findViewById(R.id.metadata_progress);
        downloadProgressBar = findViewById(R.id.download_progress);
        magnetLinkField = findViewById(R.id.magnet_link);
        downloadButton = findViewById(R.id.btn_download);
        progressText = findViewById(R.id.txt_progress);
        piecesText = findViewById(R.id.txt_pieces);
        pieceSizeText = findViewById(R.id.txt_piece_size);
        downloadProgressInfo = findViewById(R.id.download_progress_info);
        recyclerView = findViewById(R.id.recycler_view);

        uiHandler = new Handler();
        downloadLocation = new File(getFilesDir(), "torrents");
        downloadLocation.mkdirs();

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            magnetLinkField.setText(intent.getData().toString());
        }

        downloadButton.setOnClickListener(v -> startTorrentDownload(magnetLinkField.getText().toString()));

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(path -> {
            File file = new File(downloadLocation, path);
            String contentType = URLConnection.guessContentTypeFromName(file.getName());
            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(),
                    getApplicationContext().getPackageName() + ".provider", file);
            intent1.setDataAndType(uri, contentType);
            intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent1);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_torrent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_torrents:
                deleteRecursively(downloadLocation);
                Toast.makeText(getApplicationContext(), "Torrents cleared", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    @Override
    protected void onDestroy() {
        stopTorrentSession();
        super.onDestroy();
    }

    private void stopTorrentSession() {
        if (torrentSession != null) {
            torrentSession.stop();
            torrentSession = null;
        }
    }

    private void startTorrentDownload(String link) {
        if (link.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Magnet link is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        stopTorrentSession();

        TorrentSessionOptions torrentSessionOptions = new TorrentSessionOptions(downloadLocation);
        torrentSession = new TorrentSession(torrentSessionOptions);

        torrentSession.setListener(new TorrentSessionListener() {
            @Override
            public void onPieceFinished(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onPieceFinished");
                Log.d("TorrentActivity", "downloaded pieces: " + torrentSessionStatus.getTorrentSessionBuffer().getDownloadedPieceCount());

                float torrentProgress = torrentSessionStatus.getProgress();

                Log.d("TorrentActivity", torrentSessionStatus.getTorrentSessionBuffer().toString());
                int downloadedPieces = torrentSessionStatus.getTorrentSessionBuffer().getDownloadedPieceCount();
                int numPieces = torrentHandle.torrentFile().numPieces();
                int pieceLength = torrentHandle.torrentFile().pieceLength();

                uiHandler.post(() -> {
                    int percentage = (int) (torrentProgress * 100);
                    progressText.setText(percentage + "%");
                    pieceSizeText.setText("Piece Size: " + pieceLength + " B");
                    piecesText.setText(downloadedPieces + "/" + numPieces);
                    downloadProgressBar.setVisibility(View.VISIBLE);
                    downloadProgressBar.setProgress(percentage);
                    downloadProgressInfo.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onAddTorrent(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onAddTorrent");
            }

            @Override
            public void onTorrentError(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onTorrentError");
            }

            @Override
            public void onTorrentFinished(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onTorrentFinished");
            }

            @Override
            public void onMetadataFailed(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onMetadataFailed");
                Toast.makeText(getApplicationContext(), "Fetching metadata failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMetadataReceived(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onMetadataReceived");
                List<String> paths = torrentHandle.torrentFile().files().paths();
                Log.d("TorrentActivity", "torrent:" + torrentHandle.name());
                Log.d("TorrentActivity", "size:" + torrentHandle.torrentFile().totalSize());
                Log.d("TorrentActivity", "pieces:" + torrentHandle.torrentFile().numPieces());
                Log.d("TorrentActivity", "infohash:" + torrentHandle.torrentFile().infoHash());
                Log.d("TorrentActivity", "origfiles:" + torrentHandle.torrentFile().origFiles().numFiles());
                Log.d("TorrentActivity", "files:" + torrentHandle.torrentFile().files().numFiles());
                FileStorage files = torrentHandle.torrentFile().files();
                uiHandler.post(() -> showMetadata(files));
            }

            @Override
            public void onTorrentDeleteFailed(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onTorrentPaused(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onTorrentDeleted(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onTorrentRemoved(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onTorrentResumed(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onBlockUploaded(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }
        });

        metadataProgressBar.setVisibility(View.VISIBLE);
        downloadProgressInfo.setVisibility(View.GONE);
        downloadProgressBar.setVisibility(View.GONE);

        Uri torrentUri = Uri.parse(link);

        AsyncTask.execute(() -> {
            torrentSession.start(getApplicationContext(), torrentUri); // Invoke on background thread.
        });
    }

    private void showMetadata(FileStorage files) {
        metadataProgressBar.setVisibility(View.GONE);
        adapter.setFileStorage(files);
        adapter.notifyDataSetChanged();
    }
}
