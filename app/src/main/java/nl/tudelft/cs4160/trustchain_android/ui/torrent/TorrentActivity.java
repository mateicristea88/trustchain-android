package nl.tudelft.cs4160.trustchain_android.ui.torrent;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private ProgressBar metadataProgressBar;
    private ProgressBar downloadProgressBar;
    private EditText magnetLinkField;
    private Button downloadButton;
    private TextView progressText;
    private TextView piecesText;
    private TextView pieceSizeText;
    private RecyclerView recyclerView;

    private File downloadLocation;
    private TorrentFilesAdapter adapter = new TorrentFilesAdapter();

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
        recyclerView = findViewById(R.id.recycler_view);

        uiHandler = new Handler();
        downloadLocation = getFilesDir();

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            magnetLinkField.setText(intent.getData().toString());
        } else {
            String sampleMagnetLink = "magnet:?xt=urn:btih:aaa24996c7fce10a3a9fe8808047bffc3cdec161&dn=Kanye+West+-+JESUS+IS+KING+%282019%29+Mp3+%28320kbps%29+%5BHunter%5D+&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Fopen.demonii.com%3A1337&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Fexodus.desync.com%3A6969";
            magnetLinkField.setText(sampleMagnetLink);
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

    private void startTorrentDownload(String link) {
        metadataProgressBar.setVisibility(View.VISIBLE);

        Uri torrentUri = Uri.parse(link);

        TorrentSessionOptions torrentSessionOptions = new TorrentSessionOptions(downloadLocation);

        TorrentSession torrentSession = new TorrentSession(torrentSessionOptions);

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

            }

            @Override
            public void onMetadataFailed(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onMetadataFailed");
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
