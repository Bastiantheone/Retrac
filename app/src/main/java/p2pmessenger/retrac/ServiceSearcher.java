package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.telephony.ServiceState;
import android.util.Log;

import java.util.Map;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;

/**
 * Created by Bastian Wieck on 9/2/2017.
 */

public class ServiceSearcher implements WifiP2pManager.ChannelListener {
    private static final String TAG = "ServiceSearcher";

    Context mContext;
    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;
    IntentFilter mIntentFilter;
    GroupInfoReceiver mReceiver;
    boolean run;

    public ServiceSearcher(Context context){
        this.mContext = context;
    }

    public void start(){
        run = true;
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        if(mWifiP2pManager == null){
            Log.d(TAG,"Device doesn't support P2P");
            return;
        }

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mReceiver = new GroupInfoReceiver();
        mContext.registerReceiver(mReceiver,mIntentFilter);

        mChannel = mWifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), this);
        mWifiP2pManager.setDnsSdResponseListeners(mChannel, new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String serviceType, WifiP2pDevice wifiP2pDevice) {
                Log.d(TAG, "onDnsSdServiceAvailable: "+instanceName);
                startPeerDiscovery();
            }
        }, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
                Log.d(TAG, "onDnsSdTxtRecordAvailable: "+fullDomain);
                P2pApplication.get().addPeer(record);
            }
        });
        startPeerDiscovery();
    }

    public void stop(){
        mContext.unregisterReceiver(mReceiver);
        run = false;
    }

    public void startPeerRequest(){
        mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                Log.d(TAG,"onPeersAvailabe");
                if(wifiP2pDeviceList.getDeviceList().isEmpty()){
                    startPeerDiscovery();
                }else{
                    startServiceDiscovery();
                }
            }
        });
    }

    public void startPeerDiscovery(){
        if (!run) // stop the looping
            return;
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: "+reason);
            }
        });
    }

    void startServiceDiscovery(){
        Log.d(TAG,"startServiceDiscovery");
        WifiP2pDnsSdServiceRequest request = WifiP2pDnsSdServiceRequest.newInstance();
        final Handler handler = new Handler();
        mWifiP2pManager.addServiceRequest(mChannel, request, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Added service request");
                handler.postDelayed(new Runnable() {
                    // There are supposedly a possible race-condition bug with the service discovery
                    // thus to avoid it, we are delaying the service discovery start here
                    @Override
                    public void run() {
                        mWifiP2pManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Started service discovery");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "Starting service discovery failed, error code " + reason);
                            }
                        });
                    }
                }, 1000);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Adding service request failed, error code " + reason);
            }
        });
    }

    @Override
    public void onChannelDisconnected(){
        Log.d(TAG, "onChannelDisconnected: ");
    }

    // GroupInfoReceiver gets the group information
    private class GroupInfoReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if (WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // ready to requestPeers
                startPeerRequest();
            }
        }
    }
}
