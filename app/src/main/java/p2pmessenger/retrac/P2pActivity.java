package p2pmessenger.retrac;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Bastian Wieck on 9/5/2017.
 */

public abstract class P2pActivity extends AppCompatActivity {
    public abstract void notifyChange(String peer);
    public abstract void connected(String hostAddress);
    public abstract void messageReceived(String message);
}
