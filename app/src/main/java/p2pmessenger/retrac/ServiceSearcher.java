package p2pmessenger.retrac;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Map;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;

/**
 * Created by Bastian Wieck on 9/2/2017.
 */

class ServiceSearcher extends AsyncTask<Void,Void,Void> implements WifiP2pManager.ChannelListener {
    private static final String TAG = "ServiceSearcher";
    static final int SERVICE_DISCOVERING_INTERVAL = 10000;

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    WifiP2pServiceRequest mWifiP2pServiceRequest;
    private boolean run;

    ServiceSearcher(Context context){
        super();
        this.mContext = context;
    }

    @Override
    public Void doInBackground(Void... params){
        start();
        return null;
    }

    private void start(){
        run = true;
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        if(mWifiP2pManager == null){
            Log.d(TAG,"Device doesn't support P2P");
            return;
        }

        mChannel = mWifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), this);

        mWifiP2pManager.setDnsSdResponseListeners(mChannel, null, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice wifiP2pDevice) {
                Log.d(TAG, "onDnsSdTxtRecordAvailable: "+fullDomain);
                P2pApplication.get().addPeer(record);
            }
        });
        mWifiP2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mServiceDiscoveringRunnable.run();
    }

    private void startServiceDiscovery() {
        mWifiP2pManager.removeServiceRequest(mChannel, mWifiP2pServiceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: remove service request");
                        mWifiP2pManager.addServiceRequest(mChannel, mWifiP2pServiceRequest,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "onSuccess: add service request");
                                        mWifiP2pManager.discoverServices(mChannel,
                                                new WifiP2pManager.ActionListener() {

                                                    @Override
                                                    public void onSuccess() {
                                                        Log.d(TAG, "onSuccess: service discovery started");
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(
                                                                mServiceDiscoveringRunnable,
                                                                SERVICE_DISCOVERING_INTERVAL);
                                                    }

                                                    @Override
                                                    public void onFailure(int error) {
                                                        Log.d(TAG, "onFailure: error starting service discovery "+error);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(int error) {
                                        Log.d(TAG, "onFailure: error adding service request "+error);
                                    }
                                });
                    }

                    @Override
                    public void onFailure(int reason) {
                        // react to failure of removing service request
                        Log.d(TAG, "onFailure: error removing service request: "+reason);
                    }
                });
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            if(run) {
                startServiceDiscovery();
            }
        }
    };

    public void stop(){
        run = false;
    }

    private void startPeerDiscovery(){
        if (!run)
            return;
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Discover Peers onSuccess: ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Discover Peers onFailure: "+reason);
            }
        });
    }

    @Override
    public void onChannelDisconnected(){
        Log.d(TAG, "onChannelDisconnected: ");
    }
}
