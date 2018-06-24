package nl.tudelft.cs4160.trustchain_android.statistics;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import nl.tudelft.cs4160.trustchain_android.network.NetworkStatusListener;
import nl.tudelft.cs4160.trustchain_android.stresstest.StressTestNode;

public class StatisticsServer implements NodeStatistics {

    public Map<NetworkStatusListener, Integer> messagesReceived = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> messagesSent = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Long> bytesReceivedCount = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Long> bytesSentCount = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> introductionRequestsSent = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> introductionRequestsReceived = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> introductionResponsesSent = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> introductionResponsesReceived = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> puncturesSent = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> puncturesReceived = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> punctureRequestsSent = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> punctureRequestsReceived = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> blockMessagesSent = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> blockMessagesReceived = new ConcurrentHashMap<>();
    public Map<NetworkStatusListener, Integer> crawlRequestsReceived = new ConcurrentHashMap<>();
    public static Map<NetworkStatusListener, Long> startTime = new ConcurrentHashMap<>();
    private boolean logInitialized = false;

    private static StatisticsServer statistics;

    public StatisticsServer() {
    }

    public static synchronized StatisticsServer getInstance() {
        if (statistics == null) {
            statistics = new StatisticsServer();
        }
        return statistics;
    }

    public void start(NetworkStatusListener node) {
        startTime.put(node, System.currentTimeMillis());
        messagesReceived.put(node, 0);
        messagesSent.put(node, 0);
        bytesReceivedCount.put(node, 0L);
        bytesSentCount.put(node, 0L);
        introductionRequestsSent.put(node, 0);
        introductionRequestsReceived.put(node, 0);
        introductionResponsesSent.put(node, 0);
        introductionResponsesReceived.put(node, 0);
        puncturesSent.put(node, 0);
        puncturesReceived.put(node, 0);
        punctureRequestsSent.put(node, 0);
        punctureRequestsReceived.put(node, 0);
        blockMessagesSent.put(node, 0);
        blockMessagesReceived.put(node, 0);
        crawlRequestsReceived.put(node, 0);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                logStats(node);
            }
        }, 0, 10000);
    }

    private void logStats(NetworkStatusListener node) {
        if (!logInitialized) {
            Log.i("Statistics-" + node.getName(), "runtime,messagesSent,messagesReceived," +
                    "introductionRequestsSent,introductionRequestsReceived,introductionResponsesSent," +
                    "introductionResponsesReceived,puncturesSent,puncturesReceived," +
                    "punctureRequestsSent,punctureRequestsReceived,blockMessagesSent," +
                    "blockMessagesReceived,bytesSentCount,bytesReceivedCount,activeConnections,newConnections");
            logInitialized = true;
        }
        long runtime = System.currentTimeMillis() - StatisticsServer.startTime.get(node);
        StringBuilder sb = new StringBuilder()
                .append(runtime)
                .append(",")
                .append(messagesSent.get(node))
                .append(",")
                .append(messagesReceived.get(node))
                .append(",")
                .append(introductionRequestsSent.get(node))
                .append(",")
                .append(introductionRequestsReceived.get(node))
                .append(",")
                .append(introductionResponsesSent.get(node))
                .append(",")
                .append(introductionResponsesReceived.get(node))
                .append(",")
                .append(puncturesSent.get(node))
                .append(",")
                .append(puncturesReceived.get(node))
                .append(",")
                .append(punctureRequestsSent.get(node))
                .append(",")
                .append(punctureRequestsReceived.get(node))
                .append(",")
                .append(blockMessagesSent.get(node))
                .append(",")
                .append(blockMessagesReceived.get(node))
                .append(",")
                .append(bytesSentCount.get(node))
                .append(",")
                .append(bytesReceivedCount.get(node))
                .append(",")
                .append(node.getPeerHandler().getactivePeersList().size())
                .append(",")
                .append(node.getPeerHandler().getnewPeersList().size());
        Log.i("Statistics-" + node.getName(), sb.toString());
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized void messageSent(NetworkStatusListener name) {
        messagesSent.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void messageReceived(NetworkStatusListener name) {
        messagesReceived.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void introductionRequestSent(NetworkStatusListener name) {
        introductionRequestsSent.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void introductionRequestReceived(NetworkStatusListener name) {
        introductionRequestsReceived.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void introductionResponseSent(NetworkStatusListener name) {
        introductionResponsesSent.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void introductionResponseReceived(NetworkStatusListener name) {
        introductionResponsesReceived.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void punctureReceived(NetworkStatusListener name) {
        puncturesReceived.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void punctureSent(NetworkStatusListener name) {
        puncturesSent.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void punctureRequestReceived(NetworkStatusListener name) {
        punctureRequestsReceived.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void punctureRequestSent(NetworkStatusListener name) {
        punctureRequestsSent.compute(name, (s, integer) -> integer + 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public synchronized void blockMessageReceived(NetworkStatusListener name) {
        blockMessagesReceived.compute(name, (s, integer) -> integer + 1);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized void blockMessageSent(NetworkStatusListener name) {
        blockMessagesSent.compute(name, (s, integer) -> integer + 1);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized void bytesSent(NetworkStatusListener name, int bytes) {
        bytesSentCount.compute(name, (s, aLong) -> aLong + bytes);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized void bytesReceived(NetworkStatusListener name, int bytes) {
        bytesReceivedCount.compute(name, (s, aLong) -> aLong + bytes);
    }
}
