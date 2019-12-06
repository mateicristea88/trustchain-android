package nl.tudelft.cs4160.trustchain_android.ui.torrent;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_torrent);

        uiHandler = new Handler();

        EditText magnetLink = findViewById(R.id.magnet_link);
        Button downloadButton = findViewById(R.id.btn_download);

        String sampleMagnetLink = "magnet:?xt=urn:btih:aaa24996c7fce10a3a9fe8808047bffc3cdec161&dn=Kanye+West+-+JESUS+IS+KING+%282019%29+Mp3+%28320kbps%29+%5BHunter%5D+&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Fopen.demonii.com%3A1337&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Fexodus.desync.com%3A6969";
        magnetLink.setText(sampleMagnetLink);

        downloadButton.setOnClickListener(v -> startTorrentDownload(magnetLink.getText().toString()));
    }

    private void startTorrentDownload(String link) {
        ProgressBar progress = findViewById(R.id.metadataProgress);
        progress.setVisibility(View.VISIBLE);

        Uri torrentUri = Uri.parse(link);

        TorrentSessionOptions torrentSessionOptions = new TorrentSessionOptions(
                //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                getFilesDir(),
                false,
                false,
                false,
                false,
                8,
                0,
                0,
                200,
                10,
                88
        );

        TorrentSession torrentSession = new TorrentSession(torrentSessionOptions);

        torrentSession.setListener(new TorrentSessionListener() {
            @Override
            public void onPieceFinished(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onPieceFinished");
                Log.d("TorrentActivity", "downloaded pieces: " + torrentSessionStatus.getTorrentSessionBuffer().getDownloadedPieceCount());
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView downloadedView = findViewById(R.id.downloadedPieces);
                        //int downloadedCount = torrentSessionStatus.getTorrentSessionBuffer().getDownloadedPieceCount();
                        //Log.d("TorrentActivity", torrentSessionStatus.getTorrentSessionBuffer().toString());
                        //int numPieces = torrentHandle.torrentFile().numPieces();
                        float torrentProgress = torrentSessionStatus.getProgress();
                        //downloadedView.setText("Progress: " + torrentSessionStatus.getProgress() + " (" + downloadedCount + "/" + numPieces + ")");
                        downloadedView.setText("Progress: " + torrentProgress);
                        ProgressBar downloadProgressBar = findViewById(R.id.downloadProgress);
                        downloadProgressBar.setVisibility(View.VISIBLE);
                        downloadProgressBar.setProgress((int) (torrentProgress * 100));
                    }
                });
            }

            @Override
            public void onAddTorrent(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {
                Log.d("TorrentActivity", "onAddTorrent");
            }

            @Override
            public void onTorrentError(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onTorrentFinished(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

            }

            @Override
            public void onMetadataFailed(TorrentHandle torrentHandle, TorrentSessionStatus torrentSessionStatus) {

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
        ProgressBar progress = findViewById(R.id.metadataProgress);
        progress.setVisibility(View.GONE);

        LinearLayout container = findViewById(R.id.container);
        container.removeAllViews();

        /*
        Log.d("TorrentActivity", "torrent:" + torrentHandle.name());
        Log.d("TorrentActivity", "size:" + torrentHandle.torrentFile().totalSize());
        Log.d("TorrentActivity", "pieces:" + torrentHandle.torrentFile().numPieces());
        Log.d("TorrentActivity", "infohash:" + torrentHandle.torrentFile().infoHash());
        Log.d("TorrentActivity", "origfiles:" + torrentHandle.torrentFile().origFiles().numFiles());
        Log.d("TorrentActivity", "files:" + torrentHandle.torrentFile().files().numFiles());
         */
        for (int i = 0; i < files.numFiles(); i++) {
            String path = files.fileName(i);
            long offset = files.fileOffset(i);
            long size = files.fileSize(i);
            Log.d("TorrentActivity", "file " + i + ": " + path + ", " + offset + ", " + size);
            TextView itemView = new TextView(this);
            itemView.setText(path);
            container.addView(itemView);
        }

    }

    /*
    private SessionManager sessionManager = new SessionManager(false);

    private final Object dhtLock = new Object();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                Log.d("TorrentActivity", "alert: " + alert.type() + " " + alert.message() + " " + alert);

                switch (alert.type()) {
                    case ADD_TORRENT:
                        AddTorrentAlert addTorrentAlert = (AddTorrentAlert) alert;
                        TorrentHandle handle = addTorrentAlert.handle();
                        FileStorage fileStorage = handle.torrentFile().files();
                        List<String> paths = fileStorage.paths();
                        for (String path : paths) {
                            Log.d("TorrentActivity", "metadata file: " + path);
                        }
                        handle.resume();
                        break;
                    case METADATA_RECEIVED:
                        MetadataReceivedAlert metadataReceivedAlert = (MetadataReceivedAlert) alert;
                        break;
                    case PIECE_FINISHED:
                        PieceFinishedAlert pieceFinishedAlert = (PieceFinishedAlert) alert;
                        int pieceIndex = pieceFinishedAlert.pieceIndex();
                        int pieces = pieceFinishedAlert.handle().torrentFile().numPieces();
                        Log.d("TorrentActivity", "piece finished:" + pieceIndex + " / " + pieces);
                        long[] progress = pieceFinishedAlert.handle().fileProgress();
                        Log.d("TorrentActivity", "progress:" + Arrays.toString(progress));
                        break;
                    case DHT_BOOTSTRAP:
                        synchronized (dhtLock) {
                            dhtLock.notify();
                        }
                        break;
                    case DHT_STATS:
                        synchronized (dhtLock) {
                            if (isDhtReady()) {
                                dhtLock.notify();
                            }
                        }
                        break;
                }
            }
        });

        setContentView(R.layout.activity_torrent);

        EditText magnetLink = findViewById(R.id.magnet_link);
        Button downloadButton = findViewById(R.id.btn_download);

        downloadButton.setOnClickListener(v -> startTorrentDownload(magnetLink.getText().toString()));

        String sampleMagnetLink = "magnet:?xt=urn:btih:aaa24996c7fce10a3a9fe8808047bffc3cdec161&dn=Kanye+West+-+JESUS+IS+KING+%282019%29+Mp3+%28320kbps%29+%5BHunter%5D+&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Fopen.demonii.com%3A1337&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Fexodus.desync.com%3A6969";
        magnetLink.setText(sampleMagnetLink);
        //String sampleTorrentFile = "https://www.frostclick.com/torrents/video/animation/Big_Buck_Bunny_1080p_surround_frostclick.com_frostwire.com.torrent";
        //magnetLink.setText(sampleTorrentFile);
    }

    private boolean isDhtReady() {
        // TODO: check minimum dht nodes
        return sessionManager.stats().dhtNodes() >= 1;
    }

    private void startTorrentDownload(String link) {
        AsyncTask.execute(() -> {
            if (!sessionManager.isRunning()) {
                SessionParams params = new SessionParams(new SettingsPack());
                sessionManager.start(params);
            }

            if (link.startsWith("magnet")) {
                try {
                    synchronized (dhtLock) {
                        Log.d("TorrentActivity", "waiting for dht lock");
                        if (!isDhtReady()) {
                            dhtLock.wait();
                        }
                        Log.d("TorrentActivity", "dht is ready");
                        downloadMagnetLink(link);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                downloadTorrentFile(link);
            }
        });

    }

    private void downloadMagnetLink(String magnetLink) {
        sessionManager.download(magnetLink, getFilesDir());
    }

    private void downloadTorrentFile(String torrentFile) {
        //String torrentFile = "https://now.bt.co/download/torrents/54f90f31025e930500cd5e6e.torrent";
        //String torrentFile = "http://www.frostclick.com/torrents/video/animation/Big_Buck_Bunny_1080p_surround_frostclick.com_frostwire.com.torrent";
        //Uri torrentUri = Uri.parse(torrentFile);
        try {
            URL url = new URL(torrentFile);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.connect();


            if (connection.getResponseCode() != 200) {
                Log.e("TorrentActivity", "response code: " + connection.getResponseCode());
                return;
            }

            byte[] bytes = readInputStreamBytes(connection.getInputStream());

            TorrentInfo torrentInfo = TorrentInfo.bdecode(bytes);
            //sessionManager.download(magnetLink, File.createTempFile("temp", "torrent"));
            sessionManager.download(torrentInfo, getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readInputStreamBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }
     */
}
