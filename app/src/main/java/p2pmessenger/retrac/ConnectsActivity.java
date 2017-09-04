package p2pmessenger.retrac;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Map;

public class ConnectsActivity extends AppCompatActivity {
    static final String TAG = "ConnectsActivity";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connects);

        // initialize widgets
        textView = (TextView)findViewById(R.id.text_field);
        Button sendButton = (Button)findViewById(R.id.send_button);
        Button receiveButton = (Button)findViewById(R.id.receive_button);
        Button connectButton = (Button)findViewById(R.id.connect_button);

        // create onClick Listeners for the Buttons
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceSearcher serviceSearcher = new ServiceSearcher(getApplicationContext());
                serviceSearcher.start();
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceAdvertiser serviceAdvertiser = new ServiceAdvertiser(getApplicationContext());
                serviceAdvertiser.start();
            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String>record = P2pApplication.get().peers.get(0);
                textView.setText(record.get("name"));
                P2pConnection connection = new P2pConnection(getBaseContext(),record.get("SSID"),record.get("Password"),record.get("InetAddress"));
            }
        });
    }

    void setText(String text){
        textView.setText(text);
    }
}
