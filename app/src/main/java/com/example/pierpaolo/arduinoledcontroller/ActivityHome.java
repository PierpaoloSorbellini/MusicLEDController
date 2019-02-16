package com.example.pierpaolo.arduinoledcontroller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by pierpaolo on 08/03/18;
 */

/*
Contains guide and reference for the app and ask permission at first access
 */

//TODO: add guide with the procedure to mount your own led Controller


public class ActivityHome extends BaseActivity {

    private DataManager dataManager = new DataManager();
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();



    // ********************************************************************************************************
    //                                 ON CREATE
    // ********************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "ACTIVITY HOME:", "*****************onCreate()*****************" );
        super.onCreate( savedInstanceState );

        //*********************Preparation************************
        ITEM_SELECTED = 0;
        super.setContentView( R.layout.activity_home );
        super.changeToolbarTitle( "Home" );

        //**********************Request Permission************************
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1 );
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2 );
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.WAKE_LOCK}, 3 );
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.BLUETOOTH}, 4 );
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 5 );
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 6 );

        //*************************Read information from text file***********************
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            dataManager.readInfo( getApplicationContext() );
        }
    }



    // ********************************************************************************************************
    //                                 ON PAUSE
    // ********************************************************************************************************
    @Override
    protected void onPause() {
        Log.d( "ACTIVITY HOME:", "*****************onPause()*****************" );
        super.onPause();
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
                }
                break;
        }
    }



}
