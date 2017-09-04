package p2pmessenger.retrac;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Bastian Wieck on 9/3/2017.
 */

public class P2pApplication extends Application {
    private static P2pApplication app;
    Activity mActivity;
    List<Map<String, String>> peers;

    private P2pApplication(){
        // fixme pick the best list
        peers = new ArrayList<>();
    }

    public static P2pApplication get(){
        if(app == null){
            app = new P2pApplication();
        }
        return app;
    }

    public void updateActivity(Activity activity){
        this.mActivity = activity;
    }

    public void addPeer(Map<String, String> record){
        // fixme check if already in list
        peers.add(record);
    }

    public void removePeer(String info){
        // FIXME
    }
}
