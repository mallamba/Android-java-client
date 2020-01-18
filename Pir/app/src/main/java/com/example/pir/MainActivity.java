package com.example.pir;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private  EditText eTextIP, eTextPort;
    private Button connectButton, disConn;
    private ImageView imageView;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE ;
    RSA rsa;

    private TCPClient   tcpClient;
    int index = 0;
    String path;

    private static final int SELECT_PICTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ask for external storage permission
        askPermissions();
        //get the path for the storage folder
        path = Environment.getExternalStorageDirectory().toString();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                final int ACTIVITY_SELECT_IMAGE = 1234;
                startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
            }
        });



        eTextIP = findViewById(R.id.ipInput);
        eTextPort = findViewById(R.id.portInput);
        eTextPort.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(getDrawable(R.drawable.server));
        connectButton = findViewById(R.id.connectBtn);

        //connectButton.setBackground(getDrawable(R.drawable.conn) );
        disConn = findViewById(R.id.test);
        connectButton.setOnClickListener(this);
        disConn.setEnabled(false);
        disConn.setOnClickListener(this);

        rsa = new RSA();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 1234:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();


                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                    /* Now you have choosen image in Bitmap format in object "yourSelectedImage". You can use it in way you want! */
                    imageView = findViewById(R.id.imageView);


                    //imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    imageView.setImageBitmap(yourSelectedImage);
                }
        }

    };

    private void askPermissions() {// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.connectBtn ) {
            editButtons(1);
            setTextInTextBar("Connecting to server...");
            String ipString = eTextIP.getText().toString();
            int port = eTextPort.getInputType();
            if(is_ip_address(ipString) && is_port(port)) {
                Log.d("-------------", "In do in background");
                try {

                    tcpClient = new TCPClient(ipString, port, new TCPClient.MessageCallback() {
                        @Override
                        public void callbackMessageReceiver(String message) {
                            Log.d("Received MESSAGE:  ", message);
                            dealWithMessage(message);
                            //changeImage(Bitmap.createBitmap(message));
                        }

                        @Override
                        public void callbackMessageReceiver(byte[] message) {
                            if (message != null && message.length > 10000)
                                changeImage(message);
                        }

                        @Override
                        public void callbackMessageReceiver(Bitmap message) {
                            if (message != null)
                                setImage(message);
                        }

                        @Override
                        public void callbackMessageReceiver(int message) {
                            editButtons(message);
                        }
                    }, rsa);
                    tcpClient.start();
                    //textView.setText("connected");

                } catch (NullPointerException e) {
                    Log.d("CATCH ERROR", "Caught null pointer exception");
                    e.printStackTrace();
                }

            }
            else {
                setTextInTextBar("Try again with ip & port");
                setTextInTextView("Try again with ip & port");
            }

        }
        if (v.getId() == R.id.test ) {
            setTextInTextView("Disconnecting...");
            setTextInTextBar("Disconnecting...");
            if(tcpClient != null){
                ClosingThread c = new ClosingThread(tcpClient);
                c.start();
            }
            else
                editButtons(-1);


        }

    }


    public void editButtons(final int v){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(v == 1) {
                    connectButton.setEnabled(false);
                    disConn.setEnabled(true);
                    disConn.setBackground(  getDrawable(R.drawable.mybutton)  );
                    connectButton.setBackgroundColor(getColor(R.color.colorPrimaryDark ));
                    eTextIP.setEnabled(false);
                    eTextPort.setEnabled(false);
                    setTextInTextBar("Connected to server...");
                }

                if(v == -1) {
                    connectButton.setEnabled(true);
                    disConn.setEnabled(false);
                    connectButton.setBackground(  getDrawable(R.drawable.mybutton)  );
                    disConn.setBackgroundColor(getColor(R.color.colorPrimaryDark ));
                    eTextIP.setEnabled(true);
                    eTextPort.setEnabled(true);
                    setTextInTextBar("Disconnected...");
                    setTextInTextView("Disconnected...");
                }

            }
        });
    }



    public  Boolean is_ip_address(final String str) {
        if(str == null || str.isEmpty())
            return false;

        int countDigits = 0;
        int countDots = 0;
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for(char c : str.toCharArray()){
            if(Character.isDigit(c)){
                countDigits++;
            } else if(c == '.'){
                countDots++;
            }
        }
        if(countDigits >= 4 && countDots == 3)
            return true;
        else
            return false;
    }

    public  Boolean is_port(final int nbr) {
        if( (nbr / 1000 ) >= 1 )
            return true;
        else
            return false;
    }



    public void dealWithMessage(String str){
        if(!str.isEmpty() && str.length() < 30)
            setTextInTextView(str);
        else {
            Log.d("DEAL WITH MESSAGE", "LENGTH: " + str.length());
            try{

            }catch(Exception e) {
                e.getMessage();
            }
        }
    }

    public void changeImage(final byte[] arr){
        index++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(arr != null) {
                        Bitmap pictureBitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
                        String d = dateFormat.format(new Date());

                        File file = new File(path, d + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                        FileOutputStream fOut = new FileOutputStream(file.getAbsolutePath());
                        fOut.write(arr);

                        fOut.flush(); // Not really required
                        fOut.close(); // do not forget to close the stream

                        setTextInTextBar(file.getAbsolutePath());
                        Log.d("mediastore: ", "--- --- -- - " + file.getAbsolutePath());
                        ContextWrapper cw = new ContextWrapper(getApplicationContext());

                        //imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                        imageView.setImageBitmap(pictureBitmap);
                    }

                } catch(Exception e) {
                    Log.d("XXXXXX XXXXXX XXXXX ", "--- --- -- - " + e.getMessage());
                    setNoImage();
                }
            }
        });
    }

    public void setImage(final Bitmap b){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(b);
            }
        });
    }


    public void setNoImage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //imageView = findViewById(R.id.imageView);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.b123));
            }
        });
    }

    public void setTextInTextView(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.textView);
                textView.setText(s);
            }
        });
    }

    public void setTextInTextBar(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(getWindow().getDecorView().getRootView(), s, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private class SendingThread extends Thread {
        TCPClient t;
        String message;
        SendingThread(TCPClient t, String message){
            this.t = t;
            this.message = message;
        }
        public void run(){
            if(tcpClient != null)
                tcpClient.sendMessage(message);
        }
    }

    private class ClosingThread extends Thread {
        TCPClient t;

        ClosingThread(TCPClient t){
            this.t = t;

        }
        public void run(){
            if(tcpClient != null)
                tcpClient.closeDown();
        }
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }



}
