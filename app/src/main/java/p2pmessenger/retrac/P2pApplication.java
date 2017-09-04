package p2pmessenger.retrac;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Bastian Wieck on 9/3/2017.
 */

class P2pApplication extends Application {
    private static P2pApplication app;
    Activity mActivity;
    List<Map<String, String>> peers;

    private P2pApplication(){
        peers = new ArrayList<>();
    }

    static P2pApplication get(){
        if(app == null){
            app = new P2pApplication();
        }
        return app;
    }

    void updateActivity(Activity activity){
        this.mActivity = activity;
    }

    void addPeer(Map<String, String> record){
        int ind = 0;
        for(Map<String, String> item : peers){
            if(item.get(ServiceAdvertiser.NAME).equals(record.get(ServiceAdvertiser.NAME))){
                // updating the record instead of creating a new one
                peers.add(ind,record);
                return;
            }
            ind++;
        }
        peers.add(record);
    }

    void removePeer(String info){
        // FIXME
    }
}
