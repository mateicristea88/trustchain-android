package nl.tudelft.cs4160.trustchain_android.stresstest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.util.Util;

public class StressTestActivity extends AppCompatActivity implements NodeStatistics {

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

    private TextView nodesRunning;

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
                for (StressTestNode node : nodes) {
                    node.stopNode();
                }
                nodesRunning.setText(String.valueOf(nodes.size()));
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        StatisticsServer.getInstance().setStatisticsDisplay(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        introductionRequestReceived();
        introductionRequestSent();
        introductionResponseReceived();
        introductionResponseSent();
        punctureSent();
        punctureReceived();
        punctureRequestReceived();
        punctureRequestSent();
        messageReceived();
        messageSent();
        bytesReceived(0);
        bytesSent(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        StatisticsServer.getInstance().removeStatisticsDisplay();
    }

    public void startStressTest(int amount) {
        for (int i = 0; i < amount; i++) {
            port += 5;
            StressTestNode node = new StressTestNode(this, port);
            nodes.add(node);
            node.startNode();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    nodesRunning.setText(String.valueOf(nodes.size()));
                }
            });
        }
    }

//    @Override
//    public void onBackPressed() {
//        for (StressTestNode node : nodes) {
//            node.stopNode();
//        }
//        super.onBackPressed();
//    }

    @Override
    public void messageSent() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              messagesSent.setText(String.valueOf(StatisticsServer.getInstance().messagesSent));
                          }
                      }
        );
    }

    @Override
    public void messageReceived() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              messagesReceived.setText(String.valueOf(StatisticsServer.getInstance().messagesReceived));
                          }
                      }
        );
    }

    @Override
    public void introductionRequestSent() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              introductionRequestsSent.setText(String.valueOf(StatisticsServer.getInstance().introductionRequestsSent));
                          }
                      }
        );
    }

    @Override
    public void introductionRequestReceived() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              introductionRequestsReceived.setText(String.valueOf(StatisticsServer.getInstance().introductionRequestsReceived));
                          }
                      }
        );
    }

    @Override
    public void introductionResponseSent() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              introductionResponsesSent.setText(String.valueOf(StatisticsServer.getInstance().introductionResponsesSent));
                          }
                      }
        );
    }

    @Override
    public void introductionResponseReceived() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              introductionsResponsesReceived.setText(String.valueOf(StatisticsServer.getInstance().introductionResponsesReceived));
                          }
                      }
        );
    }

    @Override
    public void punctureReceived() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                puncturesReceived.setText(String.valueOf(StatisticsServer.getInstance().puncturesReceived));
            }
        });
    }

    @Override
    public void punctureSent() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              puncturesSent.setText(String.valueOf(StatisticsServer.getInstance().puncturesSent));
                          }
                      });
    }

    @Override
    public void punctureRequestReceived() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              punctureRequestsReceived.setText(String.valueOf(StatisticsServer.getInstance().punctureRequestsReceived));
                          }
                      });
    }

    @Override
    public void punctureRequestSent() {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              punctureRequestsSent.setText(String.valueOf(StatisticsServer.getInstance().punctureRequestsSent));
                          }
                      });
    }

    @Override
    public void blockMessageReceived() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blockMessagesReceived.setText(String.valueOf(StatisticsServer.getInstance().blockMessagesReceived));
            }
        });
    }

    @Override
    public void blockMessageSent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blockMessagesSent.setText(String.valueOf(StatisticsServer.getInstance().blockMessagesSent));
            }
        });
    }

    @Override
    public void bytesReceived(int bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bytesReceived.setText(Util.readableSize(StatisticsServer.getInstance().bytesReceivedCount));
            }
        });
    }

    @Override
    public void bytesSent(int bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bytesSent.setText(Util.readableSize(StatisticsServer.getInstance().bytesSentCount));
            }
        });
    }
}
