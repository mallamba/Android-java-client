package com.example.pir;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient extends Thread {

    private static final String TAG = "TCPClient";
    int port;

    private String ipNumber, incomingMessage, XOR_KEY;
    BufferedReader in;
    PrintWriter out;
    Socket socket;
    private MessageCallback listener = null;
    private boolean mRun = false;
    RSA rsa;


    /**
     * TCPClient class constructor, which is created in AsyncTasks after the button click.
     */
    public TCPClient(String ip, int port, MessageCallback listener, RSA rsa) {
        this.listener = listener;
        this.ipNumber = ip;
        this.port = port;
        this.rsa = rsa;
    }

    /**
     * Public method for sending the message via OutputStream object.
     *
     * @param message Message passed as an argument and sent via OutputStream object.
     */
    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }


    public void closeDown() {
        try {
            out.flush();
            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            Log.d("fail fail fail: ", "Socket Closed");
            e.printStackTrace();
        }
        Log.d("SOCKET CLOSE: ", "Socket Closed");
        listener.callbackMessageReceiver(-1);
    }

    /**
     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
     */
    public void stopClient() {
        Log.d(TAG, "Client stopped!");
        mRun = false;
    }


    public boolean isRunning() {
        return mRun;
    }

    public void run() {
        mRun = true;
        try {
            String a = "192.168.1.33";
            InetAddress serverAddress = InetAddress.getByName(ipNumber);
            listener.callbackMessageReceiver("Connecting...");
            socket = new Socket(serverAddress, port);
            listener.callbackMessageReceiver("Connected" + a);
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                this.sendMessage(rsa.getE() + "");
                this.sendMessage(rsa.getN() + "");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                XOR_KEY = in.readLine();
                try {
                    XOR_KEY =  rsa.RSADecrypt(Integer.parseInt(XOR_KEY)).toString();
                } catch (Exception e) {
                    Log.d("------------- : ", "error ParseINT " + e.getMessage());
                }
                listener.callbackMessageReceiver(1);
                int size;
                String imageString = "";
                while (mRun) {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedInputStream ind = new BufferedInputStream(socket.getInputStream());
                    String message = in.readLine();
                    if (message.startsWith("xxx")) {
                        if (message.startsWith("xxxsize")) {
                            message = message.substring(7);
                            size = Integer.parseInt(message);
                            byte[] byteArray = new byte[size];
                            for (int read = 0; read < size;) { // read until the byte
                                read += ind.read(byteArray, read, size - read);
                            }
                            if(size > 1000)
                                listener.callbackMessageReceiver( XOR.byteArrayEncrypt( byteArray, XOR_KEY) );
                        }
                    }
                }

            } catch (Exception e) {
                listener.callbackMessageReceiver(-1);
            } finally {
                out.flush();
                out.close();
                in.close();
                socket.close();
                listener.callbackMessageReceiver(-1);
            }

        } catch (Exception e) {
        }

    }

    public Bitmap bm(InputStream is) {
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        Log.d("DEAL WITH MESSAGE", "image change..");
        return bitmap;
    }

    /**
     * Callback Interface for sending received messages to 'onPublishProgress' method in AsyncTask.
     */
    public interface MessageCallback {
        /**
         * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
         *
         * @param message Received message from server app.
         */
        public void callbackMessageReceiver(String message);
        public void callbackMessageReceiver(int message);
        public void callbackMessageReceiver(byte[] message);
        public void callbackMessageReceiver(Bitmap message);

    }


}