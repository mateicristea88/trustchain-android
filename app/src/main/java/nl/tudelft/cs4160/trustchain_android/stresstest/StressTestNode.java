package nl.tudelft.cs4160.trustchain_android.stresstest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.NetworkCommunicationListener;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerListener;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.BootstrapIPStorage;

public class StressTestNode implements PeerListener, NetworkCommunicationListener {

    private static final String TAG = StressTestNode.class.getName();
    private Network network;
    private PeerHandler peerHandler;
    private String wan = "";
    public final static String CONNECTABLE_ADDRESS = "130.161.211.254";
    public final static int DEFAULT_PORT = 1873;
    private final static int BUFFER_SIZE = 65536;
    private Context context;

    private String userName;
    private DualSecret keyPair;

    int port;

    // --- Node stats ---
    NodeStatistics statistics;
    int messagesSent = 0;
    int messagesReceived = 0;
    int introductionRequestsSent = 0;
    int introductionRequestsReceived = 0;
    int introductionResponsesSent = 0;
    int introductionResponsesReceived = 0;
    int puncturesSent = 0;
    int puncturesReceived = 0;
    int blockMessagesSent = 0;
    int blockMessagesReceived = 0;
    int crawlRequestsReceived = 0;
    private long bytesReceived = 0;
    private long bytesSent = 0;


    public void startNode () {
        initVariables();
        addInitialPeer();
        startListenThread();
        startSendThread();
//        initPeerLists();
//        if (savedInstanceState != null) {
//            updatePeerLists();
//        }
    }

    public StressTestNode (Context context, int port, NodeStatistics statistics) {
        this.context = context;
        this.userName = UsernameGenerator.getUsername();
        this.keyPair = Key.createNewKeyPair();
        this.port = port;
        this.statistics = statistics;
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

//    /**
//     * Initialize the inboxItem lists.
//     */
//    private void initPeerLists() {
//        ListView incomingPeerConnectionListView = findViewById(R.id.incoming_peer_connection_list_view);
//        ListView outgoingPeerConnectionListView = findViewById(R.id.outgoing_peer_connection_list_view);
//        incomingPeerAdapter = new PeerListAdapter(getApplicationContext(), R.layout.peer_connection_list_item, peerHandler.getIncomingList(), (CoordinatorLayout) findViewById(R.id.myCoordinatorLayout));
//        incomingPeerConnectionListView.setAdapter(incomingPeerAdapter);
//        outgoingPeerAdapter = new PeerListAdapter(getApplicationContext(), R.layout.peer_connection_list_item, peerHandler.getOutgoingList(), (CoordinatorLayout) findViewById(R.id.myCoordinatorLayout));
//        outgoingPeerConnectionListView.setAdapter(outgoingPeerAdapter);
//    }

    /**
     * Initialize all local variables
     * If this activity is opened with a saved instance state
     * we load the list of peers from this saved state.
//     * @param savedInstanceState
     */
    private void initVariables() {

        peerHandler = new PeerHandler(userName);
        initKey();
        network = new Network(userName, keyPair.getPublicKeyPair(), context, port);

        getPeerHandler().setPeerListener(this);
        network.setNetworkCommunicationListener(this);
        network.updateConnectionType((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
//        ((TextView) findViewById(R.id.peer_id)).setText(peerHandler.getHashId());
    }

    /**
     * If the app is launched for the first time
     * a new keyPair is created and saved locally in the storage.
     * A genesis block is also created automatically.
     */
    private void initKey() {
        DualSecret kp = Key.loadKeys(context);
        if (kp == null) {
            kp = Key.createAndSaveKeys(context);
            MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(kp);
//            dbHelper.insertInDB(block);
        }
    }

    /**
//     * Start the thread send thread responsible for sending a {@link IntroductionRequest} to a random inboxItem every 5 seconds.
     */
    private void startSendThread() {
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        if (peerHandler.size() > 0) {
                            Peer peer = peerHandler.getEligiblePeer(null);
                            if (peer != null) {
                                network.sendIntroductionRequest(peer);
                                messagesSent++;
                                introductionRequestsSent++;
                                statistics.incrementMessagesSent();
                                //  sendBlockMessage(peer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                } while (!Thread.interrupted());
                Log.d("App-To-App Log", "Send thread stopped");
            }
        });
        sendThread.start();
        Log.d("App-To-App Log", "Send thread started");
    }

    /**
     * Start the listen thread. The thread opens a new {@link DatagramChannel} and calls {@link Network#dataReceived(Context, ByteBuffer,
     * InetSocketAddress)} for each incoming datagram.
     */
    private void startListenThread() {
        final Context context = this.context;

        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    while (!Thread.interrupted()) {
                        inputBuffer.clear();
                        SocketAddress address = network.receive(inputBuffer);

                        bytesReceived += inputBuffer.position();
                        statistics.addBytesReceived(inputBuffer.position());

                        inputBuffer.flip();
                        network.dataReceived(context, inputBuffer, (InetSocketAddress) address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("App-To-App Log", "Listen thread stopped");
                }
            }
        });
        listenThread.start();
        Log.d("App-To-App Log", "Listen thread started");
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
//        setWanvote(wan.replace("/",""));
    }

