package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by Bastian Wieck on 9/26/2017.
 */

public class P2pBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "P2pBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            Log.d(TAG, "onReceive: connection changed");
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pGroup wifiGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            WifiP2pInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            Log.d(TAG, "onReceive: "+networkInfo.getDetailedState());
            try{
                Log.d(TAG, "onReceive: "+info.groupOwnerAddress.getHostAddress());
            }catch (NullPointerException e){
                Log.e(TAG, "onReceive: ",e);
            }
            if(networkInfo.isConnected()){
                Log.d(TAG, "onReceive: connected");
                String hostAddress = info.groupOwnerAddress.getHostAddress();
                P2pApplication.get().clientList = wifiGroup.getClientList();
                Log.d(TAG, "onReceive: is connected owner "+wifiGroup.isGroupOwner()+" client size "+P2pApplication.get().clientList.size());
                if(P2pApplication.get().clientList.size()!=0 || !wifiGroup.isGroupOwner())
                    P2pApplication.get().mActivity.connected(hostAddress, wifiGroup.isGroupOwner());
            }
        }
        if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
            WifiInfo wiffo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if(wiffo != null){
                wiffo.getIpAddress();
            }
        }
    }
}
