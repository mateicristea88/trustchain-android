package nl.tudelft.cs4160.trustchain_android.network;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.peer.Peer;

public interface NetworkConnectionListener {
    void updateInternalSourceAddress(String address);
    void updatePeerLists(List<Peer> activePeersList, List<Peer> newPeersList);
    void updateWan(String ip);
    void updateConnectionType(String connectionTypeStr);
    void updateActivePeers(List<Peer> activePeers);
    void updateNewPeers(List<Peer> newPeers);
}
