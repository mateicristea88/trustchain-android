package nl.tudelft.cs4160.trustchain_android.statistics;

public class StatisticsServer implements NodeStatistics {

    public int messagesReceived = 0;
    public int messagesSent = 0;
    public long bytesReceivedCount = 0;
    public long bytesSentCount = 0;
    public int introductionRequestsSent = 0;
    public int introductionRequestsReceived = 0;
    public int introductionResponsesSent = 0;
    public int introductionResponsesReceived = 0;
    public int puncturesSent = 0;
    public int puncturesReceived = 0;
    public int punctureRequestsSent = 0;
    public int punctureRequestsReceived = 0;
    public int blockMessagesSent = 0;
    public int blockMessagesReceived = 0;
    public int crawlRequestsReceived = 0;
    public static long startTime = 0;

    private static StatisticsServer statistics;
    private NodeStatistics statisticsDisplay;

    public static synchronized StatisticsServer getInstance() {
        if (statistics == null) {
            statistics = new StatisticsServer();
            startTime = System.currentTimeMillis();
        }
        return statistics;
    }

    public void setStatisticsDisplay(NodeStatistics display) {
        statisticsDisplay = display;
    }

    public void removeStatisticsDisplay() {
        statisticsDisplay = null;
    }

    @Override
    public void messageSent() {
        messagesSent++;
        if (statisticsDisplay != null) statisticsDisplay.messageSent();
    }

    @Override
    public void messageReceived() {
        messagesReceived++;
        if (statisticsDisplay != null) statisticsDisplay.messageReceived();
    }

    @Override
    public void introductionRequestSent() {
        introductionRequestsSent++;
        if (statisticsDisplay != null) statisticsDisplay.introductionRequestSent();
    }

    @Override
    public void introductionRequestReceived() {
        introductionRequestsReceived++;
        if (statisticsDisplay != null) statisticsDisplay.introductionRequestReceived();
    }

    @Override
    public void introductionResponseSent() {
        introductionResponsesSent++;
        if (statisticsDisplay != null) statisticsDisplay.introductionResponseSent();
    }

    @Override
    public void introductionResponseReceived() {
        introductionResponsesReceived++;
        if (statisticsDisplay != null) statisticsDisplay.introductionResponseReceived();
    }

    @Override
    public void punctureReceived() {
        puncturesReceived++;
        if (statisticsDisplay != null) statisticsDisplay.punctureReceived();
    }

    @Override
    public void punctureSent() {
        puncturesSent++;
        if (statisticsDisplay != null) statisticsDisplay.punctureSent();
    }

    @Override
    public void punctureRequestReceived() {
        punctureRequestsReceived++;
        if (statisticsDisplay != null) statisticsDisplay.punctureRequestReceived();
    }

    @Override
    public void punctureRequestSent() {
        punctureRequestsSent++;
        if (statisticsDisplay != null) statisticsDisplay.punctureRequestSent();
    }

    @Override
    public void blockMessageReceived() {
        blockMessagesReceived++;
        if (statisticsDisplay != null) statisticsDisplay.blockMessageReceived();
    }

    @Override
    public void blockMessageSent() {
        blockMessagesSent++;
        if (statisticsDisplay != null) statisticsDisplay.blockMessageSent();
    }

    @Override
    public void bytesSent(int bytes) {
        bytesSentCount += bytes;
        if (statisticsDisplay != null) statisticsDisplay.bytesSent(bytes);
    }

    @Override
    public void bytesReceived(int bytes) {
        bytesReceivedCount += bytes;
        if (statisticsDisplay != null) statisticsDisplay.bytesReceived(bytes);
    }
}
