package nl.tudelft.cs4160.trustchain_android.ui.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.ui.chainexplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.funds.FundsActivity;
import nl.tudelft.cs4160.trustchain_android.funds.qr.ExportWalletQRActivity;
import nl.tudelft.cs4160.trustchain_android.funds.qr.ScanQRActivity;
import nl.tudelft.cs4160.trustchain_android.ui.inbox.InboxActivity;
import nl.tudelft.cs4160.trustchain_android.ui.changebootstrap.ChangeBootstrapActivity;
import nl.tudelft.cs4160.trustchain_android.offline.ReceiveOfflineActivity;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.camera.CameraActivity;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.NetworkConnectionListener;
import nl.tudelft.cs4160.trustchain_android.network.NetworkConnectionService;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.SharedPreferencesStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.RequestCode;

import static nl.tudelft.cs4160.trustchain_android.ui.userconfiguration.UserConfigurationActivity.VERSION_NAME_KEY;

public class OverviewConnectionsActivity extends AppCompatActivity implements NetworkConnectionListener {
    private static final String TAG = "OverviewConnections";

    private PeerListAdapter activePeersAdapter;
    private PeerListAdapter newPeersAdapter;
    private TextView activePeersText;
    private TextView newPeersText;
    private NetworkConnectionService service;

