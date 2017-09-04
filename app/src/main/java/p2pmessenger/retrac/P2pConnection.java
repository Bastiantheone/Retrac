package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * Created by Bastian Wieck on 9/2/2017.
 */

// P2pConnection creates a connection to the given SSID.
// It uses WifiManager to avoid user dialog prompt.
public class P2pConnection {
    private static final String TAG = "P2pConnection";
    Context mContext;
    WifiManager mWifiManager;
    WifiP2pManager wifiP2pManager;
    WifiConfiguration mWifiConfiguration;
    ConnectionBroadcast receiver;
    WifiP2pManager.Channel mChannel;
    int netId = 0;
    String mInetAddress = "";

    public P2pConnection(Context context, String ssid, String password, String inetAddress){
        Log.d(TAG, "P2pConnection: "+ssid+", "+password+", "+inetAddress);
        this.mContext = context;
        mInetAddress = inetAddress;

        wifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = wifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "onChannelDisconnected: ");
            }
        });

        receiver = new ConnectionBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        this.mContext.registerReceiver(receiver, filter);

        this.mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.mWifiConfiguration = new WifiConfiguration();
        this.mWifiConfiguration.SSID = String.format("\"%s\"", ssid);
        this.mWifiConfiguration.preSharedKey = String.format("\"%s\"", password);

        this.netId = this.mWifiManager.addNetwork(this.mWifiConfiguration);
        this.mWifiManager.enableNetwork(this.netId, false);
        this.mWifiManager.reconnect();
    }

    public void Stop(){
        this.mWifiManager.disconnect();
    }

    public void SetInetAddress(String address){
        this.mInetAddress = address;
    }

    public String GetInetAddress(){
        return this.mInetAddress;
    }

    private class ClientSocketAsync extends AsyncTask<String,Void,Void>{
        @Override
        public Void doInBackground(String... params){
            Log.d(TAG, "doInBackground: connect socket "+params[0]);
            Socket socket = new Socket();
            try {

                socket.bind(null);
                socket.connect(new InetSocketAddress(params[0],8888), 10000);
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ConnectionBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
                WifiInfo info = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if(info != null){
                    Log.d(TAG, "onReceive: "+info.getIpAddress());
                    new ClientSocketAsync().execute(mInetAddress);
                }
            }
        }
    }
}
