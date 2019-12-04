package nl.tudelft.cs4160.trustchain_android.ui.chainexplorer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.storage.repository.PeerRepository;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

import static android.view.Gravity.CENTER;

/**
 * This activity will show a chain of a given TrustChain peer.
 */
public class ChainExplorerActivity extends AppCompatActivity {
    BlockRepository blockRepository;
    PeerRepository peerRepository;
    ChainExplorerAdapter adapter;
    ListView blocksList;


    static final String TAG = "ChainExplorerActivity";
    private static final String TITLE = "My chain overview";

    public static final String BUNDLE_EXTRAS_PUBLIC_KEY = "publicKey";

    private DualSecret kp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_explorer);
        blocksList = findViewById(R.id.blocks_list);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(GridLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, CENTER));
        progressBar.setIndeterminate(true);
        blocksList.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = findViewById(android.R.id.content);
        root.addView(progressBar);


        kp = Key.loadKeys(getApplicationContext());
        init();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chainexplorer, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                Intent chainExplorerInfoActivity = new Intent(this, ChainExplorerInfoActivity.class);
                startActivity(chainExplorerInfoActivity);
                return true;
            default:
                return true;
        }
    }

    private byte[] retrievePublicKeyPair() {
        if (getIntent().hasExtra(BUNDLE_EXTRAS_PUBLIC_KEY )) {
            return getIntent().getByteArrayExtra(BUNDLE_EXTRAS_PUBLIC_KEY);
        }
        return kp.getPublicKeyPair().toBytes();
    }

    /**
     * Initialize the variables.
     */
    private void init() {
        AppDatabase database = AppDatabase.getInstance(this);
        blockRepository = new BlockRepository(database.blockDao());
        peerRepository = new PeerRepository(database.peerDao());
        byte[] publicKeyPair = retrievePublicKeyPair();
        Log.i(TAG, "Using " + Arrays.toString(publicKeyPair) + " as public keypair");
        try {
            List<MessageProto.TrustChainBlock> blocks = blockRepository.getBlocks(publicKeyPair, true);
            if (blocks.size() > 0) {
                byte[] ownPubKey = kp.getPublicKeyPair().toBytes();
                byte[] firstPubKey = blocks.get(0).getPublicKey().toByteArray();
                if (Arrays.equals(ownPubKey, firstPubKey)){
                    this.setTitle(TITLE);
                } else {
                    Peer peer = peerRepository.getByPublicKey(blocks.get(0).getPublicKey().toByteArray());
                    String username = (peer != null) ? peer.getName() : "unknown peer";
                    this.setTitle("Chain of " + username);
                }
                adapter = new ChainExplorerAdapter(this, peerRepository, blocks,
                        kp.getPublicKeyPair().toBytes(), publicKeyPair);
                blocksList.setAdapter(adapter);
            } else {
                // ToDo display empty chain
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        blocksList.setOnItemClickListener((parent, view, position, id) -> {
            LinearLayout expandedItem = view.findViewById(R.id.expanded_item);
            ImageView expandArrow = view.findViewById(R.id.expand_arrow);

            // Expand the item when it is clicked
            if (expandedItem.getVisibility() == View.GONE) {
                expandedItem.setVisibility(View.VISIBLE);
                Log.v(TAG, "Item height: " + expandedItem.getHeight());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    expandArrow.setImageDrawable(getDrawable(R.drawable.ic_expand_less_black_24dp));
                } else {
                    expandArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                }
            } else {
                expandedItem.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    expandArrow.setImageDrawable(getDrawable(R.drawable.ic_expand_more_black_24dp));
                } else {
                    expandArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
                }
            }
        });
    }

}