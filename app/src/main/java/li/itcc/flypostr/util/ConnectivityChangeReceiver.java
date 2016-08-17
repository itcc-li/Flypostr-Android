package li.itcc.flypostr.util;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import li.itcc.flypostr.poiadd.UploaderTask;

/**
 * Created by Arthur on 25.09.2015.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            onReceiveOnLollipop(context);
        }
        else {
            onReceiveOnOlder(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onReceiveOnLollipop(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connectivityManager.getAllNetworks();
        boolean hasWifi = false;
        //boolean hasWlan = false;
        if (networks != null) {
            for (Network network : networks) {
                NetworkInfo info = connectivityManager.getNetworkInfo(network);
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isConnected() && info.isAvailable())
                        hasWifi = true;
                }
                //if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                //    if (info.isConnected() && info.isAvailable())
                //       hasWlan = true;
                //}
            }
        }
        if (hasWifi) {
            try {
                Thread.sleep(5000);
            }
            catch (Exception x) {
            }
            new UploaderTask(context).execute((Void)null);
        }
    }

    @SuppressWarnings("deprecation")
    private void onReceiveOnOlder(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        boolean hasWifi = false;
        //boolean hasWlan = false;
        if (networkInfos != null) {
            for (NetworkInfo info : networkInfos) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isConnected() && info.isAvailable())
                        hasWifi = true;
                }
                //if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                //    if (info.isConnected() && info.isAvailable())
                //       hasWlan = true;
                //}
            }
        }
        if (hasWifi) {
            try {
                Thread.sleep(5000);
            }
            catch (Exception x) {
            }
            new UploaderTask(context).execute((Void)null);
        }
    }

}
