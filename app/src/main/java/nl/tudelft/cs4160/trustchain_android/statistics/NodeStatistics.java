package nl.tudelft.cs4160.trustchain_android.statistics;

public interface NodeStatistics {
    void messageSent();
    void messageReceived();

    void introductionRequestSent();
    void introductionRequestReceived();

    void introductionResponseSent();
    void introductionResponseReceived();

    void punctureReceived();
    void punctureSent();

    void punctureRequestReceived();
    void punctureRequestSent();

    void blockMessageReceived();
    void blockMessageSent();

    void bytesSent(int bytes);
    void bytesReceived(int bytes);
}
