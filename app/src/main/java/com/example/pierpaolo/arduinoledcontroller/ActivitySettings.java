package com.example.pierpaolo.arduinoledcontroller;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by pierpaolo on 08/04/18.
 */

public class ActivitySettings extends BaseActivity {

    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private DataManager dataManager = new DataManager();
    private Switch readAndWritePermissionSwitch;



    // ********************************************************************************************************
    //                ON CREATE
    // ********************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "SETTINGS ACTIVITY:", "*****************onCreate()*****************" );
        super.onCreate( savedInstanceState );

        //*****************************Preparation***************************
        ITEM_SELECTED = 5;
        super.setContentView( R.layout.activity_settings );
        super.changeToolbarTitle( "Settings" );

        //*****************************Reference to the view objects*************
        readAndWritePermissionSwitch = findViewById( R.id.readAndWritePermissionSwitch );
        final SeekBar decoderSpanBar = findViewById( R.id.decoderSpanBar );
        final TextView decoderSpanText = findViewById( R.id.decoderSpanText );


        //*******************************Decoder Span SeekBar**************************
        decoderSpanBar.setProgress( musicPlayerInfo.spanForDecoder - 2 );
        decoderSpanText.setText( "Frame analyzed per time:  " + Integer.toString( musicPlayerInfo.spanForDecoder ) );
        decoderSpanBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                decoderSpanBar.setProgress( progress );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicPlayerInfo.spanForDecoder = decoderSpanBar.getProgress()+2;
                decoderSpanText.setText( "Frame analyzed per time:  " + Integer.toString( musicPlayerInfo.spanForDecoder ) );
            }
        } );

        //*****************set switch for read and write permission*********************
        readAndWritePermissionSwitch.setChecked( musicPlayerInfo.readAndWritePermission);
        readAndWritePermissionSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    readAndWritePermission();
                }
                else {
                    if (musicPlayerInfo.readAndWritePermission){
                        readAndWritePermissionSwitch.setChecked( true );
                        Toast.makeText(getApplicationContext(),"Disable the app permission from the android settings",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }



    // ********************************************************************************************************
    //                ON PAUSE
    // ********************************************************************************************************
    @Override
    protected void onPause() {
        Log.d( "SETTINGS ACTIVITY:", "*****************onPause()*****************" );
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            dataManager.writeInfo( getApplicationContext() );
        }
        super.onPause();
    }


    // ********************************************************************************************************
    //                METHOD FOR REQUESTING BLUETOOTH PERMISSION
    // ********************************************************************************************************
    public void readAndWritePermission() {
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1 );
        ActivityCompat.requestPermissions( this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2 );
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.WAKE_LOCK}, 3 );
    }



    // ********************************************************************************************************
    //                LISTENER FOR PERMISSION RESULT
    // ********************************************************************************************************
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    musicPlayerInfo.readAndWritePermission = true;
                } else {
                    readAndWritePermissionSwitch.setChecked( false );
                }
                break;
        }
    }
}

