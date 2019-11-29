package nl.tudelft.cs4160.trustchain_android.ui.main;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.NetworkConnectionService;
import nl.tudelft.cs4160.trustchain_android.util.Util;

public class PeerListAdapter extends ArrayAdapter<Peer> {
    static class ViewHolder {
        TextView mPeerId;
        TextView mConnection;
        TextView mLastSent;
        TextView mLastReceived;
        TextView mDestinationAddress;
        TextView mStatusIndicator;
        TableLayout mTableLayoutConnection;
    }

    public PeerListAdapter(Context context, int resource, List<Peer> peerConnectionList) {
        super(context, resource, peerConnectionList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
                holder.mStatusIndicator.setTextColor(getContext().getResources().getColor(R.color.colorStatusConnected));
            } else {
                holder.mStatusIndicator.setTextColor(getContext().getResources().getColor(R.color.colorStatusCantConnect));
            }
        } else {
            if (peer.isAlive()) {
                holder.mStatusIndicator.setTextColor(getContext().getResources().getColor(R.color.colorStatusConnecting));
            } else {
                holder.mStatusIndicator.setTextColor(getContext().getResources().getColor(R.color.colorStatusCantConnect));
            }
        }

        if (peer.getIpAddress() != null) {
            holder.mDestinationAddress.setText(String.format("%s:%d", peer.getIpAddress().toString().substring(1), peer.getPort()));
        }

        if (System.currentTimeMillis() - peer.getLastReceivedTime() < 500) {
            animate(holder.mLastReceived, getContext().getResources().getColor(R.color.colorReceived));
        }

        if (System.currentTimeMillis() - peer.getLastSentTime() < 500) {
            animate(holder.mLastSent, getContext().getResources().getColor(R.color.colorSent));

        }

        if (peer.isReceivedFrom()) {
            holder.mLastReceived.setText(getContext().getString(R.string.last_received,
                    Util.timeToString(System.currentTimeMillis() - peer.getLastReceivedTime())));
        }
        holder.mLastSent.setText(getContext().getString(R.string.last_sent,
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
            getContext().getResources().getColor(android.R.color.secondary_text_light));
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

    interface OnPeerClickListener {
        void onPeerClick(Peer peer);
    }
}
