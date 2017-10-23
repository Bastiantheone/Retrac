package p2pmessenger.retrac;

import android.app.Activity;
import android.app.Application;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bastian Wieck on 9/3/2017.
 */

class P2pApplication extends Application {
    private static final String TAG = "P2pApplication";
    private static P2pApplication app;
    protected String username;
    P2pActivity mActivity;
    List<Map<String, String>> peers;
    Collection<WifiP2pDevice> clientList;

    ServiceSearcher serviceSearcher;
    ServiceAdvertiser serviceAdvertiser;

    private P2pApplication(){
        peers = new ArrayList<>();
    }

    static P2pApplication get(){
        if(app == null){
            app = new P2pApplication();
        }
        return app;
    }

    void connected(Socket socket){
        
    }

    void updateActivity(P2pActivity activity){
        this.mActivity = activity;
    }

    void addPeer(Map<String, String> record){
        // FIXME add time stamp and add regular check to see if service is still advertised
        record.put(ServiceAdvertiser.DIRECT, "T");
        int ind = 0;
        boolean exists = false;
        for(Map<String, String> item : peers){
            if(item.get(ServiceAdvertiser.NAME).equals(record.get(ServiceAdvertiser.NAME))){
                // updating the record instead of creating a new one
                peers.add(ind,record);
                exists = true;
                break;
            }
            ind++;
        }
        if(!exists) {
            mActivity.notifyChange(record.get(ServiceAdvertiser.NAME));
            peers.add(record);
        }
        if(record.get(ServiceAdvertiser.NEIGHBORS).length() != 0){ // There are neighbors
            String neighborString = record.get(ServiceAdvertiser.NEIGHBORS);
            String[] neighbors = neighborString.split("-");
            for(String neighbor : neighbors) {
                exists = false;
                ind = 0;
                for (Map<String, String> item : peers) {
                    if (item.get(ServiceAdvertiser.NAME).equals(neighbor)) {
                        exists = true;
                        if(item.get(ServiceAdvertiser.DIRECT).equals("F")){
                            Map<String, String> newRecord = new HashMap<String, String>();
                            newRecord.put(ServiceAdvertiser.NAME, neighbor);
                            newRecord.put(ServiceAdvertiser.SSID, record.get(ServiceAdvertiser.SSID));
                            newRecord.put(ServiceAdvertiser.PASSWORD, record.get(ServiceAdvertiser.PASSWORD));
                            newRecord.put(ServiceAdvertiser.ADDRESS, record.get(ServiceAdvertiser.ADDRESS));
                            newRecord.put(ServiceAdvertiser.DIRECT, "F");
                            peers.add(ind, newRecord);
                        }
                        break;
                    }
                    ind ++;
                }
                if(!exists){
                    Log.d(TAG, "addPeer: new neighbor added");
                    Map<String, String> newRecord = new HashMap<String, String>();
                    newRecord.put(ServiceAdvertiser.NAME, neighbor);
                    newRecord.put(ServiceAdvertiser.SSID, record.get(ServiceAdvertiser.SSID));
                    newRecord.put(ServiceAdvertiser.PASSWORD, record.get(ServiceAdvertiser.PASSWORD));
                    newRecord.put(ServiceAdvertiser.ADDRESS, record.get(ServiceAdvertiser.ADDRESS));
                    newRecord.put(ServiceAdvertiser.DIRECT, "F");
                    mActivity.notifyChange(neighbor);
                    peers.add(newRecord);
                }
            }
        }
    }

    void removePeer(String info){
        // FIXME
    }

    public void start(){
        Log.d(TAG, "start: ");
        serviceSearcher = new ServiceSearcher(mActivity.getApplicationContext());
        serviceSearcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        serviceAdvertiser = new ServiceAdvertiser(mActivity.getApplicationContext());
        serviceAdvertiser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stopKeepGroup(){
        Log.d(TAG, "stopKeepGroup: ");
        try {
            serviceSearcher.stop();
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }try {
            serviceSearcher.cancel(true);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void stop(){
        Log.d(TAG, "stop: ");
        stopDiscovery();
        try {
            serviceAdvertiser.stop();
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }try {
            serviceAdvertiser.cancel(true);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try {
            serviceSearcher.stop();
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }try {
            serviceSearcher.cancel(true);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    
    void stopDiscovery(){
        Log.d(TAG, "stopDiscovery: ");
        serviceAdvertiser.stop();
        serviceSearcher.stop();
        serviceSearcher.cancel(true);
        serviceAdvertiser.cancel(false);
    }
}
