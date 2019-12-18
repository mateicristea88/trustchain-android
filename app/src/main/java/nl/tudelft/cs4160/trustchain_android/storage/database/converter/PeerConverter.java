package nl.tudelft.cs4160.trustchain_android.storage.database.converter;

import android.util.Base64;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.storage.database.entity.DbPeer;

public class PeerConverter {
    private static final String TAG = "PeerConverter";

    private PeerConverter() {}

    public static DbPeer toDbPeer(Peer peer) {
        DbPeer dbPeer = new DbPeer();
        dbPeer.address = peer.getIpAddress().getHostAddress();
        dbPeer.port = peer.getPort();
        dbPeer.publicKey = Base64.encodeToString(peer.getPublicKeyPair().toBytes(), Base64.DEFAULT);
        dbPeer.name = peer.getName();
        dbPeer.connectionType = peer.getConnectionType();
        dbPeer.lastSentTime = (peer.getLastSentTime() > 0) ? new Date(peer.getLastSentTime()) : null;
        dbPeer.lastReceivedTime = (peer.getLastReceivedTime() > 0) ? new Date(peer.getLastReceivedTime()) : null;
        dbPeer.creationTime = new Date(peer.getCreationTime());
        return dbPeer;
    }

    public static Peer fromDbPeer(DbPeer dbPeer) {
        InetSocketAddress address = new InetSocketAddress(dbPeer.address, dbPeer.port);
        PublicKeyPair publicKeyPair = new PublicKeyPair(Base64.decode(dbPeer.publicKey, Base64.DEFAULT));
        return new Peer(address, publicKeyPair, dbPeer.name);
    }

    public static List<Peer> fromDbPeers(List<DbPeer> dbPeers) {
        List<Peer> peers = new ArrayList<>();
        for (DbPeer dbPeer : dbPeers) {
            peers.add(PeerConverter.fromDbPeer(dbPeer));
        }
        return peers;
    }
}
