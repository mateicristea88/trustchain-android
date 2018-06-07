package nl.tudelft.cs4160.trustchain_android.stresstest;

interface NodeStatistics {
    void incrementMessagesSent();
    void incrementMessagesReceived();

    void addBytesSent(int bytes);
    void addBytesReceived(int bytes);
}
