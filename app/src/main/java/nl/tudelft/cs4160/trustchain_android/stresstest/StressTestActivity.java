package nl.tudelft.cs4160.trustchain_android.stresstest;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.statistics.NodeStatistics;
import nl.tudelft.cs4160.trustchain_android.statistics.StatisticsServer;
import nl.tudelft.cs4160.trustchain_android.util.Util;

public class StressTestActivity extends AppCompatActivity {

    private EditText nodesToStart;
    private Button startStressTestButton;
    private Button stopStressTestButton;
    private int port = OverviewConnectionsActivity.DEFAULT_PORT;

    private List<StressTestNode> nodes;

    private TextView messagesSent;
    private TextView messagesReceived;
    private TextView bytesSent;
    private TextView bytesReceived;
    private TextView puncturesReceived;
    private TextView puncturesSent;
    private TextView punctureRequestsReceived;
    private TextView punctureRequestsSent;
    private TextView introductionRequestsSent;
    private TextView introductionRequestsReceived;
    private TextView introductionResponsesSent;
    private TextView introductionsResponsesReceived;
    private TextView blockMessagesSent;
    private TextView blockMessagesReceived;
    private TextView uptime;
    private TextView nodesRunning;

    private Handler uptimeUpdateHandler;
    private Runnable statisticsUpdateTask = new Runnable() {
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public void run() {
            StatisticsServer stats = StatisticsServer.getInstance();
            runOnUiThread(() -> {
                long time = System.currentTimeMillis();
                long runtime = System.currentTimeMillis() - StatisticsServer.startTime.get(Network.getInstance(getApplicationContext()).getStatusListener());
                uptime.setText(Util.timeToString(runtime));
                messagesSent.setText(String.valueOf(stats.messagesSent.values().stream().mapToInt((i) -> i).sum()));
                messagesReceived.setText(String.valueOf(stats.messagesReceived.values().stream().mapToInt((i) -> i).sum()));
                introductionRequestsSent.setText(String.valueOf(stats.introductionRequestsSent.values().stream().mapToInt((i) -> i).sum()));
                introductionRequestsReceived.setText(String.valueOf(stats.introductionRequestsReceived.values().stream().mapToInt((i) -> i).sum()));
                introductionResponsesSent.setText(String.valueOf(stats.introductionResponsesSent.values().stream().mapToInt((i) -> i).sum()));
                introductionsResponsesReceived.setText(String.valueOf(stats.introductionResponsesReceived.values().stream().mapToInt((i) -> i).sum()));
                puncturesReceived.setText(String.valueOf(stats.puncturesReceived.values().stream().mapToInt((i) -> i).sum()));
                puncturesSent.setText(String.valueOf(stats.puncturesSent.values().stream().mapToInt((i) -> i).sum()));
                punctureRequestsReceived.setText(String.valueOf(stats.punctureRequestsReceived.values().stream().mapToInt((i) -> i).sum()));
                punctureRequestsSent.setText(String.valueOf(stats.punctureRequestsSent.values().stream().mapToInt((i) -> i).sum()));
                blockMessagesReceived.setText(String.valueOf(stats.blockMessagesReceived.values().stream().mapToInt((i) -> i).sum()));
                blockMessagesSent.setText(String.valueOf(stats.blockMessagesSent.values().stream().mapToInt((i) -> i).sum()));
                bytesReceived.setText(Util.readableSize(stats.bytesReceivedCount.values().stream().mapToLong((i) -> i).sum()));
                bytesSent.setText(Util.readableSize(stats.bytesSentCount.values().stream().mapToLong((i) -> i).sum()));
                uptimeUpdateHandler.postDelayed(statisticsUpdateTask, 990);
                Log.v("Statistics", "Update completed, took " + (System.currentTimeMillis() - time) + " millis");
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_test);
        nodes = new ArrayList<>();

        nodesToStart = findViewById(R.id.nodes_to_start);
        nodesToStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nodesToStart.selectAll();
            }
        });
        startStressTestButton = findViewById(R.id.start_stress_test);
        stopStressTestButton = findViewById(R.id.stop_stress_test);

        startStressTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStressTest(Integer.parseInt(nodesToStart.getText().toString()));
            }
        });
        stopStressTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNodes();
            }
        });

        messagesReceived = findViewById(R.id.messages_received);
        messagesSent = findViewById(R.id.messages_sent);
        bytesReceived = findViewById(R.id.bytes_received);
        bytesSent = findViewById(R.id.bytes_sent);
        puncturesSent = findViewById(R.id.punctures_sent);
        puncturesReceived = findViewById(R.id.punctures_received);
        punctureRequestsSent = findViewById(R.id.puncture_requests_sent);
        punctureRequestsReceived = findViewById(R.id.puncture_requests_received);
        introductionResponsesSent = findViewById(R.id.introduction_responses_sent);
        introductionsResponsesReceived = findViewById(R.id.introduction_responses_received);
        introductionRequestsSent= findViewById(R.id.introduction_requests_sent);
        introductionRequestsReceived = findViewById(R.id.introduction_requests_received);
        blockMessagesSent = findViewById(R.id.block_messages_sent);
        blockMessagesReceived = findViewById(R.id.block_messages_received);
        nodesRunning = findViewById(R.id.nodes_running);
        uptime = findViewById(R.id.run_time);


        HandlerThread updateThread = new HandlerThread("StatUpdater");
        updateThread.start();
        uptimeUpdateHandler = new Handler(updateThread.getLooper());
    }

    private void stopNodes() {
        for (StressTestNode node : nodes) {
            node.stopNode();
        }
        nodes.clear();
        nodesRunning.setText(String.valueOf(nodes.size()));
    }
    
    protected void onStart() {
        super.onStart();
//        StatisticsServer.getInstance().setStatisticsDisplay(this);
        uptimeUpdateHandler.postDelayed(statisticsUpdateTask, 200);
    }

    @Override
    protected void onDestroy() {
        stopNodes();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startStressTest(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        StatisticsServer.getInstance().removeStatisticsDisplay();
        uptimeUpdateHandler.removeCallbacks(statisticsUpdateTask);
    }

    public void startStressTest(int amount) {
        for (int i = 0; i < amount; i++) {
            port += 5;
            StressTestNode node = new StressTestNode(this, port);
            nodes.add(node);
            node.startNode();
        }
        runOnUiThread(() -> nodesRunning.setText(String.valueOf(nodes.size())));
    }
}