    private List<Peer> activePeersList = new ArrayList<>();
    private List<Peer> newPeersList = new ArrayList<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            NetworkConnectionService.LocalBinder binder = (NetworkConnectionService.LocalBinder) iBinder;
            service = binder.getService();
            service.addNetworkConnectionListener(OverviewConnectionsActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    /**
     * Initialize views, start send and receive threads if necessary.
     * Start a thread that refreshes the peers every second.
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_connections);
        initTextViews();
        initExitButton();
        initPeerLists();

        Runnable refreshTask = () -> {
            while(true) {
                runOnUiThread(() -> {
                    activePeersAdapter.notifyDataSetChanged();
                    newPeersAdapter.notifyDataSetChanged();
                });
                try {
                    // update every 498 ms, because we want to display a sent/received message cue when a message was received less than 500ms ago.
                    Thread.sleep(498);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(refreshTask).start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(this, NetworkConnectionService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        service.removeNetworkConnectionListener(this);
        unbindService(serviceConnection);
    }

    /**
     * Get stored information and display it in the correct text views.
     */
    private void initTextViews() {
        ((TextView) findViewById(R.id.peer_id)).setText(UserNameStorage.getUserName(this));
        String versionName = "unknown";
        try {
            versionName = SharedPreferencesStorage.readSharedPreferences(this, VERSION_NAME_KEY, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((TextView )findViewById(R.id.version)).setText(getString(R.string.version, versionName));
        activePeersText = findViewById(R.id.active_peers_text);
        newPeersText = findViewById(R.id.new_peers_text);
    }

    /**
     * Inflates the menu with a layout.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Define what should be executed when one of the item in the menu is clicked.
     *
     * @param item the item in the menu.
     * @return true if everything was executed.- [ ] No out-of-sleep feature on Android. dead overlay.
- [ ] update on_packet() every second a screen refresh and update message-timeout values on screen.
- [ ] design and implement a fault-resilient overlay. make flawless.
- [ ] documented algorithm
- [ ] Add last send message + got last response message
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chain_menu:
                Intent chainExplorerActivity = new Intent(this, ChainExplorerActivity.class);
                startActivity(chainExplorerActivity);
                return true;
            case R.id.receive_offline:
                Intent intent = new Intent(this, ReceiveOfflineActivity.class);
                intent.putExtra("return", true);
                startActivity(intent);
                return true;
            case R.id.connection_explanation_menu:
                Intent ConnectionExplanationActivity = new Intent(this, nl.tudelft.cs4160.trustchain_android.ui.connectionexplanation.ConnectionExplanationActivity.class);
                startActivity(ConnectionExplanationActivity);
                return true;
            case R.id.import_tokens:
                startActivity(new Intent(OverviewConnectionsActivity.this, ScanQRActivity.class));
                return true;
            case R.id.export_tokens:
                startActivity(new Intent(OverviewConnectionsActivity.this, ExportWalletQRActivity.class));
                return true;
            case R.id.funds:
                startActivity(new Intent(this, FundsActivity.class));
                return true;
            case R.id.find_peer:
                Intent bootstrapActivity = new Intent(this, ChangeBootstrapActivity.class);
                startActivityForResult(bootstrapActivity, RequestCode.CHANGE_BOOTSTRAP);
                return true;
            case R.id.passport_scan:
                Intent cameraActivity = new Intent(this, CameraActivity.class);
                startActivity(cameraActivity);
                return true;
            default:
                return false;
        }
    }

    /**
     * On click open inbox button open the inbox activity.
     * @param view
     */
    public void onClickOpenInbox(View view) {
        ArrayList<Peer> peerList = new ArrayList<>();
        peerList.addAll(activePeersList);
        peerList.addAll(newPeersList);
        InboxActivity.peerList = peerList;
        Intent inboxActivityIntent = new Intent(this, InboxActivity.class);
        startActivity(inboxActivityIntent);
    }

    /**
     * Initialize the exit button.
     */
    private void initExitButton() {
        Button mExitButton = findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(view -> finish());
    }

    /**
     * Initialize the inboxItem lists.
     */
    private void initPeerLists() {
        ListView connectedPeerConnectionListView = findViewById(R.id.active_peers_list_view);
        ListView incomingPeerConnectionListView = findViewById(R.id.new_peers_list_view);
        CoordinatorLayout content = findViewById(R.id.content);
        activePeersAdapter = new PeerListAdapter(getApplicationContext(), R.layout.item_peer_connection_list, activePeersList, content);
        connectedPeerConnectionListView.setAdapter(activePeersAdapter);
        newPeersAdapter = new PeerListAdapter(getApplicationContext(), R.layout.item_peer_connection_list, newPeersList, content);
        incomingPeerConnectionListView.setAdapter(newPeersAdapter);
    }


    /**
     * This method is the callback when submitting the new bootstrap address.
     * The method is called when leaving the ChangeBootstrapActivity.
     * The filled in ip address is passed on to this method.
     * When the callback of the bootstrap activity is successful
     * set this ip address as ConnectableAddress in the preferences.
     *
     * @param requestCode
     * @param resultCode
     * @param data the data passed on by the previous activity, in this case the ip address
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.CHANGE_BOOTSTRAP && resultCode == Activity.RESULT_OK) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ConnectableAddress", data.getStringExtra("ConnectableAddress"));
            editor.apply();

            if (service != null) {
                service.addInitialPeer();
            }
        }
    }

    /**
     * Set the external ip field based on the WAN vote.
     *
     * @param ip the ip address.
     */
    @Override
    public void updateWan(String ip) {
        TextView mWanVote = findViewById(R.id.wanvote);
        mWanVote.setText(ip);
    }

    /**
     * Display connectionType
     *
     * @param connectionTypeStr String representation of the connection type
     */
    @Override
    public void updateConnectionType(String connectionTypeStr) {
        ((TextView) findViewById(R.id.connection_type)).setText(connectionTypeStr);
    }

    /**
     * Update the source address textview
     * @param address
     */
    @Override
    public void updateInternalSourceAddress(final String address) {
        Log.d(TAG, "Local ip: " + address);
        TextView localIp = findViewById(R.id.local_ip_address_view);
        localIp.setText(address);
    }

    /**
     * Update the showed inboxItem lists.
     * First split into new peers and the active list
     * Then remove the peers that aren't responding for a long time.
     */
    @Override
    public void updatePeerLists(List<Peer> activePeersList, List<Peer> newPeersList) {
        this.activePeersList.clear();
        this.activePeersList.addAll(activePeersList);
        this.newPeersList.clear();
        this.newPeersList.addAll(newPeersList);
        notifyPeerListsChanged();
    }

    /**
     * Update the connected peer adapter by notifying that the data has changed.
     */
    @Override
    public void updateActivePeers(List<Peer> peers) {
        this.activePeersList.clear();
        this.activePeersList.addAll(peers);
        notifyPeerListsChanged();
    }

    /**
     * Update the incoming peer adapter by notifying that the data has changed. Usually when a new
     * peer has been found that we are not connected to yet.
     */
    @Override
    public void updateNewPeers(List<Peer> peers) {
        this.newPeersList.clear();
        this.newPeersList.addAll(peers);
        notifyPeerListsChanged();
    }

    private void notifyPeerListsChanged() {
        activePeersAdapter.notifyDataSetChanged();
        newPeersAdapter.notifyDataSetChanged();
        activePeersText.setText(getString(R.string.left_connections_with_count, activePeersAdapter.getCount()));
        newPeersText.setText(getString(R.string.right_connections_with_count, newPeersAdapter.getCount()));
    }
}