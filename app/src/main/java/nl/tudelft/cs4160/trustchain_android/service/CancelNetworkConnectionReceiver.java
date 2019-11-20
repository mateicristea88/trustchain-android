package nl.tudelft.cs4160.trustchain_android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelNetworkConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, NetworkConnectionService.class);
        context.stopService(serviceIntent);
    }
}
