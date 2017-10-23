package p2pmessenger.retrac;

import android.support.v7.app.AppCompatActivity;

import java.net.InetAddress;

/**
 * Created by Bastian Wieck on 9/5/2017.
 */

public abstract class P2pActivity extends AppCompatActivity {
    public static final String ADDRESS = "A";
    public static final String FORWARD = "F";
    public static final String MESSAGE = "M";

    public abstract void notifyChange(String peer);
    public abstract void connected(String hostAddress, boolean toMe);
    public abstract void messageReceived(String message);
    public abstract void groupOwnerConnected(InetAddress address);
    public abstract void forwardMessage(String target, String from, String message);
}
