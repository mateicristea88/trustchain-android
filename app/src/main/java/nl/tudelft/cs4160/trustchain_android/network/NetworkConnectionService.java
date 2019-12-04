package nl.tudelft.cs4160.trustchain_android.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.TrustchainApplication;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.ui.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.peer.PeerListener;
import nl.tudelft.cs4160.trustchain_android.statistics.StatisticsServer;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.BootstrapIPStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

/**
 * A foreground service that maintains the network connections.
 */
public class NetworkConnectionService extends Service {
    // The server ip address, this is the bootstrap phone that's always running
    public static final String CONNECTABLE_ADDRESS = "130.161.211.254";

    private static final String TAG = "ConnectionService";
    private final static int BUFFER_SIZE = 65536;
    public final static int DEFAULT_PORT = 1873;
    private static final String NOTIFICATION_CHANNEL = "service_notifications";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    @Inject
    BlockRepository blockRepository;

    private Network network;
    private PeerHandler peerHandler;
    private boolean networkRunning = true;
    private String wan = "";

    private Handler uiHandler;
    private boolean isBound;
    private String connectionType;

    private NetworkStatusListener networkStatusListener = new NetworkStatusListener() {
        @Override
        public void updateInternalSourceAddress(String address) {
            notifyListeners(listener -> listener.updateInternalSourceAddress(address));
        }

        /**
         * Update the showed inboxItem lists.
         * First split into new peers and the active list
         * Then remove the peers that aren't responding for a long time.
         */
        @Override
        public void updatePeerLists() {
            Log.d(TAG, "updatePeerList");
            peerHandler.removeDeadPeers();
            peerHandler.splitPeerList();
            notifyListeners(listener -> listener.updatePeerLists(
                    peerHandler.getActivePeersList(), peerHandler.getNewPeersList()));
        }

        /**
         * Update wan address
         * @param message a message that was received, the destination is our wan address
         */
        @Override
        public void updateWan(MessageProto.Message message) throws UnknownHostException {
            InetAddress addr = InetAddress.getByAddress(message.getDestinationAddress().toByteArray());
            int port = message.getDestinationPort();
            InetSocketAddress socketAddress = new InetSocketAddress(addr, port);

            if (peerHandler.getWanVote().vote(socketAddress)) {
                wan = peerHandler.getWanVote().getAddress().toString();

                String address = wan.replace("/","");

                notifyListeners(listener -> listener.updateWan(address));
            }
        }

        @Override
        public void updateConnectionType(String connectionTypeStr) {
            if (connectionType == null || !connectionType.equals(connectionTypeStr)) {
                showForegroundNotification();
            }
            notifyListeners(listener -> listener.updateConnectionType(connectionTypeStr));
        }

        @Override
        public PeerHandler getPeerHandler() {
            return peerHandler;
        }

        @Override
        public String getName() {
            return UserNameStorage.getUserName(getBaseContext());
        }
    };

    private PeerListener peerListener = new PeerListener() {
        @Override
        public void updateActivePeers() {
            Log.d(TAG, "updateActivePeers");
            notifyListeners(listener -> listener.updatePeerLists(peerHandler.getActivePeersList(),
                    peerHandler.getNewPeersList()));
        }

        @Override
        public void updateNewPeers() {
            Log.d(TAG, "updateNewPeers");
            notifyListeners(listener -> listener.updatePeerLists(peerHandler.getActivePeersList(),
                    peerHandler.getNewPeersList()));
        }
    };

    private List<WeakReference<NetworkConnectionListener>> networkConnectionListeners = new ArrayList<>();

    @Override
    public void onCreate() {
        ((TrustchainApplication) getApplicationContext()).appComponent.inject(this);
        super.onCreate();

        uiHandler = new Handler();

        initKey();
        StatisticsServer.getInstance().start(networkStatusListener);

        peerHandler = new PeerHandler(Key.loadKeys(this).getPublicKeyPair(),
                UserNameStorage.getUserName(this));
        peerHandler.setPeerListener(peerListener);

        network = Network.getInstance(getApplicationContext());
        network.getMessageHandler().setPeerHandler(peerHandler);
        network.setNetworkStatusListener(networkStatusListener);

        createNotificationChannel();
        showForegroundNotification();

        addInitialPeer();
        startListenThread();
        startSendThread();
    }

    @Override
    public void onDestroy() {
        network.closeChannel();
        networkRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        showForegroundNotification();
        return new LocalBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        isBound = true;
        showForegroundNotification();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        showForegroundNotification();
        return true;
    }

