package nl.tudelft.cs4160.trustchain_android.stresstest;

interface NodeStatistics {
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

    void bytesSent(int bytes);
    void bytesReceived(int bytes);
}
