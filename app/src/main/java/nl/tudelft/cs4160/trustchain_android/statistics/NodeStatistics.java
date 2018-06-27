package nl.tudelft.cs4160.trustchain_android.statistics;

import nl.tudelft.cs4160.trustchain_android.network.NetworkStatusListener;

public interface NodeStatistics {
    void messageSent(NetworkStatusListener name);
    void messageReceived(NetworkStatusListener name);

    void introductionRequestSent(NetworkStatusListener name);
    void introductionRequestReceived(NetworkStatusListener name);

    void introductionResponseSent(NetworkStatusListener name);
    void introductionResponseReceived(NetworkStatusListener name);

    void punctureReceived(NetworkStatusListener name);
    void punctureSent(NetworkStatusListener name);

    void punctureRequestReceived(NetworkStatusListener name);
    void punctureRequestSent(NetworkStatusListener name);

    void blockMessageReceived(NetworkStatusListener name);
    void blockMessageSent(NetworkStatusListener name);

    void bytesSent(NetworkStatusListener name, int bytes);
    void bytesReceived(NetworkStatusListener name, int bytes);
}
