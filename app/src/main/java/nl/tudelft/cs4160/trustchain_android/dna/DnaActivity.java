package nl.tudelft.cs4160.trustchain_android.dna;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.EditText;

import com.androidplot.xy.XYPlot;

import java.io.IOException;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.ui.main.PeerListAdapter;

public class DnaActivity extends Activity {
    private static final String TAG = "DnaActivity";
    private static final String FRAGMENT_TAG = "dnaFragment";
    //private Network network;
    public static List<Peer> activePeersList;
    public static List<Peer> newPeersList;
    public PeerListAdapter activePeersAdapter;
    public PeerListAdapter newPeersAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dna);







        CoordinatorLayout content = findViewById(R.id.content);
        activePeersAdapter= new PeerListAdapter(getApplicationContext(),  R.layout.item_peer_connection_list, activePeersList, content);
        newPeersAdapter = new PeerListAdapter(getApplicationContext(), R.layout.item_peer_connection_list, newPeersList, content);

        Runnable refreshTask = () -> {
            while(true) {
                runOnUiThread(() -> {
                    activePeersAdapter.notifyDataSetChanged();
                    newPeersAdapter.notifyDataSetChanged();
                });
                try {
                    // update every 498 ms, because we want to display a sent/received message cue when a message was received less than 500ms ago.
                    Thread.sleep(498);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(refreshTask).start();





        //TODO how to get peer list????

    }


    public void sendMessage(View view) throws IOException {
        EditText x_val = findViewById(R.id.dna_x_value);
        String x0 = x_val.getText().toString();

        EditText x_val_2 = findViewById(R.id.dna_x_value2);
        String x1 = x_val_2.getText().toString();

        EditText y_val = findViewById(R.id.dna_y_value);
        String y0 = y_val.getText().toString();

        EditText y_val_2 = findViewById(R.id.dna_y_value2);
        String y1 = y_val_2.getText().toString();

        EditText nr_iterations = findViewById(R.id.dna_iterations);
        String itr = nr_iterations.getText().toString();

        double[] x = {Double.valueOf(x0), Double.valueOf(x1), 1.0};
        double[] y = {Double.valueOf(y0), Double.valueOf(y1), 1.0};


        XYPlot plot = (XYPlot) findViewById(R.id.plot);

        plot.clear();
        Entity entity = new Entity(x,y, plot, Integer.valueOf(itr));

        entity.run();
        plot.invalidate();


        /** TODO create network, send network introduction request to available peer
         *  TODO fix git (this project is on develop, sourcetree is on master)
         *  TODO send x+y at some point, receive x+y somehow, apply ML
         *
         *
         */



    }



}