    public void addNetworkConnectionListener(NetworkConnectionListener listener) {
        networkConnectionListeners.add(new WeakReference<>(listener));

        String connectionType = network.getConnectionTypeString((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        listener.updateConnectionType(connectionType);

        listener.updatePeerLists(peerHandler.getActivePeersList(), peerHandler.getNewPeersList());
    }

    public void removeNetworkConnectionListener(NetworkConnectionListener listener) {
        Iterator<WeakReference<NetworkConnectionListener>> iterator = networkConnectionListeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<NetworkConnectionListener> listenerRef = iterator.next();
            if (listenerRef.get() == listener) {
                iterator.remove();
            }
        }
    }

    /**
     * If the app is launched for the first time
     * a new keyPair is created and saved locally in the storage.
     * If no blocks are present in the database, a genesis block is created.
     */
    private void initKey() {
        DualSecret kp = Key.loadKeys(getApplicationContext());
        if (kp == null) {
            kp = Key.createAndSaveKeys(getApplicationContext());
        }
        int blockCount = blockRepository.getBlockCount();
        if (blockCount == 0) {
            MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(kp);
            blockRepository.insertOrUpdate(block);
        }
    }

    /**
     * Start the thread send thread responsible for sending an introduction request to 10 random peers every 5 seconds as a heartbeat timer.
     * This number is chosen arbitrarily to avoid the app sending too much packets and using too much data keeping connections open with many peers.
     */
    private void startSendThread() {
        Thread sendThread = new Thread(() -> {
            boolean snackbarVisible = false;
            //Toast networkUnreachableToast = Toast.makeText(getBaseContext(), "Network unavailable", Toast.LENGTH_SHORT);

            // wait max one second for the CreateInetSocketAddressTask to finish, indicated by that the bootstrap is added to the peerlist
            int t = 0;
            while(peerHandler.size() == 0 && t < 100) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t++;
            }

            while(!Thread.interrupted() && networkRunning) {
                try {
                    // update connection type and internal ip address

                    String connectionType = network.getConnectionTypeString((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
                    notifyListeners(listener -> listener.updateConnectionType(connectionType));

                    if (!this.connectionType.equals(connectionType)) {
                        showForegroundNotification();
                    }

                    network.showLocalIpAddress();
                    if (peerHandler.size() > 0) {
                        // select 10 random peers to send an introduction request to
                        int limit = 10;
                        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
                        lock.readLock().lock();
                        List<Peer> connectedPeers = new ArrayList<>(peerHandler.getPeerList());
                        lock.readLock().unlock();
                        if(connectedPeers.size() <= limit) {
                            for(Peer peer : connectedPeers){
                                network.sendIntroductionRequest(peer);
                            }
                        } else {
                            Random rand = new Random();
                            for (int i = 0; i < limit; i++) {
                                int index = rand.nextInt(connectedPeers.size());
                                network.sendIntroductionRequest(connectedPeers.get(index));
                                connectedPeers.remove(index);
                            }
                        }
                    }
                    // if the network is reachable again, remove the snackbar
                    if(snackbarVisible) {
                        //networkUnreachableToast.cancel();
                        snackbarVisible = false;
                        Log.i(TAG, "Network reachable again");
                    }
                } catch (SocketException e) {
                    Log.i(TAG, "network unreachable");
                    if(!snackbarVisible) {
                        //networkUnreachableToast.show();
                        snackbarVisible = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Send thread stopped");
        });
        sendThread.start();
        Log.d(TAG, "Send thread started");
    }

    /**
     * Start the listen thread. The thread opens a new {@link DatagramChannel} and calls {@link Network#dataReceived(Context, ByteBuffer,
     * InetSocketAddress)} for each incoming datagram.
     */
    private void startListenThread() {
        final Context context = this;

        Thread listenThread = new Thread(() -> {
            try {
                ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                while (!Thread.interrupted() && networkRunning) {
                    inputBuffer.clear();
                    Log.d(TAG, "Receive");
                    SocketAddress address = network.receive(inputBuffer);
                    Log.d(TAG, "Received from " + address);
                    inputBuffer.flip();
                    network.dataReceived(context, inputBuffer, (InetSocketAddress) address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Listen thread stopped");
        });
        listenThread.start();
        Log.d(TAG, "Listen thread started");
    }

    /**
     * Add the bootstrap to the peerlist.
     */
    public void addInitialPeer() {
        String address = BootstrapIPStorage.getIP(this);
        CreateInetSocketAddressTask createInetSocketAddressTask = new CreateInetSocketAddressTask(peerHandler);
        try {
            if (address != null && !address.equals("")) {
                createInetSocketAddressTask.execute(address, String.valueOf(DEFAULT_PORT));
            } else {
                createInetSocketAddressTask.execute(CONNECTABLE_ADDRESS, String.valueOf(DEFAULT_PORT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyListeners(ListenerNotifier notifier) {
        for (WeakReference<NetworkConnectionListener> listenerRef : networkConnectionListeners) {
            NetworkConnectionListener listener = listenerRef.get();
            if (listener != null) {
                uiHandler.post(() -> notifier.notify(listener));
            }
        }
    }

    private void showForegroundNotification() {
        Intent notificationIntent = new Intent(this, OverviewConnectionsActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent cancelBroadcastIntent = new Intent(this, CancelNetworkConnectionReceiver.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, cancelBroadcastIntent, 0);

        connectionType = network.getConnectionTypeString((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                        .setContentTitle("TrustChain")
                        .setContentText("Connection: " + connectionType)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentIntent(pendingIntent);

        // Allow cancellation when the app is running in background
        if (!isBound) {
            builder.addAction(new NotificationCompat.Action(0, "Stop", cancelPendingIntent));
        }

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Service notifications";
            String description = "Shows when the network connection service is running";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Asynctask to create the inetsocketaddress since network stuff can no longer happen on the main thread in android v3 (honeycomb).
     */
    private static class CreateInetSocketAddressTask extends AsyncTask<String, Void, InetSocketAddress> {
        private PeerHandler peerHandler;

        CreateInetSocketAddressTask(PeerHandler peerHandler) {
            this.peerHandler = peerHandler;
        }

        @Override
        protected InetSocketAddress doInBackground(String... params) {
            InetSocketAddress inetSocketAddress = null;

            try {
                InetAddress connectableAddress = InetAddress.getByName(params[0]);
                int port = Integer.parseInt(params[1]);
                inetSocketAddress = new InetSocketAddress(connectableAddress, port);

                peerHandler.addPeer(inetSocketAddress, null,null);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return inetSocketAddress;
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public NetworkConnectionService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NetworkConnectionService.this;
        }
    }

    interface ListenerNotifier {
        void notify(NetworkConnectionListener listener);
    }
}
