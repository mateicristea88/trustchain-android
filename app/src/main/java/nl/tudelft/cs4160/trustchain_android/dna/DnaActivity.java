package nl.tudelft.cs4160.trustchain_android.dna;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;

public class DnaActivity extends Activity {
    private static final String TAG = "DnaActivity";
    private static final String FRAGMENT_TAG = "dnaFragment";
    private Network network;
    public static List<Peer> activePeersList;
    public static List<Peer> newPeersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dna);
        network = Network.getInstance(getApplicationContext());

        System.out.println("HELLO");



        //TODO how to get peer list????

        //network.sendIntroductionRequest();
    }




}
