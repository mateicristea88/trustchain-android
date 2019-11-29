package nl.tudelft.cs4160.trustchain_android.ui.inbox;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.storage.repository.PeerRepository;

public class InboxActivity extends AppCompatActivity {
    public static ArrayList<Peer> peerList;
    private RecyclerView mRecyclerView;
    private ArrayList<InboxItem> inboxItems = new ArrayList<>();
    private InboxAdapter mAdapter = new InboxAdapter(inboxItems);
    private PeerRepository peerRepository;
    private BlockRepository blockRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        AppDatabase database = AppDatabase.getInstance(this);
        peerRepository = new PeerRepository(database.peerDao());
        blockRepository = new BlockRepository(database.blockDao());
        mRecyclerView = findViewById(R.id.my_recycler_view);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // specify an adapter (see also next example)
        mAdapter = new InboxAdapter(inboxItems);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Update the inbox counters. A new adapter is created based on the new state of the inboxes,
     * which is then set in the recyclerview. This has to run on the main UI thread because
     * only the original thread that created a view hierarchy can touch its views.
     */
    private void getInboxItems() {
        mAdapter.setPeerList(peerList);
        mRecyclerView.setAdapter(mAdapter);

        peerRepository.getAllPeers().observe(this, peers -> {
            if (peers != null) {
                inboxItems.clear();
                for (Peer peer : peers) {
                    // get half blocks from block repository
                    int halfBlockCount = blockRepository.getHalfBlockCount(
                            peer.getPublicKeyPair().toBytes(),
                            Key.loadPublicKeyPair(getApplicationContext()).toBytes()
                    );
                    inboxItems.add(new InboxItem(peer, halfBlockCount));
                }
                Collections.reverse(inboxItems);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getInboxItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu);
        return true;
    }

    /**
     * Define what should be executed when one of the item in the menu is clicked.
     *
     * @param item the item in the menu.
     * @return true if everything was executed.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_inbox:
                peerRepository.deleteAllPeers();
                inboxItems.clear();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return true;
        }
    }

}
