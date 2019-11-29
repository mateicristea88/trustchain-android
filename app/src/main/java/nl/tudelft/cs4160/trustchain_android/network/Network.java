package nl.tudelft.cs4160.trustchain_android.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto.Message;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.storage.repository.PeerRepository;
import nl.tudelft.cs4160.trustchain_android.ui.peersummary.PeerSummaryActivity;
import nl.tudelft.cs4160.trustchain_android.statistics.StatisticsServer;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

public class Network {
    private final String TAG = this.getClass().getName();
    private DatagramChannel channel;
    public String name;
    private int connectionType;
    private InetSocketAddress internalSourceAddress;
    private static Network network;
    private BlockRepository blockRepository;
    private PeerRepository peerRepository;
    private PublicKeyPair publicKey;
    private MessageHandler messageHandler;
    private NetworkStatusListener networkStatusListener;
    private int port = NetworkConnectionService.DEFAULT_PORT;
    private PeerSummaryActivity mutualBlockListener;
    private StatisticsServer statistics;

    private final static int INTRODUCTION_REQUEST_ID = 1;
    private final static int INTRODUCTION_RESPONSE_ID = 2;
    private final static int PUNCTURE_REQUEST_ID = 3;
    private final static int PUNCTURE_ID = 4;
    private final static int BLOCK_MESSAGE_ID = 5;
    private final static int CRAWL_REQUEST_ID = 6;

    /**
     * Empty constructor
     */
    private Network() {
    }

    /**
     * Non-singleton version of Network _specifically for stress tests_.
     * Allows to overwrite the node's name, keypair and port number.
     * @param username
     * @param publicKey
     * @param context
     * @param port
     */
    public Network(String username, PublicKeyPair publicKey, Context context, int port) {
        this.name = username;
        this.publicKey = publicKey;
        this.port = port;
        initVariables(context, false);
    }


    /**
     * Get the network instance.
     * If the network isn't initialized create a network and set the variables.
     * @param context
     * @return
     */
    public synchronized static Network getInstance(Context context) {
        if (network == null) {
            network = new Network();
            network.initVariables(context, true);
        }
        return network;
    }

    /**
     * Set the network communication listener.
     * @param networkStatusListener
     */
    public void setNetworkStatusListener(NetworkStatusListener networkStatusListener) {
        this.networkStatusListener = networkStatusListener;
    }

    /**
     * Set the crawl request listener
     * @param mutualBlockListener
     */
    public void setMutualBlockListener(PeerSummaryActivity mutualBlockListener) {
        this.mutualBlockListener = mutualBlockListener;
    }

    /**
     * Initialize local variables including opening the channel.
     * name and publicKey are only set if that wasn't already done by the constructor.
     * @param dbAccess whether or not the message handler for this network instance should access the database.
     */
    private void initVariables(Context context, boolean dbAccess) {
        this.statistics = StatisticsServer.getInstance();
        if (name == null) name = UserNameStorage.getUserName(context);
        if (publicKey == null) publicKey = Key.loadKeys(context).getPublicKeyPair();
        AppDatabase database = AppDatabase.getInstance(context);
        blockRepository = new BlockRepository(database.blockDao());
        peerRepository = new PeerRepository(database.peerDao());
        messageHandler = new MessageHandler(this,
                dbAccess ? blockRepository : null,
                new PeerHandler(publicKey,name));
        openChannel();
        showLocalIpAddress();
    }

