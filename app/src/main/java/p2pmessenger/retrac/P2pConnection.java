package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Handler;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * Created by Bastian Wieck on 9/2/2017.
 */

// P2pConnection creates a connection to the given SSID.
// It uses WifiManager to avoid user dialog prompt.
class P2pConnection {
    private static final String TAG = "P2pConnection";

    private Context mContext;
    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2PManager;
    private WifiP2pManager.Channel mChannel;
    private WifiConfiguration mWifiConfiguration;
    private String mAddress;
    private int netId = 0;

    P2pConnection(Context context, String ssid, String password, String address){
        Log.d(TAG, "P2pConnection: "+ssid+", "+password);
        mAddress = address;
        this.mContext = context;

        P2pApplication.get().stopDiscovery();

        this.mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.mWifiConfiguration = new WifiConfiguration();
        this.mWifiConfiguration.SSID = String.format("\"%s\"", ssid);
        this.mWifiConfiguration.preSharedKey = String.format("\"%s\"", password);

        this.netId = this.mWifiManager.addNetwork(this.mWifiConfiguration);
        this.mWifiManager.enableNetwork(this.netId, false);
        this.mWifiManager.reconnect();
        Log.d(TAG, "P2pConnection: ready");
        mWifiP2PManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2PManager.initialize(this.mContext, this.mContext.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "onChannelDisconnected: ");
            }
        });
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(runnable, 10000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            P2pApplication.get().mActivity.connected(mAddress,false);
        }
    };
}
