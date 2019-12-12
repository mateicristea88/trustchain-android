package nl.tudelft.cs4160.trustchain_android.stresstest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.network.NetworkStatusListener;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.peer.PeerListener;
import nl.tudelft.cs4160.trustchain_android.statistics.StatisticsServer;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.storage.repository.PeerRepository;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.BootstrapIPStorage;

public class StressTestPeer implements PeerListener, NetworkStatusListener {

    private static final String TAG = StressTestPeer.class.getName();
    private Network network;
    private PeerHandler peerHandler;
    private String wan = "";
    public final static String CONNECTABLE_ADDRESS = "130.161.211.254";
    public final static int DEFAULT_PORT = 1873;
    private final static int BUFFER_SIZE = 65536;
    private Context context;
    private BlockRepository blockRepository;
    private PeerRepository peerRepository;

    public String userName;
    private DualSecret keyPair;
    private boolean networkRunning;

    int port;

    public void startNode () {
        initVariables();
        addInitialPeer();
        startListenThread();
        startSendThread();
        StatisticsServer.getInstance().start(this);

        Runnable refreshTask = () -> {
            while (true) {
                // TODO: stop this task in stopNode
                peerHandler.removeDeadPeers();
                peerHandler.splitPeerList();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(refreshTask).start();
    }

    public StressTestPeer(Context context, BlockRepository blockRepository, PeerRepository peerRepository, int port) {
        this.context = context;
        this.blockRepository = blockRepository;
        this.peerRepository = peerRepository;
        this.userName = UsernameGenerator.getUsername();
        this.keyPair = Key.createNewKeyPair();
        this.port = port;
    }

    /**
     * Add the initial hard-coded connectable inboxItem to the inboxItem list.
     */
    public void addInitialPeer() {
        String address = BootstrapIPStorage.getIP(context);
        CreateInetSocketAddressTask createInetSocketAddressTask = new CreateInetSocketAddressTask(this);
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

    /**
     * Initialize all local variables
     * If this activity is opened with a saved instance state
     * we load the list of peers from this saved state.
//     * @param savedInstanceState
     */
    private void initVariables() {
        networkRunning = true;
        peerHandler = new PeerHandler(keyPair.getPublicKeyPair(), userName);
        getPeerHandler().setPeerListener(this);

        network = new Network(context, blockRepository, peerRepository, userName, keyPair.getPublicKeyPair(), port);
        network.getMessageHandler().setPeerHandler(getPeerHandler());
        network.setNetworkStatusListener(this);
        updateConnectionType(network.getConnectionTypeString((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)));
    }

    /**
//     * Start the thread send thread responsible for sending a {@link IntroductionRequest} to a random inboxItem every 5 seconds.
     */
    private void startSendThread() {
        Thread sendThread = new Thread(() -> {
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
                    updateConnectionType(network.getConnectionTypeString((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)));
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
                } catch (SocketException e) {
                    Log.i(TAG, "network unreachable");
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
        Thread listenThread = new Thread(() -> {
            try {
                ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                while (!Thread.interrupted() && networkRunning) {
                    inputBuffer.clear();
                    SocketAddress address = network.receive(inputBuffer);
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
     * Update wan address
     * @param message a message that was received, the destination is our wan address
     */
    public void updateWan(MessageProto.Message message) throws UnknownHostException {
        byte[] da = message.getDestinationAddress().toByteArray();
        InetAddress addr = InetAddress.getByAddress(da);
        int port = message.getDestinationPort();
        InetSocketAddress socketAddress = new InetSocketAddress(addr, port);

        if (peerHandler.getWanVote().vote(socketAddress)) {
            wan = peerHandler.getWanVote().getAddress().toString();
        }
    }

    @Override
    public void updateConnectionType(String connectionTypeStr) {
        // No UI -> do nothing
    }

    @Override
    public void updateInternalSourceAddress(String address) {
        // No UI -> do nothing
    }

    /**
     * Return the peer handler object.
     * @return
     */
    @Override
    public PeerHandler getPeerHandler() {
        return peerHandler;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public void updateActivePeers() {

    }

    @Override
    public void updateNewPeers() {

    }

    /**
     * Asynctask to create the inetsocketaddress since network stuff can no longer happen on the main thread in android v3 (honeycomb).
     */
    private static class CreateInetSocketAddressTask extends AsyncTask<String, Void, InetSocketAddress> {
        private WeakReference<StressTestPeer> activityReference;

        CreateInetSocketAddressTask(StressTestPeer context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected InetSocketAddress doInBackground(String... params) {
            InetSocketAddress inetSocketAddress = null;
            StressTestPeer activity = activityReference.get();
            if (activity == null) return null;

            try {
                InetAddress connectableAddress = InetAddress.getByName(params[0]);
                int port = Integer.parseInt(params[1]);
                inetSocketAddress = new InetSocketAddress(connectableAddress, port);

                activity.peerHandler.addPeer(inetSocketAddress, null,null);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return inetSocketAddress;
        }
    }

    public void stopNode() {
        network.closeChannel();
        networkRunning = false;
    }
}
