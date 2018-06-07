package nl.tudelft.cs4160.trustchain_android.stresstest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.util.Util;

public class StressTestActivity extends AppCompatActivity implements NodeStatistics {

    private EditText nodesToStart;
    private NumberPicker numberPicker;
    private Button startStressTestButton;
    private int port = OverviewConnectionsActivity.DEFAULT_PORT;

    private List<StressTestNode> nodes;

    private TextView messagesSent;
    private TextView messagesReceived;
    private TextView bytesSent;
    private TextView bytesReceived;
    private long bytesReceivedCount = 0;
    private long bytesSentCount = 0;
    private int messagesReceivedCount = 0;
    private int messagesSentCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_test);
        nodes = new ArrayList<>();

        nodesToStart = findViewById(R.id.nodes_to_start);
        startStressTestButton = findViewById(R.id.start_stress_test);
        numberPicker = findViewById(R.id.number_picker);
        numberPicker.setMaxValue(1000);
        numberPicker.setMinValue(0);

        startStressTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStressTest(numberPicker.getValue());
            }
        });

        messagesReceived = findViewById(R.id.messages_received);
        messagesSent = findViewById(R.id.messages_sent);
        bytesReceived = findViewById(R.id.bytes_received);
        bytesSent = findViewById(R.id.bytes_sent);
    }

    public void startStressTest(int amount) {
        for (int i = 0; i < amount; i++) {
            port++;
            StressTestNode node = new StressTestNode(this, port, this);
            nodes.add(node);
            node.startNode();
        }
    }


    @Override
    public void incrementMessagesSent() {
        messagesSentCount++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesSent.setText("Sent: " + messagesSentCount);
            }
        });
    }

    @Override
    public void incrementMessagesReceived() {
        messagesReceivedCount++;
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              messagesReceived.setText("Received: " + messagesReceivedCount);
                          }
                      }
        );
    }

    @Override
    public void addBytesSent(int bytes) {
        bytesSentCount += bytes;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bytesSent.setText(Util.readableSize(bytesSentCount));
            }
        });
    }

    @Override
    public void addBytesReceived(int bytes) {
        bytesReceivedCount += bytes;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bytesReceived.setText(Util.readableSize(bytesReceivedCount));
            }
        });
    }


    @Override
    public void onBackPressed() {
        for (StressTestNode node : nodes) {
            node.stopNode();
        }
        super.onBackPressed();
    }
}
