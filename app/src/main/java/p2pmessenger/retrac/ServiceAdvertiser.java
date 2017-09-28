package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * Created by Bastian Wieck on 9/2/2017.
 */

// ServiceAdvertiser receives broadcasts when the status changed.
class ServiceAdvertiser extends AsyncTask<Void,Void,Void> implements WifiP2pManager.ChannelListener{
    private static final String TAG = "ServiceAdvertiser";

    static final String SERVICE_TYPE = "_presence._tcp";
    static final String INSTANCE_NAME = "_retrac";
    static final String SSID = "ssid";
    static final String PASSWORD = "password";
    static final String NAME = "name";

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private Context mContext;

    private String mSSID;
    private String mPassword = "";

    ServiceAdvertiser(Context context) {
        this.mContext = context;
    }

    @Override
    public Void doInBackground(Void... params){
        start();
        return null;
    }

    private void start(){
        wifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        if(wifiP2pManager == null){
            Log.d(TAG,"Device doesn't support P2P");
            return;
        }
        mChannel = wifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), this);
        wifiP2pManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: clear local services");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: "+reason);
            }
        });

        mReceiver = new GroupInfoReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        this.mContext.registerReceiver(mReceiver, mIntentFilter);

        wifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "CreateGroup onSuccess: ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "CreateGroup onFailure: "+reason);
            }
        });
    }

    public void stopKeepGroup(){
        Log.d(TAG, "stopKeepGroup: ");
        try {
            this.mContext.unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        stopLocalServices();
    }

    public void stop() {
        stopKeepGroup();
        removeGroup();
    }



    // startLocalService starts a local service to advertise the SSID and the Password so that another Peer
    // can connect to this phone without user interaction.
    private void startLocalService(){
        Map<String, String> record = new HashMap<String, String>();
        record.put(NAME,P2pApplication.get().username);
        record.put(SSID,mSSID);
        record.put(PASSWORD,mPassword);

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance( INSTANCE_NAME, SERVICE_TYPE, record);

        Log.d(TAG,"Add local service SSID "+mSSID+" password "+mPassword);
        wifiP2pManager.addLocalService(mChannel, service, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.d(TAG,"Added local service");
            }

            public void onFailure(int reason) {
                Log.d(TAG,"Adding local service failed, error code " + reason);
            }
        });
    }

    private void stopLocalServices() {
        wifiP2pManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.d(TAG, "Cleared local services");
            }

            public void onFailure(int reason) {
                Log.d(TAG, "Clearing local services failed, error code " + reason);
            }
        });

    }

    private void removeGroup() {
        wifiP2pManager.removeGroup(mChannel,new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.d(TAG, "Cleared Local Group ");
            }

            public void onFailure(int reason) {
                Log.d(TAG, "Clearing Local Group failed, error code " + reason);
            }
        });
    }
    
    @Override
    public void onChannelDisconnected(){
        Log.d(TAG, "onChannelDisconnected: ");
    }

    private class GroupInfoReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            final String action = intent.getAction();
            if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                Log.d(TAG,"Wifi P2P P2pConnection Changed Action");
                getGroupInfo();
            }
        }

        private void getGroupInfo(){
            wifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                    if (wifiP2pGroup == null){
                        Log.d(TAG, "onGroupInfoAvailable: null");
                        return;
                    }
                    Log.d(TAG,"onGroupInfoAvailable");
                    mSSID = wifiP2pGroup.getNetworkName();
                    mPassword = wifiP2pGroup.getPassphrase();
                    startLocalService();
                }
            });
        }
    }
}