    @Override
    public void updateInternalSourceAddress(String address) {

    }

    @Override
    public void updatePeerLists() {

    }

    @Override
    public void updateConnectionType(int connectionType, String typename, String subtypename) {

    }

    @Override
    public void handleIntroductionRequest(Peer peer, MessageProto.IntroductionRequest request) throws IOException {
        messagesReceived++;
        introductionRequestsReceived++;

        peer.setConnectionType((int) request.getConnectionType());
        peer.setNetworkOperator(request.getNetworkOperator());
        if (getPeerHandler().size() > 1) {
            Peer invitee = getPeerHandler().getEligiblePeer(peer);
            if (invitee != null) {
                network.sendIntroductionResponse(peer, invitee);
                messagesSent++;
                introductionResponsesSent++;
                statistics.incrementMessagesSent();
                network.sendPunctureRequest(invitee, peer);
                messagesSent++;
                puncturesSent++;
                statistics.incrementMessagesSent();
                Log.d("Network", "Introducing " + invitee.getAddress() + " to " + peer.getAddress());
            }
        } else {
            Log.d("Network", "Peerlist too small, can't handle introduction request");
            network.sendIntroductionResponse(peer, null);
            messagesSent++;
            introductionResponsesSent++;
            statistics.incrementMessagesSent();
        }
    }

    @Override
    public void handleIntroductionResponse(Peer peer, MessageProto.IntroductionResponse response) throws Exception {
        messagesReceived++;
        introductionResponsesReceived++;
        statistics.incrementMessagesReceived();

        peer.setConnectionType((int) response.getConnectionType());
        peer.setNetworkOperator(response.getNetworkOperator());
        List<ByteString> pex = response.getPexList();
        for (ByteString pexPeer : pex) {
            Peer p = Peer.deserialize(pexPeer.toByteArray());
            Log.d(TAG, "From " + peer + " | found peer in pexList: " + p);

            getPeerHandler().getOrMakePeer(p.getPeerId(), p.getAddress());
        }
    }

    @Override
    public void handlePunctureRequest(Peer peer, MessageProto.PunctureRequest request) throws IOException {
        messagesReceived++;
        puncturesReceived++;
        statistics.incrementMessagesReceived();

        Peer puncturePeer = null;
        try {
            puncturePeer = Peer.deserialize(request.getPuncturePeer().toByteArray());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (!getPeerHandler().peerExistsInList(puncturePeer)) {
            network.sendPuncture(puncturePeer);
            puncturesSent++;
            statistics.incrementMessagesSent();
        }
    }

    @Override
    public void handleReceivedBlock(Peer peer, MessageProto.TrustChainBlock block) throws IOException {
        // Blocks are ignored
        messagesReceived++;
        blockMessagesReceived++;
        statistics.incrementMessagesReceived();
    }

    @Override
    public void handleCrawlRequest(Peer peer, MessageProto.CrawlRequest request) throws IOException {
        // Crawl requests are ignored
        messagesReceived++;
        crawlRequestsReceived++;
        statistics.incrementMessagesReceived();
    }

    @Override
    public void handlePuncture(Peer peer, MessageProto.Puncture puncture) throws IOException {
        messagesReceived++;
        puncturesReceived++;
        statistics.incrementMessagesReceived();
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
    public void updateIncomingPeers() {

    }

    @Override
    public void updateOutgoingPeers() {

    }

    /**
     * Asynctask to create the inetsocketaddress since network stuff can no longer happen on the main thread in android v3 (honeycomb).
     */
    private static class CreateInetSocketAddressTask extends AsyncTask<String, Void, InetSocketAddress> {
        private WeakReference<StressTestNode> activityReference;

        CreateInetSocketAddressTask(StressTestNode context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected InetSocketAddress doInBackground(String... params) {
            InetSocketAddress inetSocketAddress = null;
            StressTestNode activity = activityReference.get();
            if (activity == null) return null;

            try {
                InetAddress connectableAddress = InetAddress.getByName(params[0]);
                int port = Integer.parseInt(params[1]);
                inetSocketAddress = new InetSocketAddress(connectableAddress, port);

                activity.peerHandler.addPeer(null, inetSocketAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return inetSocketAddress;
        }
    }

    public void stopNode() {
        network.closeChannel();

    }

    private void logMessageReiceived() {

    }
}
