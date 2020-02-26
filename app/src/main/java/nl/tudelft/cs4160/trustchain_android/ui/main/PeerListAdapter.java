package nl.tudelft.cs4160.trustchain_android.ui.main;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.NetworkConnectionService;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.Util;

public class PeerListAdapter extends ArrayAdapter<Peer> {
    private final Context context;
    private CoordinatorLayout coordinatorLayout;

    static class ViewHolder {
        TextView mPeerId;
        TextView mConnection;
        TextView mLastSent;
        TextView mLastReceived;
        TextView mDestinationAddress;
        TextView mStatusIndicator;
        TableLayout mTableLayoutConnection;
    }

    public PeerListAdapter(Context context, int resource, List<Peer> peerConnectionList, CoordinatorLayout coordinatorLayout) {
        super(context, resource, peerConnectionList);
        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.item_peer_connection_list, parent, false);

            holder = new ViewHolder();
            holder.mStatusIndicator = convertView.findViewById(R.id.status_indicator);
            holder.mConnection = convertView.findViewById(R.id.connection);
            holder.mPeerId = convertView.findViewById(R.id.peer_id);
            holder.mLastSent = convertView.findViewById(R.id.last_sent);
            holder.mLastReceived = convertView.findViewById(R.id.last_received);
            holder.mDestinationAddress = convertView.findViewById(R.id.destination_address);
            holder.mTableLayoutConnection = convertView.findViewById(R.id.tableLayoutConnection);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Peer peer = getItem(position);

        holder.mPeerId.setText(peer.getName() == null ? "" : peer.getName());
        if (peer.getConnectionType() != -1) {
            if (NetworkConnectionService.CONNECTABLE_ADDRESS.equals(peer.getIpAddress().getHostAddress())) {
                holder.mConnection.setText("Server");
            } else {
                holder.mConnection.setText(connectionTypeString(peer.getConnectionType()));
            }
        } else {
            if(peer.isReceivedFrom()) {
                holder.mConnection.setText("unknown");
            } else {
                holder.mConnection.setText("");
            }
        }

        if (peer.isReceivedFrom()) {
            if (peer.isAlive()) {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusConnected));
            } else {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusCantConnect));
            }
        } else {
            if (peer.isAlive()) {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusConnecting));
            } else {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusCantConnect));
            }
        }

        if (peer.getIpAddress() != null) {
            holder.mDestinationAddress.setText(String.format("%s:%d", peer.getIpAddress().toString().substring(1), peer.getPort()));
        }

        if (System.currentTimeMillis() - peer.getLastReceivedTime() < 500) {
            animate(holder.mLastReceived, context.getResources().getColor(R.color.colorReceived));
        }

        if (System.currentTimeMillis() - peer.getLastSentTime() < 500) {
            animate(holder.mLastSent, context.getResources().getColor(R.color.colorSent));

        }

        setOnClickListener(holder.mTableLayoutConnection, position);

        if(peer.isReceivedFrom()) {
            holder.mLastReceived.setText(context.getString(R.string.last_received,
                    Util.timeToString(System.currentTimeMillis() - peer.getLastReceivedTime())));
        }
        holder.mLastSent.setText(context.getString(R.string.last_sent,
                Util.timeToString(System.currentTimeMillis() - peer.getLastSentTime())));

        return convertView;
    }

    /**
     * Animate the textview so it changes color for 500ms
     * @param view The textview that will change color
     * @param toColor The color that it will change to
     */
    private void animate(final TextView view, int toColor) {
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(view, "textColor",
            toColor,
            context.getResources().getColor(android.R.color.secondary_text_light));
        colorAnim.setDuration(500);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();
    }

    private String connectionTypeString(int connectionType) {
        switch (connectionType) {
            case ConnectivityManager.TYPE_WIFI:
                return "WiFi";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "Bluetooth";
            case ConnectivityManager.TYPE_ETHERNET:
                return "Ethernet";
            case ConnectivityManager.TYPE_MOBILE:
                return "Mobile";
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return "Mobile dun";
            case ConnectivityManager.TYPE_VPN:
                return "VPN";
            default:
                return "Unknown";
        }
    }

    /**
     * On click peer. If it's possible to add this peer
     * to your inbox this happens, otherwise a snackbar message
     * will explain why this isn't possible.
     * @param mTableLayoutConnection
     * @param position click position
     */
    private void setOnClickListener(TableLayout mTableLayoutConnection, int position) {
        mTableLayoutConnection.setTag(position);
        View.OnClickListener onClickListener = v -> {
            int pos = (int) v.getTag();
            Peer peer = getItem(pos);
            if(peer.isAlive() && peer.isReceivedFrom()) {
                PublicKeyPair pubKeyPair = PubKeyAndAddressPairStorage.getPubKeyByAddress(context, peer.getAddress().getHostString() + ":" + peer.getPort());
                if(pubKeyPair != null) {
                    InboxItem i = new InboxItem(peer, new ArrayList<>());
                    UserNameStorage.setNewPeerByPublicKey(context, peer.getName(), pubKeyPair);
                    InboxItemStorage.addInboxItem(context, i);
                    Snackbar mySnackbar = Snackbar.make(coordinatorLayout,
                            context.getString(R.string.snackbar_peer_added,peer.getName()), Snackbar.LENGTH_SHORT);
                    // set max lines so long peer names will still display properly
                    TextView snackbarTextView = mySnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbarTextView.setMaxLines(1);
                    snackbarTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                    mySnackbar.show();
                } else {
                    Snackbar mySnackbar = Snackbar.make(coordinatorLayout,
                            context.getString(R.string.snackbar_no_pub_key), Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(coordinatorLayout,
                        context.getString(R.string.snackbar_peer_inactive), Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        };
        mTableLayoutConnection.setOnClickListener(onClickListener);
    }
}
