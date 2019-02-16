package com.example.pierpaolo.arduinoledcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


/**
 * Created by pierpaolo on 18/08/18.
 */

/*
Manage the connection with the bluetooth device
// TODO manage disconnection with device in the status (Broadcast Receiver?)
 */

public class ActivityBluetooth extends BaseActivity {

    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private Set<BluetoothDevice> bondedDevices;
    private UUID SPP_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );
    private MyBluetoothAdapter myBluetoothAdapter;
    private static Handler handler;
    private boolean trigger = false; // used to trigger a status change in the handler
    private boolean protection = true; // prevent multiple click while loading


    // ********************************************************************************************************
    //                                 ON CREATE
    // ********************************************************************************************************
    @SuppressLint({"HandlerLeak", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "BLUETOOTH ACTIVITY", "************************onCreate()********************" );
        super.onCreate( savedInstanceState );

        //*************************Preparation******************************
        ITEM_SELECTED = 4;
        super.setContentView( R.layout.activity_bluetooth );
        super.changeToolbarTitle( "Bluetooth" );

        //**************Reference to view object***************************
        ListView deviceList = findViewById( R.id.bluetooth_list );
        Button buttonDisconnect = findViewById( R.id.buttonDisconnect );

        //*******************Display activity only if permission are guarantee
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.BLUETOOTH ) == PackageManager.PERMISSION_GRANTED) {

            //**********************Bluetooth*********************************
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService( Context.BLUETOOTH_SERVICE );
            BluetoothAdapter bluetoothAdapter = Objects.requireNonNull( bluetoothManager ).getAdapter();
            bondedDevices = bluetoothAdapter.getBondedDevices();
            musicPlayerInfo.bluetoothDevice.clear();
            for (BluetoothDevice device : bondedDevices) {
                if (device.getName().length() != 0) {
                    musicPlayerInfo.bluetoothDevice.add( device.getName() );
                }
            }
            if (musicPlayerInfo.bluetoothDevice.size() == 0) {
                Toast.makeText( getApplicationContext(), "No paired device found, pair a device from the bluetooth settings of your phone", Toast.LENGTH_SHORT ).show();
            }

            //*********************Bluetooth Adapter***********************
            myBluetoothAdapter = new MyBluetoothAdapter( this );
            deviceList.setAdapter( myBluetoothAdapter );
            deviceList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    if (protection) {
                        // select device
                        for (BluetoothDevice device : bondedDevices) {
                            String s = device.getName();
                            String ss = musicPlayerInfo.bluetoothDevice.get( position );
                            if (s.equals( ss )) {
                                musicPlayerInfo.device = device;
                            }
                        }
                        // connect to it
                        if (musicPlayerInfo.currentDeviceConnected != position) {
                            findViewById( R.id.loadingPanel ).setVisibility( View.VISIBLE );
                            connect( position );
                            protection = false;
                        }
                    }

                }
            } );

            //********************Set status at start*************************
            findViewById( R.id.loadingPanel ).setVisibility( View.GONE );
            if (musicPlayerInfo.bluetoothSocket != null) {
                if (musicPlayerInfo.bluetoothSocket.isConnected()) {
                    changeToolbarTitle( "Bluetooth: Connected" );
                } else {
                    changeToolbarTitle( "Bluetooth: Disconnected" );
                    musicPlayerInfo.currentDeviceConnected = -1;
                    myBluetoothAdapter.notifyDataSetChanged();
                }
            } else {
                changeToolbarTitle( "Bluetooth: Disconnected" );
            }

            //*********************Set Button Disconnect method********************
            buttonDisconnect.setOnClickListener( new View.OnClickListener() {
                public void onClick(View v) {
                    if (musicPlayerInfo.bluetoothSocket != null) {
                        if (musicPlayerInfo.bluetoothSocket.isConnected() && protection) {
                            try {
                                musicPlayerInfo.bluetoothSocket.close();
                                musicPlayerInfo.currentDeviceConnected = -1;
                                myBluetoothAdapter.notifyDataSetChanged();
                                changeToolbarTitle( "Bluetooth: Disconnected" );
                                Toast.makeText( getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT ).show();
                            } catch (IOException e) {
                                Log.d( "BLUETOOTH ACTIVITY", "*************Failed Disconnection from Bluetooth Socket: ERROR************" );
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } );

            //*******************Handler for view element***************************
            handler = new Handler() {
                public void handleMessage(Message msg) {
                    // handle the case where the connect method is invoked
                    if (!musicPlayerInfo.bluetoothSocket.isConnected() && trigger) {
                        changeToolbarTitle( "Bluetooth: Disconnected" );
                        Toast.makeText( getApplicationContext(), "can't connect to this device", Toast.LENGTH_SHORT ).show();
                        findViewById( R.id.loadingPanel ).setVisibility( View.GONE );
                        myBluetoothAdapter.notifyDataSetChanged();
                        trigger = false;
                        protection = true;
                    }
                    if (musicPlayerInfo.bluetoothSocket.isConnected() && trigger) {
                        changeToolbarTitle( "Bluetooth: Connected" );
                        Toast.makeText( getApplicationContext(), "device connected", Toast.LENGTH_SHORT ).show();
                        findViewById( R.id.loadingPanel ).setVisibility( View.GONE );
                        myBluetoothAdapter.notifyDataSetChanged();
                        trigger = false;
                        protection = true;
                    }
                }
            };
        }
    }


    // ********************************************************************************************************
    //                                CONNECT METHOD
    // ********************************************************************************************************
    public void connect(final int position) {

        // if device is correctly selected
        if (musicPlayerInfo.device != null) {

            // if device is connected close the connection to start a new one
            if (musicPlayerInfo.bluetoothSocket != null) {
                if (musicPlayerInfo.bluetoothSocket.isConnected()) {
                    try {
                        musicPlayerInfo.bluetoothSocket.close();
                        Log.d( "ACTIVITY BLUETOOTH:", "*****************Close the connection*****************" );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d( "ACTIVITY BLUETOOTH:", "*****************device selected " + musicPlayerInfo.device.getName() + "*****************" );

            // starts a thread to manage connection
            new Thread() {
                public void run() {
                    try { // create the socket and connect to it
                        musicPlayerInfo.bluetoothSocket = musicPlayerInfo.device.createRfcommSocketToServiceRecord( SPP_UUID );
                        musicPlayerInfo.bluetoothSocket.connect();
                        musicPlayerInfo.currentDeviceConnected = position;
                        trigger = true;
                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage( msg );
                        Log.d( "ACTIVITY BLUETOOTH:", "*****************Device Correctly Connected*****************" );
                    } catch (IOException e1) { //
                        trigger = true;
                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage( msg );
                        musicPlayerInfo.currentDeviceConnected = -1;
                        Log.d( "ACTIVITY BLUETOOTH:", "*****************Socket not created*****************" );
                        e1.printStackTrace();
                        try {
                            musicPlayerInfo.bluetoothSocket.close();
                            Log.d( "ACTIVITY BLUETOOTH:", "*****************Socket closed*****************" );
                        } catch (IOException closeException) {
                            Log.d( "ACTIVITY BLUETOOTH:", "*****************Can't close the socket*****************" );
                        }
                    }
                }
            }.start();
        }
    }
}