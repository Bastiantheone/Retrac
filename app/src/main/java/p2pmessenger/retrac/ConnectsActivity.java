package p2pmessenger.retrac;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class ConnectsActivity extends P2pActivity {
    static final String TAG = "ConnectsActivity";
    TextView textView;
    Spinner mSpinner;
    ArrayList<String> peers;
    ArrayAdapter<String>adapter;
    int selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connects);

        // initialize widgets
        textView = (TextView)findViewById(R.id.text_field);
        mSpinner = (Spinner)findViewById(R.id.spinner);
        Button connectButton = (Button)findViewById(R.id.connect_button);
        Button stopButton = (Button) findViewById(R.id.stop_button);

        peers = new ArrayList<>();
        selected = -1;

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,peers);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
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
                P2pConnection connection = new P2pConnection(getBaseContext(),record.get(
                        ServiceAdvertiser.SSID),record.get(ServiceAdvertiser.PASSWORD),
                        record.get(ServiceAdvertiser.INET_ADDRESS));
                P2pApplication.get().setConnection(connection);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                P2pApplication.get().stop();
            }
        });
    }

    void setText(String text){
        textView.setText(text);
    }

    @Override
    public void onStop(){
        super.onStop();

        // P2pApplication.get().stop();
    }

    @Override
    public void onStart(){
        super.onStart();

        P2pApplication.get().updateActivity(this);
        P2pApplication.get().start();
    }

    @Override
    public void notifyChange(String text){
        String current = textView.getText().toString();
        setText(current+text);
        peers.add(text);
        adapter.notifyDataSetChanged();

    }
}
