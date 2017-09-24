package p2pmessenger.retrac;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Bastian Wieck on 9/3/2017.
 */

class P2pApplication extends Application {
    private static final String TAG = "P2pApplication";
    private static P2pApplication app;
    P2pActivity mActivity;
    List<Map<String, String>> peers;

    ServiceSearcher serviceSearcher;
    ServiceAdvertiser serviceAdvertiser;
    P2pConnection connection;

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
        int ind = 0;
        for(Map<String, String> item : peers){
            if(item.get(ServiceAdvertiser.NAME).equals(record.get(ServiceAdvertiser.NAME))){
                // updating the record instead of creating a new one
                peers.add(ind,record);
                return;
            }
            ind++;
        }
        mActivity.notifyChange(record.get(ServiceAdvertiser.NAME));
        peers.add(record);
    }

    void removePeer(String info){
        // FIXME
    }

    public void setConnection(P2pConnection connection){
        this.connection = connection;
    }

    public void start(){
        Log.d(TAG, "start: ");
        serviceSearcher = new ServiceSearcher(mActivity.getApplicationContext());
        serviceSearcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        serviceAdvertiser = new ServiceAdvertiser(mActivity.getApplicationContext());
        serviceAdvertiser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stop(){
        Log.d(TAG, "stop: ");
        stopDiscovery();
        if(connection!=null){
            connection.stop();
        }
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
