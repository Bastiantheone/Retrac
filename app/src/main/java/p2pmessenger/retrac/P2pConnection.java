package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
class P2pConnection {
    private static final String TAG = "P2pConnection";
    static final int PORT = 5743;

    private Context mContext;
    private WifiManager mWifiManager;
    private WifiConfiguration mWifiConfiguration;
    private ConnectionBroadcast receiver;
    private int netId = 0;
    private String mInetAddress = "";

    ClientSocketAsync client;

    P2pConnection(Context context, String ssid, String password, String inetAddress){
        Log.d(TAG, "P2pConnection: "+ssid+", "+password+", "+inetAddress);
        this.mContext = context;
        mInetAddress = inetAddress;

        P2pApplication.get().stopDiscovery();


        receiver = new ConnectionBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.mContext.registerReceiver(receiver, filter);

        this.mWifiManager = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.mWifiConfiguration = new WifiConfiguration();
        this.mWifiConfiguration.SSID = String.format("\"%s\"", ssid);
        this.mWifiConfiguration.preSharedKey = String.format("\"%s\"", password);

        this.netId = this.mWifiManager.addNetwork(this.mWifiConfiguration);
        this.mWifiManager.enableNetwork(this.netId, false);
        this.mWifiManager.reconnect();
    }

    public void stop(){
        this.mWifiManager.disconnect();
        client.cancel(true);

    }

    private class ClientSocketAsync extends AsyncTask<Void,Void,Socket>{
        String mInetAddress;
        ClientSocketAsync(String inetAddress){
            super();
            Log.d(TAG, "ClientSocketAsync: ");
            mInetAddress = inetAddress;
        }
        @Override
        public Socket doInBackground(Void... params){
            Log.d(TAG, "doInBackground: connect socket "+mInetAddress);
            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(mInetAddress,PORT), 5000);
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }catch (Exception e){
                e.printStackTrace();
            }
            return socket;
        }

        @Override
        public void onPostExecute(Socket socket){
            if (socket != null) {
                Log.d(TAG, "onPostExecute: success");
                P2pApplication.get().connected(socket);
            }
        }
    }

    private class ConnectionBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
                WifiInfo info = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if(info != null){
                    Log.d(TAG, "Network changed onReceive: "+info.getSSID()+" Inet: "+mInetAddress);

                    client = new ClientSocketAsync(mInetAddress);
                    client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }
}
