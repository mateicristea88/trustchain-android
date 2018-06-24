package nl.tudelft.cs4160.trustchain_android.statistics;

import android.os.Build;
import android.support.annotation.RequiresApi;

import nl.tudelft.cs4160.trustchain_android.network.NetworkStatusListener;

public interface NodeStatistics {
    @RequiresApi(api = Build.VERSION_CODES.N)
    void messageSent(NetworkStatusListener name);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void messageReceived(NetworkStatusListener name);

    @RequiresApi(api = Build.VERSION_CODES.N)
    void introductionRequestSent(NetworkStatusListener name);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void introductionRequestReceived(NetworkStatusListener name);

    @RequiresApi(api = Build.VERSION_CODES.N)
    void introductionResponseSent(NetworkStatusListener name);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void introductionResponseReceived(NetworkStatusListener name);

    @RequiresApi(api = Build.VERSION_CODES.N)
    void punctureReceived(NetworkStatusListener name);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void punctureSent(NetworkStatusListener name);

    @RequiresApi(api = Build.VERSION_CODES.N)
    void punctureRequestReceived(NetworkStatusListener name);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void punctureRequestSent(NetworkStatusListener name);

    @RequiresApi(api = Build.VERSION_CODES.N)
    void blockMessageReceived(NetworkStatusListener name);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void blockMessageSent(NetworkStatusListener name);

    @RequiresApi(api = Build.VERSION_CODES.N)
    void bytesSent(NetworkStatusListener name, int bytes);
    @RequiresApi(api = Build.VERSION_CODES.N)
    void bytesReceived(NetworkStatusListener name, int bytes);
}
