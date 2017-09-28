package p2pmessenger.retrac;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Handler;

public class ConnectsActivity extends P2pActivity {
    static final String TAG = "ConnectsActivity";
    TextView textView;
    EditText editor;
    Spinner mSpinner;
    ArrayList<String> peers;
    ArrayAdapter<String>adapter;
    int selected;
    ServerSocketAsync server;
    P2pBroadcastReceiver receiver;
    IntentFilter intentFilter;
    boolean connectionInitiated;
    String hostAddress;
    SharedPreferences sharedPreferences;
    private final static String USERNAME_PREFERENCE = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connects);

        if(sharedPreferences==null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        P2pApplication.get().username = sharedPreferences.getString(USERNAME_PREFERENCE, "Default");

        // initialize widgets
        textView = (TextView)findViewById(R.id.text_field);
        mSpinner = (Spinner)findViewById(R.id.spinner);
        Button connectButton = (Button)findViewById(R.id.connect_button);
        Button stopButton = (Button) findViewById(R.id.stop_button);
        Button sendButton = (Button) findViewById(R.id.send_button);
        editor = (EditText) findViewById(R.id.edit_text);
        final Button userNameButton = (Button) findViewById(R.id.username_button);

        peers = new ArrayList<>();
        selected = -1;

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,peers);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Log.d(TAG, "onItemSelected: "+position);
                selected = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selected = -1;
            }
        });

        // create onClick Listeners for the Button
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected == -1) {
                    setText("nothing selected");
                }
                Map<String, String>record = P2pApplication.get().peers.get(selected);
                connectionInitiated = true;
                P2pConnection connection = new P2pConnection(getBaseContext(),record.get(
                        ServiceAdvertiser.SSID),record.get(ServiceAdvertiser.PASSWORD));
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                P2pApplication.get().stop();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editor.getText().toString();
                send(message,hostAddress);
            }
        });

        userNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                P2pApplication.get().username = editor.getText().toString();
                sharedPreferences.edit().putString(USERNAME_PREFERENCE,editor.getText().toString()).apply();
                Log.d(TAG, "onClick: changed username");
            }
        });

        server = new ServerSocketAsync();
        server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        receiver = new P2pBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        this.registerReceiver(receiver, intentFilter);
    }

    void setText(String text){
        textView.setText(text);
    }

    @Override
    public void onStart(){
        super.onStart();

        P2pApplication.get().updateActivity(this);
        P2pApplication.get().start();
    }

    public void send(String message, String address){
        Log.d(TAG, "send: "+message);
        Intent intent = new Intent(getBaseContext(),TransferMessageService.class);
        intent.setAction(TransferMessageService.ACTION_SEND_MESSAGE);
        Bundle bundle = new Bundle();
        bundle.putString(TransferMessageService.EXTRAS_MESSAGE,message);
        bundle.putString(TransferMessageService.EXTRAS_ADDRESS,address);
        bundle.putInt(TransferMessageService.EXTRAS_PORT,8888);
        intent.putExtras(bundle);
        startService(intent);
    }

    @Override
    public void notifyChange(String peer){
        Log.d(TAG, "notifyChange: "+peer);
        peers.add(peer);
        adapter.notifyDataSetChanged();

    }
    @Override
    public void connected(String hostAddress){
        Log.d(TAG, "connected: "+hostAddress);
        this.hostAddress = hostAddress;
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void messageReceived(String message){
        Log.d(TAG, "messageReceived: "+message);
        setText(message);
        server = new ServerSocketAsync();
        server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(receiver != null)
            registerReceiver(receiver,intentFilter);
    }
}
