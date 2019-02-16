package com.example.pierpaolo.arduinoledcontroller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.skydoves.colorpickerpreference.ColorEnvelope;
import com.skydoves.colorpickerpreference.ColorListener;
import com.skydoves.colorpickerpreference.ColorPickerView;

/**
 * Created by pierpaolo on 25/08/18;
 */

/*
Allows user to manual decide the color of the led
 */

public class ActivityManualController extends BaseActivity {

    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private DataManager dataManager = new DataManager();
    private static ManualColorController manualColorController = new ManualColorController();
    private BluetoothComunication bluetoothComunication = BluetoothComunication.getBluetoothComunication();


    // ********************************************************************************************************
    //                                 ON CREATE
    // ********************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "ACT. MANUAL CONTROLLER", "*********************onCreate()********************" );
        super.onCreate( savedInstanceState );

        //************************Preparation*********************************
        ITEM_SELECTED = 3;
        super.setContentView( R.layout.activity_manualcontroller );
        super.changeToolbarTitle( "Manual Controller" );

        //********************** Reference  the view items*************************
        final Switch flashSwitch = findViewById( R.id.flashSwitch );
        final Switch fadeSwitch = findViewById( R.id.fadeSwitch );
        final Switch enableManualControl = findViewById( R.id.enableManualControlSwitch );
        SeekBar intensityBar = findViewById( R.id.intensityBar );
        SeekBar fadeSpeedBar = findViewById( R.id.fadeSpeedBar );
        SeekBar flashSpeedBar = findViewById( R.id.flashSpeedBar );
        ColorPickerView colorPickerView = findViewById( R.id.colorPickerView );
        final LinearLayout linearLayout = findViewById( R.id.currentColor );
        final Switch enableMusicControl = findViewById( R.id.enableMusicControlSwitch );

        linearLayout.setBackgroundColor( Color.rgb( musicPlayerInfo.manualColor[0], musicPlayerInfo.manualColor[1], musicPlayerInfo.manualColor[2] ) );
        //*******************Color Picker*************************************
        colorPickerView.setColorListener( new ColorListener() {
            @Override
            public void onColorSelected(ColorEnvelope colorEnvelope) {
                musicPlayerInfo.manualColor = colorEnvelope.getColorRGB();
                linearLayout.setBackgroundColor( colorEnvelope.getColor() );
                manualColorController.setColor();
            }
        } );

        //***********************Intensity Bar***************************
        intensityBar.setMax( 200 );
        intensityBar.setProgress( (int) (musicPlayerInfo.colorIntensity * 255) );
        intensityBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress( i );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicPlayerInfo.colorIntensity = (double) seekBar.getProgress() / 255;
                manualColorController.setColor();
            }
        } );

        //*************************Flash Bar*****************************
        flashSpeedBar.setMax( 200 );
        flashSpeedBar.setProgress( musicPlayerInfo.speedFlash - 30 );
        flashSpeedBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress( i );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicPlayerInfo.speedFlash = seekBar.getProgress() + 30;

            }
        } );

        //*************************Fade Bar********************************
        fadeSpeedBar.setMax( 300 );
        fadeSpeedBar.setProgress( musicPlayerInfo.speedFade-30 );
        fadeSpeedBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBar.setProgress( i );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicPlayerInfo.speedFade = seekBar.getProgress() + 30;

            }
        } );

        //**********************Fade Switch*****************************************
        fadeSwitch.setChecked( musicPlayerInfo.isFade );
        fadeSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( isChecked ){
                    musicPlayerInfo.isFlash = false;
                    flashSwitch.setChecked( musicPlayerInfo.isFlash );
                }
                musicPlayerInfo.isFade = isChecked;
                manualColorController.fade();

            }
        } );

        //***********************Flash Switch**************************************
        flashSwitch.setChecked( musicPlayerInfo.isFlash );
        flashSwitch.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        musicPlayerInfo.isFade = false;
                        fadeSwitch.setChecked( musicPlayerInfo.isFade );
                    }
                    musicPlayerInfo.isFlash = isChecked;
                    manualColorController.flash();
            }
        } );

        //***********************Enable Manual Control Switch*************************
        enableManualControl.setChecked( musicPlayerInfo.manualControlEnable );
        enableManualControl.setOnCheckedChangeListener( new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicPlayerInfo.musicControlEnable = false;
                    enableMusicControl.setChecked(false);
                    manualColorController.setColor();
                    manualColorController.fade();
                    manualColorController.flash();
                }
                else {
                    bluetoothComunication.switchOff();
                }
                musicPlayerInfo.manualControlEnable = isChecked;
            }
        } );

        //*******************************Enable Manual Control Switch*************************
        enableMusicControl.setChecked( musicPlayerInfo.musicControlEnable );
        enableMusicControl.setOnCheckedChangeListener( new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    musicPlayerInfo.manualControlEnable = false;
                    enableManualControl.setChecked(false);
                }
                else {
                    bluetoothComunication.switchOff();
                }
                musicPlayerInfo.musicControlEnable = isChecked;
            }
        } );
    }


    // ********************************************************************************************************
    //                                 ON PAUSE
    // ********************************************************************************************************
    @Override
    protected void onPause() {
        Log.d( "ACT. MANUAL CONTROLLER", "*********************onPause()********************" );
        if (ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED) {
            dataManager.writeInfo( getApplicationContext() );
        }
        super.onPause();
    }

}