    /**
     * Open the network channel on the default port.
     */
    private void openChannel() {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * On receive data via the channel.
     * @param inputBuffer the received data
     * @return
     * @throws IOException
     */
    public SocketAddress receive(ByteBuffer inputBuffer) throws IOException {
        if (!channel.isOpen()) {
            openChannel();
        }
        return channel.receive(inputBuffer);
    }

    /**
     * Close the channel
     */
    public void closeChannel() {
        Log.i("Network", "Closing channel");
        channel.socket().close();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request and return the current connection type.
     * @return a string representation of the current connection type
     */
    public String getConnectionTypeString(ConnectivityManager cm) {
        String typename = "No connection";
        String subtypeName = "";
        try {
            connectionType = cm.getActiveNetworkInfo().getType();
            typename = cm.getActiveNetworkInfo().getTypeName();
            subtypeName = cm.getActiveNetworkInfo().getSubtypeName();
        } catch(Exception e) {

        }
        return typename + " " + subtypeName;
    }

    /**
     * Send an introduction request.
     * A introduction request is build and put into a message.
     * @param peer the destination.
     * @throws IOException
     */
    public void sendIntroductionRequest(Peer peer) throws IOException {
        MessageProto.IntroductionRequest request = MessageProto.IntroductionRequest.newBuilder()
                .setConnectionType(connectionType)
                .build();

        Message.Builder messageBuilder = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(INTRODUCTION_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setIntroductionRequest(request));

        statistics.introductionRequestSent(networkStatusListener);
        sendMessage(messageBuilder.build(), peer);
    }

    /**
     * Send a block message via the network to a peer
     * @param peer the receiving peer
     * @param block the data
     * @throws IOException
     */
    public void sendBlockMessage(Peer peer, TrustChainBlock block) throws IOException {
        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(BLOCK_MESSAGE_ID)
                .setPayload(MessageProto.Payload.newBuilder().setBlock(block))
                .build();

        statistics.blockMessageSent(networkStatusListener);
        sendMessage(message, peer);
    }

    /**
     * Send a crawl request message via the network to a peer
     * @param peer the receiving peer
     * @param request the data
     * @throws IOException
     */
    public void sendCrawlRequest(Peer peer, MessageProto.CrawlRequest request) throws IOException {
        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(CRAWL_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setCrawlRequest(request))
                .build();

        sendMessage(message, peer);
    }

    /**
     * Send a puncture request.
     *
     * @param peer         the destination.
     * @param puncturePeer the Peer to puncture.
     * @throws IOException
     */
    public void sendPunctureRequest(Peer peer, Peer puncturePeer) throws IOException {
        MessageProto.PunctureRequest pRequest = MessageProto.PunctureRequest.newBuilder()
                .setSourceSocket(internalSourceAddress.toString())
                .setPuncturePeer(puncturePeer.getProtoPeer())
                .build();

        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(PUNCTURE_REQUEST_ID)
                .setPayload(MessageProto.Payload.newBuilder().setPunctureRequest(pRequest))
                .build();

        statistics.punctureRequestSent(networkStatusListener);
        sendMessage(message, peer);
    }

    /**
     * Send a puncture.
     *
     * @param peer the destination.
     * @throws IOException
     */
    public void sendPuncture(Peer peer) throws IOException {
        MessageProto.Puncture puncture = MessageProto.Puncture.newBuilder()
                .setSourceSocket(internalSourceAddress.toString())
                .build();

        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(PUNCTURE_ID)
                .setPayload(MessageProto.Payload.newBuilder().setPuncture(puncture))
                .build();

        statistics.punctureSent(networkStatusListener);
        sendMessage(message, peer);
    }

    /**
     * Send an introduction response.
     *
     * @param peer    the destination.
     * @param invitee the invitee to which the destination inboxItem will send a puncture request.
     * @throws IOException
     */
    public void sendIntroductionResponse(Peer peer, Peer invitee) throws IOException {
        List<MessageProto.Peer> peers = new ArrayList<>();
        for (Peer p : networkStatusListener.getPeerHandler().getPeerList()) {
            if (p.isReceivedFrom() && p.getName() != null && p.isAlive())
                peers.add(p.getProtoPeer());
        }

        MessageProto.IntroductionResponse response = MessageProto.IntroductionResponse.newBuilder()
                .setConnectionType(connectionType)
                .setInternalSourceSocket(internalSourceAddress.toString())
                .setInvitee(invitee.getProtoPeer())
                .addAllPeers(peers)
                .build();

        Message message = Message.newBuilder()
                .setSourcePublicKey(ByteString.copyFrom(publicKey.toBytes()))
                .setSourceName(name)
                .setDestinationAddress(ByteString.copyFrom(peer.getAddress().getAddress().getAddress()))
                .setDestinationPort(peer.getAddress().getPort())
                .setType(INTRODUCTION_RESPONSE_ID)
                .setPayload(MessageProto.Payload.newBuilder().setIntroductionResponse(response))
                .build();

        statistics.introductionResponseSent(networkStatusListener);
        sendMessage(message, peer);
    }

    /**
     * Send a message to given inboxItem.
     *
     * @param message the message to send.
     * @param peer    the destination inboxItem.
     * @throws IOException
     */
    private synchronized void sendMessage(Message message, Peer peer) throws IOException {
        ByteBuffer outputBuffer = ByteBuffer.wrap(message.toByteArray());
        channel.send(outputBuffer, peer.getAddress());
        statistics.bytesSent(networkStatusListener, outputBuffer.position());
        statistics.messageSent(networkStatusListener);
        Log.i(TAG, "Sending to " + peer.getAddress() + " (" + peer.getName() + "):\n" + message);
        peer.sentData();
    }

    /**
     * Show local ip address.
     */
    public void showLocalIpAddress() {
        ShowLocalIPTask showLocalIPTask = new ShowLocalIPTask(port);
        showLocalIPTask.execute();
    }

    /**
     * Handle incoming data.
     *  - Tries to parse the bytes into a proto Message.
     *  - Updates the external ip address according to where the message was sent.
     *  - Add the new connection as a new peer or update an existing peer.
     *  - Send the message along for further processing.
     * @param data    the data {@link ByteBuffer}.
     * @param address the incoming address.
     */
    public void dataReceived(Context context, ByteBuffer data, InetSocketAddress address) {
        // If we don't have an internal address, try to find it again instead of handling the message.
        if (internalSourceAddress == null) {
            showLocalIpAddress();
            return;
        }

        try {
            Message message = Message.parseFrom(data);
            Log.v(TAG, "Received " + message.toString());

            if (networkStatusListener != null) {
                networkStatusListener.updateWan(message);

                PublicKeyPair sourcePubKey = new PublicKeyPair(message.getSourcePublicKey().toByteArray());
                Peer peer = networkStatusListener.getPeerHandler().getOrMakePeer(address,sourcePubKey,message.getSourceName());
                if (peer == null) {
                    return;
                }

                statistics.messageReceived(networkStatusListener);
                statistics.bytesReceived(networkStatusListener, data.remaining());
                peer.receivedData();
                PubKeyAndAddressPairStorage.addPubkeyAndAddressPair(context, sourcePubKey, address);
                handleMessage(peer, message, sourcePubKey, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks which message type we've received and calls the appropriate functions to handle
     * further processing of the message.
     * @param peer the peer that sent this message
     * @param message the message that was received
     * @param pubKeyPair the publickeypair associated with the sender
     * @param context context which is used to update the inbox
     * @throws Exception
     */
    private void handleMessage(Peer peer, Message message, PublicKeyPair pubKeyPair, Context context) throws Exception {
        switch (message.getType()) {
            case INTRODUCTION_REQUEST_ID:
                statistics.introductionRequestReceived(networkStatusListener);
                messageHandler.handleIntroductionRequest(peer, message.getPayload().getIntroductionRequest());
                break;
            case INTRODUCTION_RESPONSE_ID:
                statistics.introductionResponseReceived(networkStatusListener);
                messageHandler.handleIntroductionResponse(peer, message.getPayload().getIntroductionResponse());
                break;
            case PUNCTURE_ID:
                statistics.punctureReceived(networkStatusListener);
                messageHandler.handlePuncture(peer, message.getPayload().getPuncture());
                break;
            case PUNCTURE_REQUEST_ID:
                statistics.punctureRequestReceived(networkStatusListener);
                messageHandler.handlePunctureRequest(peer, message.getPayload().getPunctureRequest());
                break;
            case BLOCK_MESSAGE_ID:
                statistics.blockMessageReceived(networkStatusListener);
                TrustChainBlock block = message.getPayload().getBlock();

                // update the inbox
                addPeerToInbox(pubKeyPair, peer, context);

                messageHandler.handleReceivedBlock(peer, block);
                if (mutualBlockListener != null) {
                    mutualBlockListener.blockAdded(block);
                }
                break;
            case CRAWL_REQUEST_ID:
                // Crawl messages not logged
                messageHandler.handleCrawlRequest(peer, message.getPayload().getCrawlRequest());
                break;
        }
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public NetworkStatusListener getStatusListener() {
        return networkStatusListener;
    }

    /**
     * Add peer to inbox.
     * This means storing the InboxItem object in the local preferences.
     * @param pubKeyPair keypair associated with this peer
     * @param peer the peer that needs to be added
     * @param context needed for storage
     */
    private void addPeerToInbox(PublicKeyPair pubKeyPair, Peer peer, Context context) {
        peerRepository.insertOrUpdate(peer);
    }

    /**
     * Show local ip visually to the user.
     */
    private class ShowLocalIPTask extends AsyncTask<Void, Void, InetAddress> {
        int port;

        public ShowLocalIPTask(int port) {
            super();
            this.port = port;
        }

        @Override
        protected InetAddress doInBackground(Void... nothin) {
            try {
                for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = (NetworkInterface) en.nextElement();
                    for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress;
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(InetAddress inetAddress) {
            super.onPostExecute(inetAddress);
            if (inetAddress != null) {
                internalSourceAddress = new InetSocketAddress(inetAddress, port);
                if (networkStatusListener != null) {
                    networkStatusListener.updateInternalSourceAddress(internalSourceAddress.toString().replace("/",""));
                }
            }
        }
    }
}
