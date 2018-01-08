package nl.tudelft.cs4160.trustchain_android.chainExplorer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.color.ChainColor;
import nl.tudelft.cs4160.trustchain_android.main.ChainExplorerInfoActivity;
import nl.tudelft.cs4160.trustchain_android.main.TrustChainActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.Peer.bytesToHex;

public class ChainExplorerAdapter extends BaseAdapter {

    static final String TAG = "ChainExplorerAdapter";

    Context context;
    List<MessageProto.TrustChainBlock> blocksList;
    HashMap<ByteString, String> peerList = new HashMap<>();

    public ChainExplorerAdapter(Context context, List<MessageProto.TrustChainBlock> blocksList, byte[] myPubKey) {
        this.context = context;
        this.blocksList = blocksList;
        // put my public key in the peerList
        peerList.put(ByteString.copyFrom(myPubKey), "me");
        peerList.put(TrustChainBlock.EMPTY_PK, "unknown");
    }

    @Override
    public int getCount() {
        return blocksList.size();
    }

    @Override
    public MessageProto.TrustChainBlock getItem(int position) {
        return blocksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Puts the data from a TrustChainBlock object into the item textview.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageProto.TrustChainBlock block = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_trustchainblock,
                    parent, false);
        }

        // Check if we already know the peer, otherwise add it to the peerList
        ByteString pubKeyByteStr = block.getPublicKey();
        ByteString linkPubKeyByteStr = block.getLinkPublicKey();

        String peerAlias = findInPeersOrAdd(pubKeyByteStr);
        String linkPeerAlias = findInPeersOrAdd(linkPubKeyByteStr);

        // Check if the sequence numbers are 0, which would mean that they are unknown
        String seqNumStr = displayStringForSequenceNumber(block.getSequenceNumber());
        String linkSeqNumStr = displayStringForSequenceNumber(block.getLinkSequenceNumber());

        // collapsed view
        TextView peer = convertView.findViewById(R.id.peer);
        TextView seqNum = convertView.findViewById(R.id.sequence_number);
        TextView linkPeer = convertView.findViewById(R.id.link_peer);
        TextView linkSeqNum = convertView.findViewById(R.id.link_sequence_number);
        TextView transaction = convertView.findViewById(R.id.transaction);

        // For the collapsed view, set the public keys to the aliases we gave them.
        peer.setText(peerAlias);
        seqNum.setText(seqNumStr);
        linkPeer.setText(linkPeerAlias);
        linkSeqNum.setText(linkSeqNumStr);
        transaction.setText(block.getTransaction().toStringUtf8());

        // expanded view
        TextView pubKey = (TextView) convertView.findViewById(R.id.pub_key);
        setOnClickListener(pubKey);
        TextView linkPubKey = (TextView) convertView.findViewById(R.id.link_pub_key);
        setOnClickListener(linkPubKey);
        TextView prevHash = (TextView) convertView.findViewById(R.id.prev_hash);
        TextView signature = (TextView) convertView.findViewById(R.id.signature);
        TextView expTransaction = (TextView) convertView.findViewById(R.id.expanded_transaction);

        pubKey.setText(bytesToHex(pubKeyByteStr.toByteArray()));
        linkPubKey.setText(bytesToHex(linkPubKeyByteStr.toByteArray()));
        prevHash.setText(bytesToHex(block.getPreviousHash().toByteArray()));
        signature.setText(bytesToHex(block.getSignature().toByteArray()));
        expTransaction.setText(block.getTransaction().toStringUtf8());

        if (peerAlias.equals("me") || linkPeerAlias.equals("me")) {
            convertView.findViewById(R.id.own_chain_indicator).setBackgroundColor(Color.GREEN);
        } else {
            convertView.findViewById(R.id.own_chain_indicator).setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    public void setOnClickListener(View view) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                Intent intent = new Intent(context, ChainExplorerActivity.class);
                intent.putExtra("publicKey", hexStringToByteArray(tv.getText().toString()));
                context.startActivity(intent);
            }
        };
        view.setOnClickListener(onClickListener);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // Check if we already know the peer, otherwise add it to the peerList
    String findInPeersOrAdd(ByteString keyByteString) {
        if (peerList.containsKey(keyByteString)) {
            return peerList.get(keyByteString);
        } else {
            String peerAlias = "peer" + (peerList.size() - 1);
            peerList.put(keyByteString, peerAlias);
            return peerAlias;
        }
    }

    // Check if the sequence numbers are 0, which would mean that they are unknown
    static String displayStringForSequenceNumber(int sequenceNumber) {
        if (sequenceNumber == 0) {
            return "unknown";
        } else {
            return String.valueOf(sequenceNumber);
        }
    }
}