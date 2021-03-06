package p2pmessenger.retrac;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Bastian Wieck on 9/26/2017.
 */

public class ServerSocketAsync extends AsyncTask<Void, Void, String> {
    private static final String TAG = "ServerSocketAsync";
    private ServerSocket serverSocket;
    private InetAddress address;

    @Override
    protected String doInBackground(Void... params){
        Log.d(TAG, "doInBackground: ");
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(8888));
            Socket server = serverSocket.accept();
            Log.d(TAG, "doInBackground: socket.accept");

            InputStream inputStream = server.getInputStream();
            address = server.getInetAddress();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return sb.toString();
        }catch (IOException e){
            Log.e(TAG, "doInBackground: ",e );
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result){
        Log.d(TAG, "onPostExecute: "+result);
        try{
            serverSocket.close();
            Log.d(TAG, "onPostExecute: server socket closed");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if(result!=null) {
            if(result.startsWith(P2pActivity.MESSAGE))
                P2pApplication.get().mActivity.messageReceived(result.substring(1));
            else if(result.startsWith(P2pActivity.ADDRESS))
                P2pApplication.get().mActivity.groupOwnerConnected(address);
            else if(result.startsWith(P2pActivity.FORWARD)) {
                int i = result.indexOf(P2pActivity.FORWARD, 2);
                int j = result.indexOf(P2pActivity.FORWARD, i+2);
                String target = result.substring(1,i);
                String from = result.substring(i+1, j);
                String message = result.substring(j+1);
                P2pApplication.get().mActivity.forwardMessage(target, from, message);
            }
        }
    }
}
