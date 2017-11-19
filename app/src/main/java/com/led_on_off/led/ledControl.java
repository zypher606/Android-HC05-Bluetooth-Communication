package com.led_on_off.led;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class ledControl extends ActionBarActivity {

   // Button btnOn, btnOff, btnDis;
    ImageButton Octave_1, Octave_2, Octave_3, Octave_4, Discnt;
    TextView receivedText;
    Button readData;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgets
        Octave_1 = (ImageButton)findViewById(R.id.octave_1);
        Octave_2 = (ImageButton)findViewById(R.id.octave_2);
        Octave_3 = (ImageButton)findViewById(R.id.octave_3);
        Octave_4 = (ImageButton)findViewById(R.id.octave_4);

        Discnt = (ImageButton)findViewById(R.id.discnt);

        readData = (Button)findViewById(R.id.readData);

        receivedText = (TextView)findViewById(R.id.receivedText);

        new ConnectBT().execute(); //Call the class to connect



        // Read data from bluetooth
        readData.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

//                readData();
            }
        });





        //commands to be sent to bluetooth
        Octave_1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                resetSwitches();
                Octave_1.setImageResource(R.drawable.on);
                sendData("1");
            }
        });

        Octave_2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resetSwitches();
                Octave_2.setImageResource(R.drawable.on);
                sendData("2");      //method to turn on
            }
        });

        Octave_3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resetSwitches();
                Octave_3.setImageResource(R.drawable.on);
                sendData("3");      //method to turn on
            }
        });

        Octave_4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resetSwitches();
                Octave_4.setImageResource(R.drawable.on);
                sendData("4");      //method to turn on
            }
        });



        Discnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });


    }

    private void resetSwitches() {
        Octave_1.setImageResource(R.drawable.red);
        Octave_2.setImageResource(R.drawable.red);
        Octave_3.setImageResource(R.drawable.red);
        Octave_4.setImageResource(R.drawable.red);
//        btSocket.getInputStream().read()
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }


    private void sendData(String data) {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write(data.toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

//    private void readData() {
//        if (btSocket!=null)
//        {
//            while (true) {
//                try {
//
//                    InputStream mmInStream = btSocket.getInputStream();
//                    byte[] readBuffer = new byte[8];
//                    mmInStream.read(readBuffer);
//
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }
//    }


    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
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



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                 myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                 BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                 btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                 BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                 btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
