package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.stresstest.StressTestActivity;

public class ConnectionExplanationActivity extends AppCompatActivity {

    private ArrayList<String> symbolList;
    private String[] explanationText;
    private int[] colorList = {R.color.colorStatusConnected, R.color.colorStatusConnecting, R.color.colorStatusCantConnect, android.R.color.secondary_text_light, android.R.color.secondary_text_light};
    private Button stressTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSymbolList();
        createExplanationTextList();
        setContentView(R.layout.activity_connection_explanation);
        createConnectionExplanationList();

        stressTest = findViewById(R.id.stress_test);
        stressTest.setOnClickListener(view -> {
            Intent i = new Intent(this, StressTestActivity.class);
            startActivity(i);
        });
    }


    /**
     * Create the items that provides the explanation of the colors.
     */
    private void createConnectionExplanationList() {
        TextView connectionInfoHeaderText = findViewById(R.id.connectionInfoHeaderText);
        connectionInfoHeaderText.setTextSize(18.f);
        ListView connectionExplanationListView = findViewById(R.id.connectionColorExplanationList);
        ConnectionExplanationListAdapter connectionExplanationListAdapter =
                new ConnectionExplanationListAdapter
                        (
                            getApplicationContext(),
                            R.layout.item_connection_explanation_list,
                            symbolList,
                            explanationText,
                            colorList
                        );

        connectionExplanationListView.setAdapter(connectionExplanationListAdapter);
    }

    /**
     * Create the list of symbols for the list view.
     */
    private void createSymbolList() {
        symbolList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String symbol = this.getString(R.string.circle_symbol);
            symbolList.add(symbol);
        }

        symbolList.add(getString(R.string.last_received,""));
        symbolList.add(getString(R.string.last_sent,""));
    }

    /**
     * Create a list of strings of explanation texts
     */
    private void createExplanationTextList() {
        List<Integer> ids = new ArrayList<>();
        ids.add(R.string.connected);
        ids.add(R.string.connecting);
        ids.add(R.string.cannot_connect);
        ids.add(R.string.time_since_received);
        ids.add(R.string.time_since_sent);

        explanationText = new String[ids.size()];
        for(int i=0; i<ids.size(); i++) {
            explanationText[i] = getString(ids.get(i));
        }
    }
}
